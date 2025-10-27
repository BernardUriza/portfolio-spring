package com.portfolio.controller;

import com.github.benmanes.caffeine.cache.Cache;
import com.portfolio.config.monitoring.QueryPerformanceInterceptor;
import com.portfolio.service.KeepAliveService;
import com.portfolio.service.StartupNotificationService;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
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

    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired(required = false)
    private QueryPerformanceInterceptor queryPerformanceInterceptor;

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
     * Comprehensive performance dashboard consolidating all metrics
     * Provides a single endpoint for monitoring database, cache, JVM, and application performance
     */
    @GetMapping("/performance/dashboard")
    public ResponseEntity<Map<String, Object>> getPerformanceDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("timestamp", LocalDateTime.now().format(FORMATTER));
        dashboard.put("timestampMs", System.currentTimeMillis());

        // 1. Database Performance Metrics
        dashboard.put("database", getDatabaseMetrics());

        // 2. Cache Performance Metrics
        dashboard.put("cache", getCachePerformanceMetrics());

        // 3. JVM Metrics
        dashboard.put("jvm", getJvmMetrics());

        // 4. Application Metrics
        dashboard.put("application", getApplicationMetrics());

        // 5. Performance Status Summary
        dashboard.put("status", calculatePerformanceStatus(dashboard));

        return ResponseEntity.ok(dashboard);
    }

    /**
     * Get database connection pool metrics (HikariCP)
     */
    private Map<String, Object> getDatabaseMetrics() {
        Map<String, Object> dbMetrics = new HashMap<>();

        if (dataSource != null && dataSource instanceof HikariDataSource hikariDataSource) {
            HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();

            Map<String, Object> connectionPool = new HashMap<>();
            connectionPool.put("activeConnections", poolMXBean.getActiveConnections());
            connectionPool.put("idleConnections", poolMXBean.getIdleConnections());
            connectionPool.put("totalConnections", poolMXBean.getTotalConnections());
            connectionPool.put("threadsAwaitingConnection", poolMXBean.getThreadsAwaitingConnection());

            dbMetrics.put("connectionPool", connectionPool);

            // Connection pool configuration
            Map<String, Object> poolConfig = new HashMap<>();
            poolConfig.put("maxPoolSize", hikariDataSource.getMaximumPoolSize());
            poolConfig.put("minIdle", hikariDataSource.getMinimumIdle());
            poolConfig.put("connectionTimeout", hikariDataSource.getConnectionTimeout());
            poolConfig.put("idleTimeout", hikariDataSource.getIdleTimeout());
            poolConfig.put("maxLifetime", hikariDataSource.getMaxLifetime());

            dbMetrics.put("poolConfig", poolConfig);

            // Health indicators
            Map<String, Object> health = new HashMap<>();
            health.put("poolUtilization", calculatePoolUtilization(poolMXBean));
            health.put("hasWaitingThreads", poolMXBean.getThreadsAwaitingConnection() > 0);
            health.put("status", poolMXBean.getThreadsAwaitingConnection() == 0 ? "healthy" : "degraded");

            dbMetrics.put("health", health);
        } else {
            dbMetrics.put("available", false);
            dbMetrics.put("message", "HikariCP DataSource not available");
        }

        return dbMetrics;
    }

    /**
     * Get cache performance metrics from all configured caches
     */
    private Map<String, Object> getCachePerformanceMetrics() {
        Map<String, Object> cacheMetrics = new HashMap<>();
        Map<String, Map<String, Object>> caches = new HashMap<>();

        long totalHits = 0;
        long totalMisses = 0;

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
                stat.put("evictionCount", stats.evictionCount());
                stat.put("averageLoadPenalty", stats.averageLoadPenalty());

                caches.put(cacheName, stat);

                totalHits += stats.hitCount();
                totalMisses += stats.missCount();
            }
        }

        cacheMetrics.put("caches", caches);
        cacheMetrics.put("totalCaches", cacheManager.getCacheNames().size());

        // Overall cache performance
        Map<String, Object> overall = new HashMap<>();
        overall.put("totalHits", totalHits);
        overall.put("totalMisses", totalMisses);
        overall.put("totalRequests", totalHits + totalMisses);
        overall.put("overallHitRate", (totalHits + totalMisses) > 0
            ? (double) totalHits / (totalHits + totalMisses)
            : 0.0);

        cacheMetrics.put("overall", overall);

        return cacheMetrics;
    }

    /**
     * Get JVM performance metrics (memory, threads, GC)
     */
    private Map<String, Object> getJvmMetrics() {
        Map<String, Object> jvmMetrics = new HashMap<>();

        // Memory metrics
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        Runtime runtime = Runtime.getRuntime();

        Map<String, Object> memory = new HashMap<>();
        memory.put("heapUsedMB", memoryMXBean.getHeapMemoryUsage().getUsed() / (1024 * 1024));
        memory.put("heapCommittedMB", memoryMXBean.getHeapMemoryUsage().getCommitted() / (1024 * 1024));
        memory.put("heapMaxMB", memoryMXBean.getHeapMemoryUsage().getMax() / (1024 * 1024));
        memory.put("heapUtilization", calculateMemoryUtilization(memoryMXBean));
        memory.put("nonHeapUsedMB", memoryMXBean.getNonHeapMemoryUsage().getUsed() / (1024 * 1024));
        memory.put("runtimeMaxMB", runtime.maxMemory() / (1024 * 1024));
        memory.put("runtimeFreeMB", runtime.freeMemory() / (1024 * 1024));

        jvmMetrics.put("memory", memory);

        // Thread metrics
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        Map<String, Object> threads = new HashMap<>();
        threads.put("threadCount", threadMXBean.getThreadCount());
        threads.put("peakThreadCount", threadMXBean.getPeakThreadCount());
        threads.put("daemonThreadCount", threadMXBean.getDaemonThreadCount());
        threads.put("totalStartedThreadCount", threadMXBean.getTotalStartedThreadCount());

        jvmMetrics.put("threads", threads);

        // GC metrics
        Map<String, Object> gc = new HashMap<>();
        gc.put("collectionCount", ManagementFactory.getGarbageCollectorMXBeans().stream()
            .mapToLong(garbageCollectorMXBean -> garbageCollectorMXBean.getCollectionCount())
            .sum());
        gc.put("collectionTimeMs", ManagementFactory.getGarbageCollectorMXBeans().stream()
            .mapToLong(garbageCollectorMXBean -> garbageCollectorMXBean.getCollectionTime())
            .sum());

        jvmMetrics.put("gc", gc);

        return jvmMetrics;
    }

    /**
     * Get application-level metrics
     */
    private Map<String, Object> getApplicationMetrics() {
        Map<String, Object> appMetrics = new HashMap<>();

        // Server status
        StartupNotificationService.ServerStatus serverStatus = startupNotificationService.getServerStatus();
        appMetrics.put("applicationName", serverStatus.applicationName);
        appMetrics.put("uptimeMs", serverStatus.uptimeMs);
        appMetrics.put("uptimeFormatted", formatUptime(serverStatus.uptimeMs));
        appMetrics.put("profiles", serverStatus.activeProfiles);
        appMetrics.put("port", serverStatus.port);
        appMetrics.put("environment", serverStatus.isRenderEnvironment ? "render" : "local");

        // Keep-alive statistics if available
        if (keepAliveService != null) {
            KeepAliveService.KeepAliveStats keepAliveStats = keepAliveService.getStats();
            Map<String, Object> keepAlive = new HashMap<>();
            keepAlive.put("successCount", keepAliveStats.successCount);
            keepAlive.put("failureCount", keepAliveStats.failureCount);
            keepAlive.put("successRate", keepAliveStats.successCount + keepAliveStats.failureCount > 0
                ? (double) keepAliveStats.successCount / (keepAliveStats.successCount + keepAliveStats.failureCount)
                : 0.0);

            appMetrics.put("keepAlive", keepAlive);
        }

        return appMetrics;
    }

    /**
     * Calculate overall performance status based on all metrics
     */
    private Map<String, Object> calculatePerformanceStatus(Map<String, Object> dashboard) {
        Map<String, Object> status = new HashMap<>();
        int score = 100; // Start with perfect score
        StringBuilder issues = new StringBuilder();

        // Check database pool utilization
        Map<String, Object> dbMetrics = (Map<String, Object>) dashboard.get("database");
        if (dbMetrics.containsKey("health")) {
            Map<String, Object> dbHealth = (Map<String, Object>) dbMetrics.get("health");
            double poolUtilization = (double) dbHealth.get("poolUtilization");

            if (poolUtilization > 0.9) {
                score -= 20;
                issues.append("Database pool utilization high (").append(String.format("%.0f%%", poolUtilization * 100)).append("). ");
            }

            if ((boolean) dbHealth.get("hasWaitingThreads")) {
                score -= 15;
                issues.append("Threads waiting for database connections. ");
            }
        }

        // Check cache hit rate
        Map<String, Object> cacheMetrics = (Map<String, Object>) dashboard.get("cache");
        Map<String, Object> cacheOverall = (Map<String, Object>) cacheMetrics.get("overall");
        double cacheHitRate = (double) cacheOverall.get("overallHitRate");

        if (cacheHitRate < 0.5 && (long) cacheOverall.get("totalRequests") > 100) {
            score -= 15;
            issues.append("Cache hit rate low (").append(String.format("%.0f%%", cacheHitRate * 100)).append("). ");
        }

        // Check JVM memory utilization
        Map<String, Object> jvmMetrics = (Map<String, Object>) dashboard.get("jvm");
        Map<String, Object> memory = (Map<String, Object>) jvmMetrics.get("memory");
        double memoryUtilization = (double) memory.get("heapUtilization");

        if (memoryUtilization > 0.85) {
            score -= 25;
            issues.append("JVM heap utilization critical (").append(String.format("%.0f%%", memoryUtilization * 100)).append("). ");
        } else if (memoryUtilization > 0.75) {
            score -= 10;
            issues.append("JVM heap utilization high (").append(String.format("%.0f%%", memoryUtilization * 100)).append("). ");
        }

        // Determine overall status
        String overallStatus;
        if (score >= 90) {
            overallStatus = "healthy";
        } else if (score >= 70) {
            overallStatus = "degraded";
        } else {
            overallStatus = "critical";
        }

        status.put("score", score);
        status.put("status", overallStatus);
        status.put("issues", issues.length() > 0 ? issues.toString().trim() : "No issues detected");
        status.put("recommendations", getRecommendations(score, issues.toString()));

        return status;
    }

    /**
     * Get performance recommendations based on detected issues
     */
    private String getRecommendations(int score, String issues) {
        if (score >= 90) {
            return "System performance is optimal. Continue monitoring.";
        }

        StringBuilder recommendations = new StringBuilder();

        if (issues.contains("Database pool")) {
            recommendations.append("Consider increasing HikariCP maxPoolSize. ");
        }

        if (issues.contains("Cache hit rate")) {
            recommendations.append("Review cache configuration and TTL settings. ");
        }

        if (issues.contains("JVM heap")) {
            recommendations.append("Increase JVM heap size (-Xmx) or investigate memory leaks. ");
        }

        if (issues.contains("waiting for")) {
            recommendations.append("Optimize database queries or add connection pooling. ");
        }

        return recommendations.length() > 0
            ? recommendations.toString().trim()
            : "Review metrics and optimize underperforming components.";
    }

    /**
     * Calculate connection pool utilization percentage
     */
    private double calculatePoolUtilization(HikariPoolMXBean poolMXBean) {
        int total = poolMXBean.getTotalConnections();
        int active = poolMXBean.getActiveConnections();

        return total > 0 ? (double) active / total : 0.0;
    }

    /**
     * Calculate JVM heap memory utilization percentage
     */
    private double calculateMemoryUtilization(MemoryMXBean memoryMXBean) {
        long used = memoryMXBean.getHeapMemoryUsage().getUsed();
        long max = memoryMXBean.getHeapMemoryUsage().getMax();

        return max > 0 ? (double) used / max : 0.0;
    }

    /**
     * Get query performance statistics
     * PERF-005: Query Logging & Slow Query Detection
     */
    @GetMapping("/query/statistics")
    public ResponseEntity<Map<String, Object>> getQueryStatistics() {
        Map<String, Object> response = new HashMap<>();

        if (queryPerformanceInterceptor == null) {
            response.put("available", false);
            response.put("message", "Query performance monitoring not enabled");
            return ResponseEntity.ok(response);
        }

        response.put("available", true);
        response.put("timestamp", LocalDateTime.now().format(FORMATTER));

        // Overall statistics
        Map<String, Object> overall = new HashMap<>();
        overall.put("totalQueries", queryPerformanceInterceptor.getTotalQueries());
        overall.put("slowQueries", queryPerformanceInterceptor.getSlowQueries());
        overall.put("slowQueryThresholdMs", queryPerformanceInterceptor.getSlowQueryThresholdMs());

        long totalQueries = queryPerformanceInterceptor.getTotalQueries();
        long slowQueries = queryPerformanceInterceptor.getSlowQueries();
        overall.put("slowQueryRate", totalQueries > 0 ? (double) slowQueries / totalQueries : 0.0);

        response.put("overall", overall);

        // Per-query statistics
        Map<String, Map<String, Object>> queryStats = new HashMap<>();
        queryPerformanceInterceptor.getQueryStatistics().forEach((query, stats) -> {
            Map<String, Object> stat = new HashMap<>();
            stat.put("executionCount", stats.getExecutionCount());
            stat.put("totalExecutionTimeMs", stats.getTotalExecutionTime());
            stat.put("averageExecutionTimeMs", stats.getAverageExecutionTime());
            stat.put("minExecutionTimeMs", stats.getMinExecutionTime());
            stat.put("maxExecutionTimeMs", stats.getMaxExecutionTime());
            stat.put("lastExecutionTime", LocalDateTime.ofInstant(
                Instant.ofEpochMilli(stats.getLastExecutionTime()),
                ZoneId.systemDefault()
            ).format(FORMATTER));

            queryStats.put(query, stat);
        });

        response.put("queries", queryStats);
        response.put("totalUniqueQueries", queryStats.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Get top N slowest queries
     * PERF-005: Query Logging & Slow Query Detection
     */
    @GetMapping("/query/slow")
    public ResponseEntity<Map<String, Object>> getSlowQueries(
            @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> response = new HashMap<>();

        if (queryPerformanceInterceptor == null) {
            response.put("available", false);
            response.put("message", "Query performance monitoring not enabled");
            return ResponseEntity.ok(response);
        }

        response.put("available", true);
        response.put("timestamp", LocalDateTime.now().format(FORMATTER));
        response.put("slowQueryThresholdMs", queryPerformanceInterceptor.getSlowQueryThresholdMs());
        response.put("limit", limit);

        // Get top slow queries
        Map<String, Map<String, Object>> slowQueries = new HashMap<>();
        queryPerformanceInterceptor.getTopSlowQueries(limit).forEach((query, stats) -> {
            Map<String, Object> stat = new HashMap<>();
            stat.put("executionCount", stats.getExecutionCount());
            stat.put("averageExecutionTimeMs", stats.getAverageExecutionTime());
            stat.put("maxExecutionTimeMs", stats.getMaxExecutionTime());

            slowQueries.put(query, stat);
        });

        response.put("slowQueries", slowQueries);
        response.put("count", slowQueries.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Get queries exceeding slow threshold
     * PERF-005: Query Logging & Slow Query Detection
     */
    @GetMapping("/query/above-threshold")
    public ResponseEntity<Map<String, Object>> getQueriesAboveThreshold() {
        Map<String, Object> response = new HashMap<>();

        if (queryPerformanceInterceptor == null) {
            response.put("available", false);
            response.put("message", "Query performance monitoring not enabled");
            return ResponseEntity.ok(response);
        }

        response.put("available", true);
        response.put("timestamp", LocalDateTime.now().format(FORMATTER));
        response.put("slowQueryThresholdMs", queryPerformanceInterceptor.getSlowQueryThresholdMs());

        // Get queries above threshold
        Map<String, Map<String, Object>> queriesAboveThreshold = new HashMap<>();
        queryPerformanceInterceptor.getSlowQueriesAboveThreshold().forEach((query, stats) -> {
            Map<String, Object> stat = new HashMap<>();
            stat.put("executionCount", stats.getExecutionCount());
            stat.put("averageExecutionTimeMs", stats.getAverageExecutionTime());
            stat.put("maxExecutionTimeMs", stats.getMaxExecutionTime());
            stat.put("exceedsThresholdBy", stats.getAverageExecutionTime() - queryPerformanceInterceptor.getSlowQueryThresholdMs());

            queriesAboveThreshold.put(query, stat);
        });

        response.put("queriesAboveThreshold", queriesAboveThreshold);
        response.put("count", queriesAboveThreshold.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Reset query performance statistics
     * PERF-005: Query Logging & Slow Query Detection
     */
    @PostMapping("/query/reset")
    public ResponseEntity<Map<String, Object>> resetQueryStatistics() {
        Map<String, Object> response = new HashMap<>();

        if (queryPerformanceInterceptor == null) {
            response.put("available", false);
            response.put("message", "Query performance monitoring not enabled");
            return ResponseEntity.ok(response);
        }

        queryPerformanceInterceptor.resetStatistics();

        response.put("status", "success");
        response.put("message", "Query performance statistics have been reset");
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