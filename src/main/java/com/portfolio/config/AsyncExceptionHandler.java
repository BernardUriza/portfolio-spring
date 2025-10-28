package com.portfolio.config;

import com.portfolio.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Executor;

/**
 * Global exception handler for @Async methods
 *
 * Prevents silent failures in asynchronous operations by:
 * - Logging uncaught exceptions with full context
 * - Sending critical alerts via AlertService
 * - Providing custom thread pool configuration
 *
 * Created as part of ASYNC-001: Add Async Exception Handling
 * Aligns with Catalytic Architecture principle of Transparency
 *
 * @author Bernard Uriza Orozco
 * @since 2025-10-28
 */
@Configuration
@EnableAsync
public class AsyncExceptionHandler implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(AsyncExceptionHandler.class);

    @Autowired
    private AlertService alertService;

    /**
     * Configure custom async exception handler
     * Replaces default behavior (silent failure) with logging and alerting
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler(alertService);
    }

    /**
     * Configure thread pool for @Async methods
     * Ensures proper thread naming for debugging
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * Custom exception handler for async operations
     * Logs full context and sends critical alerts
     */
    private static class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(CustomAsyncExceptionHandler.class);
        private final AlertService alertService;

        public CustomAsyncExceptionHandler(AlertService alertService) {
            this.alertService = alertService;
        }

        @Override
        public void handleUncaughtException(Throwable throwable, Method method, Object... params) {
            // Build detailed context for logging
            String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
            String parameters = params.length > 0
                ? Arrays.toString(params)
                : "no parameters";

            // Log with full stack trace
            log.error(
                "Uncaught exception in @Async method: {} - Parameters: {} - Exception: {}",
                methodName,
                parameters,
                throwable.getMessage(),
                throwable
            );

            // Send critical alert for monitoring
            try {
                String alertMessage = String.format(
                    "Async operation failed: %s - %s",
                    methodName,
                    throwable.getMessage()
                );
                alertService.sendCriticalAlert(
                    "ASYNC_OPERATION_FAILED",
                    alertMessage
                );
            } catch (Exception e) {
                // Don't let alert failures cascade
                log.error("Failed to send async exception alert", e);
            }

            // Additional handling based on exception type
            if (throwable instanceof OutOfMemoryError) {
                log.error("CRITICAL: OutOfMemoryError in async operation - immediate attention required");
                // Could trigger emergency shutdown or health check failure
            }
        }
    }
}
