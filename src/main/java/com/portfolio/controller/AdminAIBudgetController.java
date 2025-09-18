package com.portfolio.controller;

import com.portfolio.aspect.RateLimit;
import com.portfolio.aspect.RequiresFeature;
import com.portfolio.service.ClaudeTokenBudgetService;
import com.portfolio.service.RateLimitingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Admin controller for managing Claude AI token budget
 */
@RestController
@RequestMapping("/api/admin/ai")
public class AdminAIBudgetController {
    private static final Logger log = LoggerFactory.getLogger(AdminAIBudgetController.class);
    private final ClaudeTokenBudgetService tokenBudgetService;

    public AdminAIBudgetController(ClaudeTokenBudgetService tokenBudgetService) {
        this.tokenBudgetService = tokenBudgetService;
    }
    
    /**
     * Get current token budget status
     */
    @GetMapping("/budget")
    @RequiresFeature("admin_endpoints")
    @RateLimit(type = RateLimitingService.RateLimitType.ADMIN_ENDPOINTS)
    public ResponseEntity<Map<String, Object>> getBudgetStatus() {
        
        ClaudeTokenBudgetService.BudgetStatus status = tokenBudgetService.getBudgetStatus();
        
        return ResponseEntity.ok(Map.of(
            "budget", Map.of(
                "dailyBudget", status.getDailyBudget(),
                "currentUsage", status.getCurrentUsage(),
                "remainingTokens", status.getRemainingTokens(),
                "usagePercentage", Math.round(status.getUsagePercentage() * 100.0) / 100.0,
                "usagePercentageDisplay", String.format("%.1f%%", status.getUsagePercentage() * 100)
            ),
            "status", Map.of(
                "warnThresholdExceeded", status.isWarnThresholdExceeded(),
                "budgetExceeded", status.isBudgetExceeded(),
                "canUseAI", !status.isBudgetExceeded()
            ),
            "schedule", Map.of(
                "resetDate", status.getResetDate().toString(),
                "nextResetTime", status.getNextResetTime().toString(),
                "hoursUntilReset", java.time.Duration.between(LocalDateTime.now(), status.getNextResetTime()).toHours()
            ),
            "timestamp", LocalDateTime.now().toString()
        ));
    }
    
    /**
     * Reset token budget (admin override)
     */
    @PostMapping("/budget/reset")
    @RequiresFeature("admin_endpoints")
    @RateLimit(type = RateLimitingService.RateLimitType.ADMIN_ENDPOINTS)
    public ResponseEntity<Map<String, Object>> resetBudget() {
        
        ClaudeTokenBudgetService.BudgetStatus beforeReset = tokenBudgetService.getBudgetStatus();
        
        tokenBudgetService.resetBudget();
        
        ClaudeTokenBudgetService.BudgetStatus afterReset = tokenBudgetService.getBudgetStatus();
        
        log.info("Claude token budget manually reset - previous usage: {}, new usage: {}", 
                beforeReset.getCurrentUsage(), afterReset.getCurrentUsage());
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Claude token budget reset successfully",
            "previousUsage", beforeReset.getCurrentUsage(),
            "currentUsage", afterReset.getCurrentUsage(),
            "resetAt", LocalDateTime.now().toString(),
            "newBudget", Map.of(
                "dailyBudget", afterReset.getDailyBudget(),
                "remainingTokens", afterReset.getRemainingTokens(),
                "usagePercentage", 0.0
            )
        ));
    }
    
    /**
     * Get token usage recommendations
     */
    @GetMapping("/budget/recommendations")
    @RequiresFeature("admin_endpoints")
    @RateLimit(type = RateLimitingService.RateLimitType.ADMIN_ENDPOINTS)
    public ResponseEntity<Map<String, Object>> getBudgetRecommendations() {
        
        ClaudeTokenBudgetService.BudgetStatus status = tokenBudgetService.getBudgetStatus();
        
        return ResponseEntity.ok(Map.of(
            "recommendations", getBudgetRecommendations(status),
            "estimates", Map.of(
                "tokensPerAnalysis", "~2000-4000 tokens",
                "analysesRemaining", Math.max(0, status.getRemainingTokens() / 3000),
                "conservativeEstimate", "Assuming 3000 tokens per repository analysis"
            ),
            "suggestions", getBudgetSuggestions(status)
        ));
    }
    
    /**
     * Simulate token usage for planning
     */
    @PostMapping("/budget/simulate")
    @RequiresFeature("admin_endpoints")
    @RateLimit(type = RateLimitingService.RateLimitType.ADMIN_ENDPOINTS)
    public ResponseEntity<Map<String, Object>> simulateTokenUsage(
            @RequestParam(defaultValue = "1") int repositoriesToAnalyze,
            @RequestParam(defaultValue = "3000") int tokensPerRepository) {
        
        ClaudeTokenBudgetService.BudgetStatus currentStatus = tokenBudgetService.getBudgetStatus();
        
        int totalTokensNeeded = repositoriesToAnalyze * tokensPerRepository;
        int remainingAfterOperation = currentStatus.getRemainingTokens() - totalTokensNeeded;
        boolean wouldExceedBudget = remainingAfterOperation < 0;
        
        return ResponseEntity.ok(Map.of(
            "simulation", Map.of(
                "repositoriesToAnalyze", repositoriesToAnalyze,
                "tokensPerRepository", tokensPerRepository,
                "totalTokensNeeded", totalTokensNeeded,
                "currentRemaining", currentStatus.getRemainingTokens(),
                "remainingAfterOperation", Math.max(0, remainingAfterOperation),
                "wouldExceedBudget", wouldExceedBudget
            ),
            "recommendation", wouldExceedBudget ? 
                "This operation would exceed your daily budget. Consider reducing the scope or waiting for budget reset." :
                "This operation is within your daily budget.",
            "alternatives", wouldExceedBudget ? 
                Map.of(
                    "maxRepositoriesWithCurrentBudget", currentStatus.getRemainingTokens() / tokensPerRepository,
                    "waitForReset", "Budget resets at " + currentStatus.getNextResetTime()
                ) : null
        ));
    }
    
    private java.util.List<String> getBudgetRecommendations(ClaudeTokenBudgetService.BudgetStatus status) {
        java.util.List<String> recommendations = new java.util.ArrayList<>();
        
        if (status.isBudgetExceeded()) {
            recommendations.add("Daily budget exceeded - AI curation is temporarily disabled");
            recommendations.add("Budget will reset at " + status.getNextResetTime());
            recommendations.add("Consider increasing daily budget if this happens frequently");
        } else if (status.isWarnThresholdExceeded()) {
            recommendations.add("Warning: Approaching daily budget limit");
            recommendations.add("Consider prioritizing most important repositories for analysis");
            recommendations.add("Monitor usage closely for remainder of day");
        } else {
            double usagePercentage = status.getUsagePercentage() * 100;
            if (usagePercentage < 25) {
                recommendations.add("Budget usage is low - good time for bulk operations");
            } else if (usagePercentage < 50) {
                recommendations.add("Budget usage is moderate - continue normal operations");
            } else {
                recommendations.add("Budget usage is getting high - monitor remaining operations");
            }
        }
        
        return recommendations;
    }
    
    private java.util.List<String> getBudgetSuggestions(ClaudeTokenBudgetService.BudgetStatus status) {
        java.util.List<String> suggestions = new java.util.ArrayList<>();
        
        suggestions.add("Use field protection to prevent unnecessary re-analysis of stable content");
        suggestions.add("Focus AI curation on repositories with rich README content");
        suggestions.add("Schedule bulk operations early in the day when budget is fresh");
        
        if (status.getRemainingTokens() < 10000) {
            suggestions.add("Reserve remaining budget for critical repository updates");
        }
        
        return suggestions;
    }
}
