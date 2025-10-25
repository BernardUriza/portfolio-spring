package com.portfolio.controller;

import com.github.benmanes.caffeine.cache.Cache;
import com.portfolio.service.KeepAliveService;
import com.portfolio.service.StartupNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for monitoring server status, keep-alive, and diagnostics.
 * Created by Bernard Orozco
 */
@RestController
@RequestMapping("/api/monitoring")
@CrossOrigin(origins = "*")
public class MonitoringController {

    @Autowired(required = false)
    private KeepAliveService keepAliveService;

    @Autowired
    private StartupNotificationService startupNotificationService;

    @Autowired
    private CacheManager cacheManager;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Get comprehensive server status including keep-alive statistics
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getServerStatus() {
        Map<String, Object> response = new HashMap<>();

        // Basic server info
        StartupNotificationService.ServerStatus serverStatus = startupNotificationService.getServerStatus();
        response.put("application", serverStatus.applicationName);
        response.put("profiles", serverStatus.activeProfiles);
        response.put("uptimeMs", serverStatus.uptimeMs);
        response.put("uptimeFormatted", formatUptime(serverStatus.uptimeMs));
        response.put("port", serverStatus.port);
        response.put("isRenderEnvironment", serverStatus.isRenderEnvironment);
        response.put("currentTime", LocalDateTime.now().format(FORMATTER));

        // Keep-alive statistics if service is enabled
        if (keepAliveService != null) {
            KeepAliveService.KeepAliveStats keepAliveStats = keepAliveService.getStats();
            Map<String, Object> keepAlive = new HashMap<>();
            keepAlive.put("enabled", true);
            keepAlive.put("url", keepAliveStats.url);
            keepAlive.put("intervalMs", keepAliveStats.intervalMs);
            keepAlive.put("intervalMinutes", keepAliveStats.intervalMs / 60000.0);
            keepAlive.put("successCount", keepAliveStats.successCount);
            keepAlive.put("failureCount", keepAliveStats.failureCount);

            if (keepAliveStats.lastSuccessTime > 0) {
                LocalDateTime lastSuccess = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(keepAliveStats.lastSuccessTime),
                    ZoneId.systemDefault()
                );
                keepAlive.put("lastSuccessTime", lastSuccess.format(FORMATTER));
                keepAlive.put("minutesSinceLastSuccess",
                    (System.currentTimeMillis() - keepAliveStats.lastSuccessTime) / 60000.0);
            } else {
                keepAlive.put("lastSuccessTime", null);
                keepAlive.put("minutesSinceLastSuccess", null);
            }

            response.put("keepAlive", keepAlive);
        } else {
            Map<String, Object> keepAlive = new HashMap<>();
            keepAlive.put("enabled", false);
            keepAlive.put("message", "Keep-alive service is disabled");
            response.put("keepAlive", keepAlive);
        }

        // Memory usage
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("maxMemoryMB", runtime.maxMemory() / (1024 * 1024));
        memory.put("totalMemoryMB", runtime.totalMemory() / (1024 * 1024));
        memory.put("freeMemoryMB", runtime.freeMemory() / (1024 * 1024));
        memory.put("usedMemoryMB", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
        response.put("memory", memory);

        return ResponseEntity.ok(response);
    }

    /**
     * Simple wake-up check endpoint
     * Returns quickly to confirm server is awake
     */
    @GetMapping("/awake")
    public ResponseEntity<Map<String, Object>> checkAwake() {
        Map<String, Object> response = new HashMap<>();
        response.put("awake", true);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Get cache statistics for all configured caches
     */
    @GetMapping("/cache/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Map<String, Object>> cacheStats = new HashMap<>();

        for (String cacheName : cacheManager.getCacheNames()) {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache instanceof CaffeineCache caffeineCache) {
                Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
                com.github.benmanes.caffeine.cache.stats.CacheStats stats = nativeCache.stats();

                Map<String, Object> stat = new HashMap<>();
                stat.put("size", nativeCache.estimatedSize());
                stat.put("hitCount", stats.hitCount());
                stat.put("missCount", stats.missCount());
                stat.put("hitRate", stats.hitRate());
                stat.put("missRate", stats.missRate());
                stat.put("loadSuccessCount", stats.loadSuccessCount());
                stat.put("loadFailureCount", stats.loadFailureCount());
                stat.put("totalLoadTime", stats.totalLoadTime());
                stat.put("evictionCount", stats.evictionCount());
                stat.put("evictionWeight", stats.evictionWeight());

                cacheStats.put(cacheName, stat);
            }
        }

        response.put("caches", cacheStats);
        response.put("cacheNames", cacheManager.getCacheNames());
        response.put("timestamp", LocalDateTime.now().format(FORMATTER));

        return ResponseEntity.ok(response);
    }

    /**
     * Format uptime in human-readable format
     */
    private String formatUptime(long uptimeMs) {
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }
}