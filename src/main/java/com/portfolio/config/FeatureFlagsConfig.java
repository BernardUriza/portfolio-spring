package com.portfolio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Creado por Bernard Orozco
 */
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

    // Getters and setters
    public AutoSync getAutoSync() { return autoSync; }
    public void setAutoSync(AutoSync autoSync) { this.autoSync = autoSync; }

    public AiCuration getAiCuration() { return aiCuration; }
    public void setAiCuration(AiCuration aiCuration) { this.aiCuration = aiCuration; }

    public FactoryReset getFactoryReset() { return factoryReset; }
    public void setFactoryReset(FactoryReset factoryReset) { this.factoryReset = factoryReset; }

    public AdminEndpoints getAdminEndpoints() { return adminEndpoints; }
    public void setAdminEndpoints(AdminEndpoints adminEndpoints) { this.adminEndpoints = adminEndpoints; }

    public RateLimiting getRateLimiting() { return rateLimiting; }
    public void setRateLimiting(RateLimiting rateLimiting) { this.rateLimiting = rateLimiting; }
    
    public static class AutoSync {
        private boolean enabled = true;
        private boolean allowManualTrigger = true;
        private boolean allowScheduledSync = true;
        private int maxConcurrentSyncs = 1;
        private int maxRetryAttempts = 3;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public boolean isAllowManualTrigger() { return allowManualTrigger; }
        public void setAllowManualTrigger(boolean allowManualTrigger) { this.allowManualTrigger = allowManualTrigger; }

        public boolean isAllowScheduledSync() { return allowScheduledSync; }
        public void setAllowScheduledSync(boolean allowScheduledSync) { this.allowScheduledSync = allowScheduledSync; }

        public int getMaxConcurrentSyncs() { return maxConcurrentSyncs; }
        public void setMaxConcurrentSyncs(int maxConcurrentSyncs) { this.maxConcurrentSyncs = maxConcurrentSyncs; }

        public int getMaxRetryAttempts() { return maxRetryAttempts; }
        public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }
    }
    
    public static class AiCuration {
        private boolean enabled = true;
        private boolean allowManualCuration = true;
        private boolean respectFieldProtections = true;
        private int maxAnalysisLength = 50000;
        private boolean skipEmptyReadmes = true;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public boolean isAllowManualCuration() { return allowManualCuration; }
        public void setAllowManualCuration(boolean allowManualCuration) { this.allowManualCuration = allowManualCuration; }

        public boolean isRespectFieldProtections() { return respectFieldProtections; }
        public void setRespectFieldProtections(boolean respectFieldProtections) { this.respectFieldProtections = respectFieldProtections; }

        public int getMaxAnalysisLength() { return maxAnalysisLength; }
        public void setMaxAnalysisLength(int maxAnalysisLength) { this.maxAnalysisLength = maxAnalysisLength; }

        public boolean isSkipEmptyReadmes() { return skipEmptyReadmes; }
        public void setSkipEmptyReadmes(boolean skipEmptyReadmes) { this.skipEmptyReadmes = skipEmptyReadmes; }
    }
    
    public static class FactoryReset {
        private boolean enabled = true;
        private boolean requireConfirmationHeader = true;
        private boolean allowSseStreaming = true;
        private int rateLimitMinutes = 10;
        private boolean auditAllOperations = true;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public boolean isRequireConfirmationHeader() { return requireConfirmationHeader; }
        public void setRequireConfirmationHeader(boolean requireConfirmationHeader) { this.requireConfirmationHeader = requireConfirmationHeader; }

        public boolean isAllowSseStreaming() { return allowSseStreaming; }
        public void setAllowSseStreaming(boolean allowSseStreaming) { this.allowSseStreaming = allowSseStreaming; }

        public int getRateLimitMinutes() { return rateLimitMinutes; }
        public void setRateLimitMinutes(int rateLimitMinutes) { this.rateLimitMinutes = rateLimitMinutes; }

        public boolean isAuditAllOperations() { return auditAllOperations; }
        public void setAuditAllOperations(boolean auditAllOperations) { this.auditAllOperations = auditAllOperations; }
    }
    
    public static class AdminEndpoints {
        private boolean enabled = true;
        private boolean allowPortfolioManagement = true;
        private boolean allowSourceRepositoryManagement = true;
        private boolean allowSyncConfiguration = true;
        private boolean allowCompletionAnalysis = true;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public boolean isAllowPortfolioManagement() { return allowPortfolioManagement; }
        public void setAllowPortfolioManagement(boolean allowPortfolioManagement) { this.allowPortfolioManagement = allowPortfolioManagement; }

        public boolean isAllowSourceRepositoryManagement() { return allowSourceRepositoryManagement; }
        public void setAllowSourceRepositoryManagement(boolean allowSourceRepositoryManagement) { this.allowSourceRepositoryManagement = allowSourceRepositoryManagement; }

        public boolean isAllowSyncConfiguration() { return allowSyncConfiguration; }
        public void setAllowSyncConfiguration(boolean allowSyncConfiguration) { this.allowSyncConfiguration = allowSyncConfiguration; }

        public boolean isAllowCompletionAnalysis() { return allowCompletionAnalysis; }
        public void setAllowCompletionAnalysis(boolean allowCompletionAnalysis) { this.allowCompletionAnalysis = allowCompletionAnalysis; }
    }
    
    public static class RateLimiting {
        private boolean enabled = true;
        private int adminEndpointsPerMinute = 60;
        private int factoryResetPerHour = 1;
        private int syncOperationsPerMinute = 10;
        private int aiCurationPerMinute = 30;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public int getAdminEndpointsPerMinute() { return adminEndpointsPerMinute; }
        public void setAdminEndpointsPerMinute(int adminEndpointsPerMinute) { this.adminEndpointsPerMinute = adminEndpointsPerMinute; }

        public int getFactoryResetPerHour() { return factoryResetPerHour; }
        public void setFactoryResetPerHour(int factoryResetPerHour) { this.factoryResetPerHour = factoryResetPerHour; }

        public int getSyncOperationsPerMinute() { return syncOperationsPerMinute; }
        public void setSyncOperationsPerMinute(int syncOperationsPerMinute) { this.syncOperationsPerMinute = syncOperationsPerMinute; }

        public int getAiCurationPerMinute() { return aiCurationPerMinute; }
        public void setAiCurationPerMinute(int aiCurationPerMinute) { this.aiCurationPerMinute = aiCurationPerMinute; }
    }
}