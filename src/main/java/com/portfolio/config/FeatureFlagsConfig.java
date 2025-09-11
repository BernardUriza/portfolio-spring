package com.portfolio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "portfolio.features")
public class FeatureFlagsConfig {
    
    /**
     * Auto sync feature flags
     */
    private AutoSync autoSync = new AutoSync();
    
    /**
     * AI curation feature flags
     */
    private AiCuration aiCuration = new AiCuration();
    
    /**
     * Factory reset feature flags
     */
    private FactoryReset factoryReset = new FactoryReset();
    
    /**
     * Admin endpoints feature flags
     */
    private AdminEndpoints adminEndpoints = new AdminEndpoints();
    
    /**
     * Rate limiting feature flags
     */
    private RateLimiting rateLimiting = new RateLimiting();
    
    @Data
    public static class AutoSync {
        private boolean enabled = true;
        private boolean allowManualTrigger = true;
        private boolean allowScheduledSync = true;
        private int maxConcurrentSyncs = 1;
        private int maxRetryAttempts = 3;
    }
    
    @Data
    public static class AiCuration {
        private boolean enabled = true;
        private boolean allowManualCuration = true;
        private boolean respectFieldProtections = true;
        private int maxAnalysisLength = 50000;
        private boolean skipEmptyReadmes = true;
    }
    
    @Data
    public static class FactoryReset {
        private boolean enabled = true;
        private boolean requireConfirmationHeader = true;
        private boolean allowSseStreaming = true;
        private int rateLimitMinutes = 10;
        private boolean auditAllOperations = true;
    }
    
    @Data
    public static class AdminEndpoints {
        private boolean enabled = true;
        private boolean allowPortfolioManagement = true;
        private boolean allowSourceRepositoryManagement = true;
        private boolean allowSyncConfiguration = true;
        private boolean allowCompletionAnalysis = true;
    }
    
    @Data
    public static class RateLimiting {
        private boolean enabled = true;
        private int adminEndpointsPerMinute = 60;
        private int factoryResetPerHour = 1;
        private int syncOperationsPerMinute = 10;
        private int aiCurationPerMinute = 30;
    }
}