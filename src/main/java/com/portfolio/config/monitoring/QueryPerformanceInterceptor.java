package com.portfolio.config.monitoring;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Hibernate StatementInspector for query performance monitoring and slow query detection.
 * Tracks query execution times, counts, and identifies slow queries above threshold.
 *
 * Created by Bernard Uriza Orozco for PERF-005
 */
@Component
public class QueryPerformanceInterceptor implements StatementInspector {

    private static final Logger logger = LoggerFactory.getLogger(QueryPerformanceInterceptor.class);

    @Value("${portfolio.query.slow-query-threshold-ms:100}")
    private long slowQueryThresholdMs;

    @Value("${portfolio.query.enable-slow-query-logging:true}")
    private boolean enableSlowQueryLogging;

    @Value("${portfolio.query.enable-query-statistics:true}")
    private boolean enableQueryStatistics;

    // Thread-safe storage for query metrics
    private final Map<String, QueryStatistics> queryStats = new ConcurrentHashMap<>();
    private final ThreadLocal<Long> queryStartTime = new ThreadLocal<>();
    private final ThreadLocal<String> currentQuery = new ThreadLocal<>();

    // Global counters
    private final AtomicLong totalQueries = new AtomicLong(0);
    private final AtomicLong slowQueries = new AtomicLong(0);

    @Override
    public String inspect(String sql) {
        if (!enableQueryStatistics) {
            return sql;
        }

        // Store query start time
        queryStartTime.set(System.currentTimeMillis());
        currentQuery.set(sql);

        // Record query execution
        totalQueries.incrementAndGet();

        return sql;
    }

    /**
     * Called after query execution to calculate execution time and log if slow
     */
    public void recordQueryExecution(String sql, long executionTimeMs) {
        if (!enableQueryStatistics) {
            return;
        }

        // Update query statistics
        String normalizedSql = normalizeQuery(sql);
        QueryStatistics stats = queryStats.computeIfAbsent(normalizedSql, k -> new QueryStatistics());
        stats.recordExecution(executionTimeMs);

        // Log slow queries
        if (enableSlowQueryLogging && executionTimeMs > slowQueryThresholdMs) {
            slowQueries.incrementAndGet();
            logger.warn("SLOW QUERY DETECTED ({}ms > {}ms threshold): {}",
                executionTimeMs, slowQueryThresholdMs, sql);
        }
    }

    /**
     * Normalize query by removing parameter values for grouping similar queries
     */
    private String normalizeQuery(String sql) {
        if (sql == null) {
            return "UNKNOWN";
        }

        // Remove specific values, keep structure
        return sql
            .replaceAll("'[^']*'", "'?'")              // Replace string literals
            .replaceAll("\\b\\d+\\b", "?")              // Replace numbers
            .replaceAll("\\s+", " ")                    // Normalize whitespace
            .trim()
            .toUpperCase();
    }

    /**
     * Get all query statistics
     */
    public Map<String, QueryStatistics> getQueryStatistics() {
        return new ConcurrentHashMap<>(queryStats);
    }

    /**
     * Get total query count
     */
    public long getTotalQueries() {
        return totalQueries.get();
    }

    /**
     * Get slow query count
     */
    public long getSlowQueries() {
        return slowQueries.get();
    }

    /**
     * Get slow query threshold
     */
    public long getSlowQueryThresholdMs() {
        return slowQueryThresholdMs;
    }

    /**
     * Reset all statistics
     */
    public void resetStatistics() {
        queryStats.clear();
        totalQueries.set(0);
        slowQueries.set(0);
        logger.info("Query performance statistics reset");
    }

    /**
     * Get top N slowest queries
     */
    public Map<String, QueryStatistics> getTopSlowQueries(int limit) {
        return queryStats.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue().getAverageExecutionTime(),
                                            e1.getValue().getAverageExecutionTime()))
            .limit(limit)
            .collect(ConcurrentHashMap::new,
                    (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                    ConcurrentHashMap::putAll);
    }

    /**
     * Get queries exceeding slow threshold
     */
    public Map<String, QueryStatistics> getSlowQueriesAboveThreshold() {
        return queryStats.entrySet().stream()
            .filter(entry -> entry.getValue().getAverageExecutionTime() > slowQueryThresholdMs)
            .collect(ConcurrentHashMap::new,
                    (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                    ConcurrentHashMap::putAll);
    }

    /**
     * Inner class to track statistics for a specific query pattern
     */
    public static class QueryStatistics {
        private final AtomicLong executionCount = new AtomicLong(0);
        private final AtomicLong totalExecutionTime = new AtomicLong(0);
        private volatile long minExecutionTime = Long.MAX_VALUE;
        private volatile long maxExecutionTime = 0;
        private volatile long lastExecutionTime = 0;

        public void recordExecution(long executionTimeMs) {
            executionCount.incrementAndGet();
            totalExecutionTime.addAndGet(executionTimeMs);
            lastExecutionTime = System.currentTimeMillis();

            // Update min/max (not perfectly thread-safe but acceptable for monitoring)
            if (executionTimeMs < minExecutionTime) {
                minExecutionTime = executionTimeMs;
            }
            if (executionTimeMs > maxExecutionTime) {
                maxExecutionTime = executionTimeMs;
            }
        }

        public long getExecutionCount() {
            return executionCount.get();
        }

        public long getTotalExecutionTime() {
            return totalExecutionTime.get();
        }

        public long getAverageExecutionTime() {
            long count = executionCount.get();
            return count > 0 ? totalExecutionTime.get() / count : 0;
        }

        public long getMinExecutionTime() {
            return minExecutionTime == Long.MAX_VALUE ? 0 : minExecutionTime;
        }

        public long getMaxExecutionTime() {
            return maxExecutionTime;
        }

        public long getLastExecutionTime() {
            return lastExecutionTime;
        }
    }
}
