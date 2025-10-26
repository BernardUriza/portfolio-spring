package com.portfolio.config.monitoring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * AOP Aspect for monitoring repository method execution times.
 * Works in conjunction with QueryPerformanceInterceptor to track slow queries.
 *
 * Created by Bernard Uriza Orozco for PERF-005
 */
@Aspect
@Component
public class QueryPerformanceAspect {

    private static final Logger logger = LoggerFactory.getLogger(QueryPerformanceAspect.class);

    @Autowired
    private QueryPerformanceInterceptor queryPerformanceInterceptor;

    @Value("${portfolio.query.slow-query-threshold-ms:100}")
    private long slowQueryThresholdMs;

    @Value("${portfolio.query.enable-slow-query-logging:true}")
    private boolean enableSlowQueryLogging;

    /**
     * Intercept all repository method calls to measure execution time
     */
    @Around("execution(* com.portfolio.repository..*(..))")
    public Object monitorRepositoryExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!enableSlowQueryLogging) {
            return joinPoint.proceed();
        }

        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            // Record execution time
            queryPerformanceInterceptor.recordQueryExecution(methodName, executionTime);

            // Log slow repository calls
            if (executionTime > slowQueryThresholdMs) {
                logger.warn("SLOW REPOSITORY METHOD ({}ms > {}ms threshold): {}",
                    executionTime, slowQueryThresholdMs, methodName);
            } else if (logger.isDebugEnabled()) {
                logger.debug("Repository method executed in {}ms: {}", executionTime, methodName);
            }

            return result;
        } catch (Throwable ex) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Repository method failed after {}ms: {} - Error: {}",
                executionTime, methodName, ex.getMessage());
            throw ex;
        }
    }

    /**
     * Intercept service methods that might trigger N+1 queries
     */
    @Around("execution(* com.portfolio.service..*(..)) && " +
            "(@annotation(org.springframework.transaction.annotation.Transactional) || " +
            "@within(org.springframework.transaction.annotation.Transactional))")
    public Object monitorTransactionalMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!enableSlowQueryLogging) {
            return joinPoint.proceed();
        }

        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();
        long initialQueryCount = queryPerformanceInterceptor.getTotalQueries();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            long queriesExecuted = queryPerformanceInterceptor.getTotalQueries() - initialQueryCount;

            // Warn if many queries executed (potential N+1)
            if (queriesExecuted > 10) {
                logger.warn("POTENTIAL N+1 QUERY ISSUE: {} executed {} queries in {}ms",
                    methodName, queriesExecuted, executionTime);
            } else if (logger.isDebugEnabled()) {
                logger.debug("Transactional method {} executed {} queries in {}ms",
                    methodName, queriesExecuted, executionTime);
            }

            return result;
        } catch (Throwable ex) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Transactional method failed after {}ms: {} - Error: {}",
                executionTime, methodName, ex.getMessage());
            throw ex;
        }
    }
}
