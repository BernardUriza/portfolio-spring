package com.portfolio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitingService {
    
    private final FeatureFlagService featureFlagService;
    
    // Rate limiting storage: key -> (count, lastResetTime)
    private final Map<String, RateLimitEntry> rateLimitCache = new ConcurrentHashMap<>();
    
    /**
     * Check if request is within rate limits
     */
    public boolean isWithinRateLimit(String clientId, RateLimitType type) {
        if (!featureFlagService.isRateLimitingEnabled()) {
            return true;
        }
        
        String key = buildKey(clientId, type);
        int maxRequests = getMaxRequestsForType(type);
        ChronoUnit timeUnit = getTimeUnitForType(type);
        
        RateLimitEntry entry = rateLimitCache.computeIfAbsent(key, k -> new RateLimitEntry());
        
        synchronized (entry) {
            LocalDateTime now = LocalDateTime.now();
            
            // Reset counter if time window has passed
            if (entry.lastResetTime == null || 
                ChronoUnit.MINUTES.between(entry.lastResetTime, now) >= getTimeWindowMinutes(type)) {
                entry.count.set(0);
                entry.lastResetTime = now;
            }
            
            // Check if within limit
            if (entry.count.get() >= maxRequests) {
                log.warn("Rate limit exceeded for {} (type: {}, count: {}, max: {})", 
                        clientId, type, entry.count.get(), maxRequests);
                return false;
            }
            
            // Increment counter
            entry.count.incrementAndGet();
            log.debug("Rate limit check passed for {} (type: {}, count: {}, max: {})", 
                     clientId, type, entry.count.get(), maxRequests);
            return true;
        }
    }
    
    /**
     * Get remaining requests for client and type
     */
    public int getRemainingRequests(String clientId, RateLimitType type) {
        if (!featureFlagService.isRateLimitingEnabled()) {
            return Integer.MAX_VALUE;
        }
        
        String key = buildKey(clientId, type);
        int maxRequests = getMaxRequestsForType(type);
        
        RateLimitEntry entry = rateLimitCache.get(key);
        if (entry == null) {
            return maxRequests;
        }
        
        synchronized (entry) {
            LocalDateTime now = LocalDateTime.now();
            
            // Check if time window has passed
            if (entry.lastResetTime == null || 
                ChronoUnit.MINUTES.between(entry.lastResetTime, now) >= getTimeWindowMinutes(type)) {
                return maxRequests;
            }
            
            return Math.max(0, maxRequests - entry.count.get());
        }
    }
    
    /**
     * Get time until rate limit reset
     */
    public long getTimeUntilReset(String clientId, RateLimitType type) {
        if (!featureFlagService.isRateLimitingEnabled()) {
            return 0;
        }
        
        String key = buildKey(clientId, type);
        RateLimitEntry entry = rateLimitCache.get(key);
        
        if (entry == null || entry.lastResetTime == null) {
            return 0;
        }
        
        synchronized (entry) {
            LocalDateTime resetTime = entry.lastResetTime.plus(getTimeWindowMinutes(type), ChronoUnit.MINUTES);
            LocalDateTime now = LocalDateTime.now();
            
            if (now.isAfter(resetTime)) {
                return 0;
            }
            
            return ChronoUnit.SECONDS.between(now, resetTime);
        }
    }
    
    /**
     * Clear rate limit for specific client and type (admin use)
     */
    public void clearRateLimit(String clientId, RateLimitType type) {
        String key = buildKey(clientId, type);
        rateLimitCache.remove(key);
        log.info("Rate limit cleared for {} (type: {})", clientId, type);
    }
    
    /**
     * Clear all rate limits (admin use)
     */
    public void clearAllRateLimits() {
        rateLimitCache.clear();
        log.info("All rate limits cleared");
    }
    
    private String buildKey(String clientId, RateLimitType type) {
        return clientId + ":" + type.name();
    }
    
    private int getMaxRequestsForType(RateLimitType type) {
        return switch (type) {
            case ADMIN_ENDPOINTS -> featureFlagService.getAdminEndpointsRateLimit();
            case FACTORY_RESET -> featureFlagService.getFactoryResetRateLimit();
            case SYNC_OPERATIONS -> featureFlagService.getSyncOperationsRateLimit();
            case AI_CURATION -> featureFlagService.getAiCurationRateLimit();
        };
    }
    
    private ChronoUnit getTimeUnitForType(RateLimitType type) {
        return switch (type) {
            case ADMIN_ENDPOINTS, SYNC_OPERATIONS, AI_CURATION -> ChronoUnit.MINUTES;
            case FACTORY_RESET -> ChronoUnit.HOURS;
        };
    }
    
    private long getTimeWindowMinutes(RateLimitType type) {
        return switch (type) {
            case ADMIN_ENDPOINTS, SYNC_OPERATIONS, AI_CURATION -> 1;
            case FACTORY_RESET -> 60;
        };
    }
    
    public enum RateLimitType {
        ADMIN_ENDPOINTS,
        FACTORY_RESET,
        SYNC_OPERATIONS,
        AI_CURATION
    }
    
    private static class RateLimitEntry {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile LocalDateTime lastResetTime;
    }
}