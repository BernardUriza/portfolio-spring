package com.portfolio.controller;

import com.portfolio.aspect.RateLimit;
import com.portfolio.aspect.RequiresFeature;
import com.portfolio.core.port.out.AIServicePort;
import com.portfolio.service.ClaudeTokenBudgetService;
import com.portfolio.service.RateLimitingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin controller for managing Claude AI token budget and batch operations
 */
@RestController
@RequestMapping("/api/admin/ai")
public class AdminAIBudgetController {
    private static final Logger log = LoggerFactory.getLogger(AdminAIBudgetController.class);
    private final ClaudeTokenBudgetService tokenBudgetService;
    private final AIServicePort aiService;

    public AdminAIBudgetController(ClaudeTokenBudgetService tokenBudgetService, AIServicePort aiService) {
        this.tokenBudgetService = tokenBudgetService;
        this.aiService = aiService;
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

    /**
     * Batch analyze multiple repositories
     * Optimizes token usage by processing multiple repositories in a single operation
     */
    @PostMapping("/analyze-batch")
    @RequiresFeature("admin_endpoints")
    @RateLimit(type = RateLimitingService.RateLimitType.ADMIN_ENDPOINTS)
    public ResponseEntity<Map<String, Object>> analyzeBatch(@Valid @RequestBody BatchAnalysisRequest request) {

        log.info("Batch analysis requested for {} repositories", request.repositories.size());

        // Validate budget before starting
        int estimatedTokens = request.repositories.size() * 3000; // Conservative estimate
        ClaudeTokenBudgetService.BudgetStatus status = tokenBudgetService.getBudgetStatus();

        if (estimatedTokens > status.getRemainingTokens()) {
            log.warn("Insufficient budget for batch analysis: need {}, have {}",
                    estimatedTokens, status.getRemainingTokens());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                    "status", "error",
                    "message", "Insufficient token budget for this operation",
                    "required", estimatedTokens,
                    "available", status.getRemainingTokens(),
                    "maxRepositoriesWithCurrentBudget", status.getRemainingTokens() / 3000
            ));
        }

        // Process repositories
        List<Map<String, Object>> results = new ArrayList<>();
        List<Map<String, Object>> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;
        long startTime = System.currentTimeMillis();

        for (RepositoryAnalysisRequest repo : request.repositories) {
            try {
                log.debug("Analyzing repository: {}", repo.name);

                AIServicePort.ClaudeAnalysisResult analysis = aiService.analyzeRepository(
                        repo.name,
                        repo.description,
                        repo.readmeContent,
                        repo.topics != null ? repo.topics : List.of(),
                        repo.language
                );

                results.add(Map.of(
                        "repository", repo.name,
                        "status", "success",
                        "project", Map.of(
                                "name", analysis.project.name,
                                "description", analysis.project.description,
                                "estimatedDurationWeeks", analysis.project.estimatedDurationWeeks != null ?
                                        analysis.project.estimatedDurationWeeks : 0,
                                "technologies", analysis.project.technologies,
                                "url", analysis.project.url != null ? analysis.project.url : ""
                        ),
                        "skills", analysis.skills,
                        "experiences", analysis.experiences
                ));
                successCount++;

            } catch (Exception e) {
                log.error("Failed to analyze repository {}: {}", repo.name, e.getMessage());
                errors.add(Map.of(
                        "repository", repo.name,
                        "error", e.getMessage()
                ));
                errorCount++;
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        ClaudeTokenBudgetService.BudgetStatus afterStatus = tokenBudgetService.getBudgetStatus();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "completed");
        response.put("summary", Map.of(
                "total", request.repositories.size(),
                "successful", successCount,
                "failed", errorCount,
                "durationMs", duration
        ));
        response.put("results", results);

        if (!errors.isEmpty()) {
            response.put("errors", errors);
        }

        response.put("budget", Map.of(
                "tokensUsedApprox", status.getRemainingTokens() - afterStatus.getRemainingTokens(),
                "remainingTokens", afterStatus.getRemainingTokens(),
                "usagePercentage", String.format("%.1f%%", afterStatus.getUsagePercentage() * 100)
        ));

        response.put("timestamp", LocalDateTime.now().toString());

        log.info("Batch analysis completed: {} successful, {} failed, duration: {}ms",
                successCount, errorCount, duration);

        return ResponseEntity.ok(response);
    }

    /**
     * Request DTO for batch analysis
     */
    public static class BatchAnalysisRequest {
        @NotNull(message = "Repositories list cannot be null")
        @Size(min = 1, max = 100, message = "Batch size must be between 1 and 100 repositories")
        @Valid
        public List<RepositoryAnalysisRequest> repositories;

        public BatchAnalysisRequest() {}

        public BatchAnalysisRequest(List<RepositoryAnalysisRequest> repositories) {
            this.repositories = repositories;
        }
    }

    /**
     * Request DTO for single repository analysis
     */
    public static class RepositoryAnalysisRequest {
        @NotBlank(message = "Repository name is required")
        @Size(max = 255, message = "Repository name cannot exceed 255 characters")
        public String name;

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        public String description;

        @Size(max = 50000, message = "README content cannot exceed 50KB")
        public String readmeContent;

        @Size(max = 20, message = "Maximum 20 topics allowed")
        public List<String> topics;

        @Size(max = 50, message = "Language name cannot exceed 50 characters")
        public String language;

        public RepositoryAnalysisRequest() {}

        public RepositoryAnalysisRequest(String name, String description, String readmeContent,
                                        List<String> topics, String language) {
            this.name = name;
            this.description = description;
            this.readmeContent = readmeContent;
            this.topics = topics;
            this.language = language;
        }
    }
}
