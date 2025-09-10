package com.portfolio.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Slf4j
@Service
public class CorrelationIdService {
    
    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String REQUEST_ID_KEY = "requestId";
    
    /**
     * Get current correlation ID from MDC
     */
    public String getCurrentCorrelationId() {
        return MDC.get(CORRELATION_ID_KEY);
    }
    
    /**
     * Get current request ID from MDC
     */
    public String getCurrentRequestId() {
        return MDC.get(REQUEST_ID_KEY);
    }
    
    /**
     * Set correlation ID in MDC
     */
    public void setCorrelationId(String correlationId) {
        if (correlationId != null) {
            MDC.put(CORRELATION_ID_KEY, correlationId);
        }
    }
    
    /**
     * Set request ID in MDC
     */
    public void setRequestId(String requestId) {
        if (requestId != null) {
            MDC.put(REQUEST_ID_KEY, requestId);
        }
    }
    
    /**
     * Generate a new correlation ID
     */
    public String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Generate a new request ID (shorter than correlation ID)
     */
    public String generateRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Execute a task with correlation ID propagation
     */
    public <T> T executeWithCorrelationId(String correlationId, Supplier<T> task) {
        String previousCorrelationId = getCurrentCorrelationId();
        try {
            setCorrelationId(correlationId);
            return task.get();
        } finally {
            if (previousCorrelationId != null) {
                setCorrelationId(previousCorrelationId);
            } else {
                MDC.remove(CORRELATION_ID_KEY);
            }
        }
    }
    
    /**
     * Execute a task with both correlation ID and request ID propagation
     */
    public <T> T executeWithCorrelationContext(String correlationId, String requestId, Supplier<T> task) {
        String previousCorrelationId = getCurrentCorrelationId();
        String previousRequestId = getCurrentRequestId();
        try {
            setCorrelationId(correlationId);
            setRequestId(requestId);
            return task.get();
        } finally {
            if (previousCorrelationId != null) {
                setCorrelationId(previousCorrelationId);
            } else {
                MDC.remove(CORRELATION_ID_KEY);
            }
            
            if (previousRequestId != null) {
                setRequestId(previousRequestId);
            } else {
                MDC.remove(REQUEST_ID_KEY);
            }
        }
    }
    
    /**
     * Execute async task with correlation ID propagation
     */
    public <T> CompletableFuture<T> executeAsyncWithCorrelationId(Supplier<T> task, Executor executor) {
        String correlationId = getCurrentCorrelationId();
        String requestId = getCurrentRequestId();
        
        return CompletableFuture.supplyAsync(() -> {
            return executeWithCorrelationContext(correlationId, requestId, task);
        }, executor);
    }
    
    /**
     * Wrap Runnable with correlation ID propagation
     */
    public Runnable wrapWithCorrelationId(Runnable task) {
        String correlationId = getCurrentCorrelationId();
        String requestId = getCurrentRequestId();
        
        return () -> {
            executeWithCorrelationContext(correlationId, requestId, () -> {
                task.run();
                return null;
            });
        };
    }
    
    /**
     * Wrap Supplier with correlation ID propagation
     */
    public <T> Supplier<T> wrapWithCorrelationId(Supplier<T> task) {
        String correlationId = getCurrentCorrelationId();
        String requestId = getCurrentRequestId();
        
        return () -> executeWithCorrelationContext(correlationId, requestId, task);
    }
    
    /**
     * Log an operation with correlation context
     */
    public void logOperation(String operation, String level, String message, Object... args) {
        String correlationId = getCurrentCorrelationId();
        String requestId = getCurrentRequestId();
        
        String formattedMessage = String.format("OPERATION[%s]: %s - correlation_id=%s, request_id=%s", 
                operation, message, correlationId, requestId);
        
        switch (level.toUpperCase()) {
            case "DEBUG":
                log.debug(formattedMessage, args);
                break;
            case "INFO":
                log.info(formattedMessage, args);
                break;
            case "WARN":
                log.warn(formattedMessage, args);
                break;
            case "ERROR":
                log.error(formattedMessage, args);
                break;
            default:
                log.info(formattedMessage, args);
        }
    }
    
    /**
     * Create a correlation context object for manual propagation
     */
    public CorrelationContext captureContext() {
        return new CorrelationContext(getCurrentCorrelationId(), getCurrentRequestId());
    }
    
    /**
     * Restore correlation context from captured context
     */
    public void restoreContext(CorrelationContext context) {
        if (context != null) {
            setCorrelationId(context.getCorrelationId());
            setRequestId(context.getRequestId());
        }
    }
    
    /**
     * Clear all correlation context
     */
    public void clearContext() {
        MDC.remove(CORRELATION_ID_KEY);
        MDC.remove(REQUEST_ID_KEY);
    }
    
    /**
     * Correlation context holder
     */
    public static class CorrelationContext {
        private final String correlationId;
        private final String requestId;
        
        public CorrelationContext(String correlationId, String requestId) {
            this.correlationId = correlationId;
            this.requestId = requestId;
        }
        
        public String getCorrelationId() { return correlationId; }
        public String getRequestId() { return requestId; }
        
        @Override
        public String toString() {
            return String.format("CorrelationContext{correlationId='%s', requestId='%s'}", 
                                correlationId, requestId);
        }
    }
}