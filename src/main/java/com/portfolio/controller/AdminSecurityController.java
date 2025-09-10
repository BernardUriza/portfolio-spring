package com.portfolio.controller;

import com.portfolio.aspect.RateLimit;
import com.portfolio.aspect.RequiresFeature;
import com.portfolio.service.FeatureFlagService;
import com.portfolio.service.RateLimitingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/security")
@RequiredArgsConstructor
@Slf4j
public class AdminSecurityController {
    
    private final FeatureFlagService featureFlagService;
    private final RateLimitingService rateLimitingService;
    
    @Value("${portfolio.admin.security.enabled:true}")
    private boolean securityEnabled;
    
    @Value("${portfolio.admin.token:}")
    private String adminToken;
    
    /**
     * Get security status and feature flags overview
     */
    @GetMapping("/status")
    @RequiresFeature("admin_endpoints")
    @RateLimit(type = RateLimitingService.RateLimitType.ADMIN_ENDPOINTS)
    public ResponseEntity<Map<String, Object>> getSecurityStatus() {
        
        return ResponseEntity.ok(Map.of(
            "security", Map.of(
                "enabled", securityEnabled,
                "tokenConfigured", adminToken != null && !adminToken.trim().isEmpty(),
                "authenticationRequired", securityEnabled && adminToken != null && !adminToken.trim().isEmpty()
            ),
            "features", Map.of(
                "autoSync", Map.of(
                    "enabled", featureFlagService.isAutoSyncEnabled(),
                    "manualTriggerAllowed", featureFlagService.isManualSyncAllowed(),
                    "scheduledSyncAllowed", featureFlagService.isScheduledSyncAllowed()
                ),
                "aiCuration", Map.of(
                    "enabled", featureFlagService.isAiCurationEnabled(),
                    "manualCurationAllowed", featureFlagService.isManualCurationAllowed(),
                    "respectFieldProtections", featureFlagService.shouldRespectFieldProtections()
                ),
                "factoryReset", Map.of(
                    "enabled", featureFlagService.isFactoryResetEnabled(),
                    "requiresConfirmation", featureFlagService.requiresFactoryResetConfirmation()
                ),
                "adminEndpoints", Map.of(
                    "enabled", featureFlagService.areAdminEndpointsEnabled(),
                    "portfolioManagementAllowed", featureFlagService.isPortfolioManagementAllowed(),
                    "sourceRepositoryManagementAllowed", featureFlagService.isSourceRepositoryManagementAllowed()
                )
            ),
            "rateLimiting", Map.of(
                "enabled", featureFlagService.isRateLimitingEnabled(),
                "limits", Map.of(
                    "adminEndpointsPerMinute", featureFlagService.getAdminEndpointsRateLimit(),
                    "factoryResetPerHour", featureFlagService.getFactoryResetRateLimit(),
                    "syncOperationsPerMinute", featureFlagService.getSyncOperationsRateLimit(),
                    "aiCurationPerMinute", featureFlagService.getAiCurationRateLimit()
                )
            ),
            "timestamp", LocalDateTime.now().toString()
        ));
    }
    
    /**
     * Clear rate limits for a specific client (admin use)
     */
    @DeleteMapping("/rate-limits/{clientId}")
    @RequiresFeature("admin_endpoints")
    @RateLimit(type = RateLimitingService.RateLimitType.ADMIN_ENDPOINTS)
    public ResponseEntity<Map<String, Object>> clearClientRateLimits(
            @PathVariable String clientId,
            @RequestParam(required = false) String type) {
        
        try {
            if (type != null) {
                RateLimitingService.RateLimitType rateLimitType = 
                    RateLimitingService.RateLimitType.valueOf(type.toUpperCase());
                rateLimitingService.clearRateLimit(clientId, rateLimitType);
                
                log.info("Cleared {} rate limit for client: {}", rateLimitType, clientId);
                
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Rate limit cleared for client and type",
                    "clientId", clientId,
                    "type", rateLimitType.name()
                ));
            } else {
                // Clear all rate limit types for this client
                for (RateLimitingService.RateLimitType rateLimitType : RateLimitingService.RateLimitType.values()) {
                    rateLimitingService.clearRateLimit(clientId, rateLimitType);
                }
                
                log.info("Cleared all rate limits for client: {}", clientId);
                
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "All rate limits cleared for client",
                    "clientId", clientId
                ));
            }
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid rate limit type specified: {}", type);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Invalid rate limit type: " + type,
                "validTypes", RateLimitingService.RateLimitType.values()
            ));
        } catch (Exception e) {
            log.error("Failed to clear rate limits for client {}: {}", clientId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Failed to clear rate limits: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Clear all rate limits (admin use)
     */
    @DeleteMapping("/rate-limits")
    @RequiresFeature("admin_endpoints")
    @RateLimit(type = RateLimitingService.RateLimitType.ADMIN_ENDPOINTS)
    public ResponseEntity<Map<String, Object>> clearAllRateLimits() {
        try {
            rateLimitingService.clearAllRateLimits();
            
            log.info("Cleared all rate limits");
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "All rate limits cleared",
                "clearedAt", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            log.error("Failed to clear all rate limits: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Failed to clear all rate limits: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get rate limit status for client
     */
    @GetMapping("/rate-limits/{clientId}")
    @RequiresFeature("admin_endpoints")
    @RateLimit(type = RateLimitingService.RateLimitType.ADMIN_ENDPOINTS)
    public ResponseEntity<Map<String, Object>> getClientRateLimitStatus(@PathVariable String clientId) {
        
        Map<String, Object> rateLimitStatus = Map.of(
            "clientId", clientId,
            "rateLimits", Map.of(
                "ADMIN_ENDPOINTS", Map.of(
                    "remaining", rateLimitingService.getRemainingRequests(clientId, RateLimitingService.RateLimitType.ADMIN_ENDPOINTS),
                    "resetInSeconds", rateLimitingService.getTimeUntilReset(clientId, RateLimitingService.RateLimitType.ADMIN_ENDPOINTS)
                ),
                "FACTORY_RESET", Map.of(
                    "remaining", rateLimitingService.getRemainingRequests(clientId, RateLimitingService.RateLimitType.FACTORY_RESET),
                    "resetInSeconds", rateLimitingService.getTimeUntilReset(clientId, RateLimitingService.RateLimitType.FACTORY_RESET)
                ),
                "SYNC_OPERATIONS", Map.of(
                    "remaining", rateLimitingService.getRemainingRequests(clientId, RateLimitingService.RateLimitType.SYNC_OPERATIONS),
                    "resetInSeconds", rateLimitingService.getTimeUntilReset(clientId, RateLimitingService.RateLimitType.SYNC_OPERATIONS)
                ),
                "AI_CURATION", Map.of(
                    "remaining", rateLimitingService.getRemainingRequests(clientId, RateLimitingService.RateLimitType.AI_CURATION),
                    "resetInSeconds", rateLimitingService.getTimeUntilReset(clientId, RateLimitingService.RateLimitType.AI_CURATION)
                )
            ),
            "timestamp", LocalDateTime.now().toString()
        );
        
        return ResponseEntity.ok(rateLimitStatus);
    }
}