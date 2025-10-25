package com.portfolio.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Creado por Bernard Orozco
 */
@Service
public class ClaudeTokenBudgetService {

    private static final Logger log = LoggerFactory.getLogger(ClaudeTokenBudgetService.class);

    private final MeterRegistry meterRegistry;

    public ClaudeTokenBudgetService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    @Value("${portfolio.ai.claude.daily-token-budget:100000}")
    private int dailyTokenBudget;
    
    @Value("${portfolio.ai.claude.warn-threshold:0.8}")
    private double warnThreshold;
    
    @Value("${portfolio.ai.claude.budget-reset-hour:0}")
    private int budgetResetHour;
    
    private final AtomicInteger currentTokenUsage = new AtomicInteger(0);
    private final AtomicReference<LocalDate> lastResetDate = new AtomicReference<>(LocalDate.now());
    
    private Counter tokenUsageCounter;
    private Counter warnThresholdCounter;
    private Counter budgetExceededCounter;
    @SuppressWarnings("unused")
    private Gauge remainingTokensGauge;
    @SuppressWarnings("unused")
    private Gauge usagePercentageGauge;
    
    @PostConstruct
    public void init() {
        // Initialize metrics
        tokenUsageCounter = meterRegistry.counter("claude.tokens.used");
        warnThresholdCounter = meterRegistry.counter("claude.budget.warn_threshold_exceeded");
        budgetExceededCounter = meterRegistry.counter("claude.budget.exceeded");
        
        remainingTokensGauge = Gauge.builder("claude.tokens.remaining", this, service -> service.getRemainingTokens())
                .description("Remaining Claude tokens for today")
                .register(meterRegistry);
        
        usagePercentageGauge = Gauge.builder("claude.budget.usage_percentage", this, service -> service.getUsagePercentage())
                .description("Percentage of daily Claude budget used")
                .register(meterRegistry);
        
        log.info("Claude token budget service initialized: budget={}, warn_threshold={}", 
                dailyTokenBudget, warnThreshold);
    }
    
    /**
     * Check if we can use the specified number of tokens
     */
    public boolean canUseTokens(int tokens) {
        checkAndResetIfNewDay();
        
        int currentUsage = currentTokenUsage.get();
        int potentialUsage = currentUsage + tokens;
        
        if (potentialUsage > dailyTokenBudget) {
            log.warn("Claude token budget exceeded: requested={}, current={}, budget={}", 
                    tokens, currentUsage, dailyTokenBudget);
            budgetExceededCounter.increment();
            return false;
        }
        
        return true;
    }
    
    /**
     * Record token usage and return if budget allows
     */
    public BudgetResult useTokens(int tokens, String operation) {
        checkAndResetIfNewDay();
        
        int currentUsage = currentTokenUsage.get();
        int newUsage = currentUsage + tokens;
        
        if (newUsage > dailyTokenBudget) {
            log.warn("Claude token budget exceeded for operation '{}': requested={}, current={}, budget={}", 
                    operation, tokens, currentUsage, dailyTokenBudget);
            budgetExceededCounter.increment();
            return BudgetResult.budgetExceeded(currentUsage, dailyTokenBudget);
        }
        
        // Update usage atomically
        currentTokenUsage.set(newUsage);
        tokenUsageCounter.increment(tokens);
        
        // Check warn threshold
        double usagePercentage = (double) newUsage / dailyTokenBudget;
        if (usagePercentage >= warnThreshold && (double) currentUsage / dailyTokenBudget < warnThreshold) {
            log.warn("Claude token budget warning threshold exceeded: usage={}%, threshold={}%", 
                    usagePercentage * 100, warnThreshold * 100);
            warnThresholdCounter.increment();
        }
        
        log.debug("Claude tokens used for '{}': tokens={}, total_usage={}, remaining={}", 
                operation, tokens, newUsage, dailyTokenBudget - newUsage);
        
        return BudgetResult.success(newUsage, dailyTokenBudget, usagePercentage >= warnThreshold);
    }
    
    /**
     * Get current token usage
     */
    public int getCurrentUsage() {
        checkAndResetIfNewDay();
        return currentTokenUsage.get();
    }
    
    /**
     * Get remaining tokens
     */
    public int getRemainingTokens() {
        checkAndResetIfNewDay();
        return Math.max(0, dailyTokenBudget - currentTokenUsage.get());
    }
    
    /**
     * Get usage percentage (0.0 to 1.0)
     */
    public double getUsagePercentage() {
        checkAndResetIfNewDay();
        return (double) currentTokenUsage.get() / dailyTokenBudget;
    }
    
    /**
     * Get budget status
     */
    public BudgetStatus getBudgetStatus() {
        checkAndResetIfNewDay();
        
        int usage = currentTokenUsage.get();
        double percentage = (double) usage / dailyTokenBudget;
        
        return BudgetStatus.builder()
                .dailyBudget(dailyTokenBudget)
                .currentUsage(usage)
                .remainingTokens(Math.max(0, dailyTokenBudget - usage))
                .usagePercentage(percentage)
                .warnThresholdExceeded(percentage >= warnThreshold)
                .budgetExceeded(usage >= dailyTokenBudget)
                .resetDate(lastResetDate.get())
                .nextResetTime(getNextResetTime())
                .build();
    }
    
    /**
     * Manually reset budget (admin endpoint)
     */
    public void resetBudget() {
        currentTokenUsage.set(0);
        lastResetDate.set(LocalDate.now());
        
        log.info("Claude token budget manually reset: budget={}", dailyTokenBudget);
        meterRegistry.counter("claude.budget.manual_reset").increment();
    }
    
    /**
     * Scheduled reset at configured hour
     */
    @Scheduled(cron = "${portfolio.ai.claude.budget-reset-cron:0 0 0 * * *}")
    public void scheduledReset() {
        LocalDate today = LocalDate.now();
        if (!today.equals(lastResetDate.get())) {
            resetBudget();
            log.info("Claude token budget scheduled reset completed");
        }
    }
    
    private void checkAndResetIfNewDay() {
        LocalDate today = LocalDate.now();
        LocalDate lastReset = lastResetDate.get();
        
        if (!today.equals(lastReset)) {
            // Reset for new day
            int previousUsage = currentTokenUsage.getAndSet(0);
            lastResetDate.set(today);
            
            log.info("Claude token budget reset for new day: previous_usage={}, budget={}", 
                    previousUsage, dailyTokenBudget);
            meterRegistry.counter("claude.budget.daily_reset").increment();
        }
    }
    
    private LocalDateTime getNextResetTime() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return LocalDateTime.of(tomorrow, LocalTime.of(budgetResetHour, 0));
    }
    
    // Result classes
    public static class BudgetResult {
        private final boolean allowed;
        private final int currentUsage;
        private final int dailyBudget;
        private final boolean warnThresholdExceeded;
        private final String reason;
        
        private BudgetResult(boolean allowed, int currentUsage, int dailyBudget, 
                           boolean warnThresholdExceeded, String reason) {
            this.allowed = allowed;
            this.currentUsage = currentUsage;
            this.dailyBudget = dailyBudget;
            this.warnThresholdExceeded = warnThresholdExceeded;
            this.reason = reason;
        }
        
        public static BudgetResult success(int currentUsage, int dailyBudget, boolean warnThresholdExceeded) {
            return new BudgetResult(true, currentUsage, dailyBudget, warnThresholdExceeded, null);
        }
        
        public static BudgetResult budgetExceeded(int currentUsage, int dailyBudget) {
            return new BudgetResult(false, currentUsage, dailyBudget, true, 
                    "Daily token budget exceeded");
        }
        
        // Getters
        public boolean isAllowed() { return allowed; }
        public int getCurrentUsage() { return currentUsage; }
        public int getDailyBudget() { return dailyBudget; }
        public boolean isWarnThresholdExceeded() { return warnThresholdExceeded; }
        public String getReason() { return reason; }
        public int getRemainingTokens() { return Math.max(0, dailyBudget - currentUsage); }
    }
    
    public static class BudgetStatus {
        private int dailyBudget;
        private int currentUsage;
        private int remainingTokens;
        private double usagePercentage;
        private boolean warnThresholdExceeded;
        private boolean budgetExceeded;
        private LocalDate resetDate;
        private LocalDateTime nextResetTime;

        public BudgetStatus() {}

        public BudgetStatus(int dailyBudget, int currentUsage, int remainingTokens, double usagePercentage,
                           boolean warnThresholdExceeded, boolean budgetExceeded, LocalDate resetDate,
                           LocalDateTime nextResetTime) {
            this.dailyBudget = dailyBudget;
            this.currentUsage = currentUsage;
            this.remainingTokens = remainingTokens;
            this.usagePercentage = usagePercentage;
            this.warnThresholdExceeded = warnThresholdExceeded;
            this.budgetExceeded = budgetExceeded;
            this.resetDate = resetDate;
            this.nextResetTime = nextResetTime;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private int dailyBudget;
            private int currentUsage;
            private int remainingTokens;
            private double usagePercentage;
            private boolean warnThresholdExceeded;
            private boolean budgetExceeded;
            private LocalDate resetDate;
            private LocalDateTime nextResetTime;

            public Builder dailyBudget(int dailyBudget) {
                this.dailyBudget = dailyBudget;
                return this;
            }

            public Builder currentUsage(int currentUsage) {
                this.currentUsage = currentUsage;
                return this;
            }

            public Builder remainingTokens(int remainingTokens) {
                this.remainingTokens = remainingTokens;
                return this;
            }

            public Builder usagePercentage(double usagePercentage) {
                this.usagePercentage = usagePercentage;
                return this;
            }

            public Builder warnThresholdExceeded(boolean warnThresholdExceeded) {
                this.warnThresholdExceeded = warnThresholdExceeded;
                return this;
            }

            public Builder budgetExceeded(boolean budgetExceeded) {
                this.budgetExceeded = budgetExceeded;
                return this;
            }

            public Builder resetDate(LocalDate resetDate) {
                this.resetDate = resetDate;
                return this;
            }

            public Builder nextResetTime(LocalDateTime nextResetTime) {
                this.nextResetTime = nextResetTime;
                return this;
            }

            public BudgetStatus build() {
                return new BudgetStatus(dailyBudget, currentUsage, remainingTokens, usagePercentage,
                                       warnThresholdExceeded, budgetExceeded, resetDate, nextResetTime);
            }
        }

        // Getters and setters
        public int getDailyBudget() { return dailyBudget; }
        public void setDailyBudget(int dailyBudget) { this.dailyBudget = dailyBudget; }

        public int getCurrentUsage() { return currentUsage; }
        public void setCurrentUsage(int currentUsage) { this.currentUsage = currentUsage; }

        public int getRemainingTokens() { return remainingTokens; }
        public void setRemainingTokens(int remainingTokens) { this.remainingTokens = remainingTokens; }

        public double getUsagePercentage() { return usagePercentage; }
        public void setUsagePercentage(double usagePercentage) { this.usagePercentage = usagePercentage; }

        public boolean isWarnThresholdExceeded() { return warnThresholdExceeded; }
        public void setWarnThresholdExceeded(boolean warnThresholdExceeded) { this.warnThresholdExceeded = warnThresholdExceeded; }

        public boolean isBudgetExceeded() { return budgetExceeded; }
        public void setBudgetExceeded(boolean budgetExceeded) { this.budgetExceeded = budgetExceeded; }

        public LocalDate getResetDate() { return resetDate; }
        public void setResetDate(LocalDate resetDate) { this.resetDate = resetDate; }

        public LocalDateTime getNextResetTime() { return nextResetTime; }
        public void setNextResetTime(LocalDateTime nextResetTime) { this.nextResetTime = nextResetTime; }
    }
}
