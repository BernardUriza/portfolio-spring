package com.portfolio.service;

import com.portfolio.config.FeatureFlagsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureFlagService {
    
    private final FeatureFlagsConfig featureFlags;
    
    /**
     * Check if auto sync is enabled
     */
    public boolean isAutoSyncEnabled() {
        return featureFlags.getAutoSync().isEnabled();
    }
    
    /**
     * Check if manual sync trigger is allowed
     */
    public boolean isManualSyncAllowed() {
        return featureFlags.getAutoSync().isEnabled() && 
               featureFlags.getAutoSync().isAllowManualTrigger();
    }
    
    /**
     * Check if scheduled sync is allowed
     */
    public boolean isScheduledSyncAllowed() {
        return featureFlags.getAutoSync().isEnabled() && 
               featureFlags.getAutoSync().isAllowScheduledSync();
    }
    
    /**
     * Check if AI curation is enabled
     */
    public boolean isAiCurationEnabled() {
        return featureFlags.getAiCuration().isEnabled();
    }
    
    /**
     * Check if manual AI curation is allowed
     */
    public boolean isManualCurationAllowed() {
        return featureFlags.getAiCuration().isEnabled() && 
               featureFlags.getAiCuration().isAllowManualCuration();
    }
    
    /**
     * Check if field protections should be respected
     */
    public boolean shouldRespectFieldProtections() {
        return featureFlags.getAiCuration().isRespectFieldProtections();
    }
    
    /**
     * Check if factory reset is enabled
     */
    public boolean isFactoryResetEnabled() {
        return featureFlags.getFactoryReset().isEnabled();
    }
    
    /**
     * Check if factory reset requires confirmation header
     */
    public boolean requiresFactoryResetConfirmation() {
        return featureFlags.getFactoryReset().isRequireConfirmationHeader();
    }
    
    /**
     * Check if admin endpoints are enabled
     */
    public boolean areAdminEndpointsEnabled() {
        return featureFlags.getAdminEndpoints().isEnabled();
    }
    
    /**
     * Check if portfolio management endpoints are allowed
     */
    public boolean isPortfolioManagementAllowed() {
        return featureFlags.getAdminEndpoints().isEnabled() && 
               featureFlags.getAdminEndpoints().isAllowPortfolioManagement();
    }
    
    /**
     * Check if source repository management is allowed
     */
    public boolean isSourceRepositoryManagementAllowed() {
        return featureFlags.getAdminEndpoints().isEnabled() && 
               featureFlags.getAdminEndpoints().isAllowSourceRepositoryManagement();
    }
    
    /**
     * Check if rate limiting is enabled
     */
    public boolean isRateLimitingEnabled() {
        return featureFlags.getRateLimiting().isEnabled();
    }
    
    /**
     * Get admin endpoints rate limit per minute
     */
    public int getAdminEndpointsRateLimit() {
        return featureFlags.getRateLimiting().getAdminEndpointsPerMinute();
    }
    
    /**
     * Get factory reset rate limit per hour
     */
    public int getFactoryResetRateLimit() {
        return featureFlags.getRateLimiting().getFactoryResetPerHour();
    }
    
    /**
     * Get sync operations rate limit per minute
     */
    public int getSyncOperationsRateLimit() {
        return featureFlags.getRateLimiting().getSyncOperationsPerMinute();
    }
    
    /**
     * Get AI curation rate limit per minute
     */
    public int getAiCurationRateLimit() {
        return featureFlags.getRateLimiting().getAiCurationPerMinute();
    }
    
    /**
     * Validate if operation is allowed based on feature flags
     */
    public void validateFeatureEnabled(String featureName) {
        switch (featureName.toLowerCase()) {
            case "auto_sync":
                if (!isAutoSyncEnabled()) {
                    throw new UnsupportedOperationException("Auto sync feature is disabled");
                }
                break;
            case "manual_sync":
                if (!isManualSyncAllowed()) {
                    throw new UnsupportedOperationException("Manual sync is disabled");
                }
                break;
            case "scheduled_sync":
                if (!isScheduledSyncAllowed()) {
                    throw new UnsupportedOperationException("Scheduled sync is disabled");
                }
                break;
            case "ai_curation":
                if (!isAiCurationEnabled()) {
                    throw new UnsupportedOperationException("AI curation feature is disabled");
                }
                break;
            case "factory_reset":
                if (!isFactoryResetEnabled()) {
                    throw new UnsupportedOperationException("Factory reset feature is disabled");
                }
                break;
            case "admin_endpoints":
                if (!areAdminEndpointsEnabled()) {
                    throw new UnsupportedOperationException("Admin endpoints are disabled");
                }
                break;
            case "portfolio_management":
                if (!isPortfolioManagementAllowed()) {
                    throw new UnsupportedOperationException("Portfolio management is disabled");
                }
                break;
            case "source_repository_management":
                if (!isSourceRepositoryManagementAllowed()) {
                    throw new UnsupportedOperationException("Source repository management is disabled");
                }
                break;
            default:
                log.warn("Unknown feature flag validation requested: {}", featureName);
        }
    }
}
