package com.portfolio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import jakarta.persistence.OptimisticLockException;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class OptimisticLockingService {
    
    private final AuditTrailService auditTrailService;
    
    /**
     * Execute an operation with optimistic locking retry logic
     */
    public <T> T executeWithRetry(Supplier<T> operation, String operationName, 
                                  String entityType, Long entityId, String userId) {
        return executeWithRetry(operation, operationName, entityType, entityId, userId, 3);
    }
    
    /**
     * Execute an operation with configurable retry attempts
     */
    public <T> T executeWithRetry(Supplier<T> operation, String operationName, 
                                  String entityType, Long entityId, String userId, 
                                  int maxRetries) {
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < maxRetries) {
            try {
                attempt++;
                T result = operation.get();
                
                if (attempt > 1) {
                    log.info("Optimistic locking retry succeeded: operation={}, entity_type={}, " +
                            "entity_id={}, attempt={}", operationName, entityType, entityId, attempt);
                }
                
                return result;
                
            } catch (ObjectOptimisticLockingFailureException | 
                     OptimisticLockException e) {
                
                lastException = e;
                
                log.warn("Optimistic locking failure: operation={}, entity_type={}, entity_id={}, " +
                        "attempt={}/{}, error={}", operationName, entityType, entityId, 
                        attempt, maxRetries, e.getMessage());
                
                // Audit the optimistic lock failure
                auditTrailService.auditOptimisticLockFailure(entityType, entityId, 
                    null, null, userId, operationName);
                
                if (attempt >= maxRetries) {
                    break;
                }
                
                // Progressive backoff
                try {
                    Thread.sleep(attempt * 100L); // 100ms, 200ms, 300ms, etc.
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during optimistic locking retry", ie);
                }
            }
        }
        
        // If we get here, all retries failed
        log.error("Optimistic locking failed after {} attempts: operation={}, entity_type={}, entity_id={}", 
                 maxRetries, operationName, entityType, entityId);
        
        throw new OptimisticLockingFailureException(
            String.format("Failed to complete operation '%s' on %s[%d] after %d attempts due to concurrent modifications", 
                         operationName, entityType, entityId, maxRetries), lastException);
    }
    
    /**
     * Handle optimistic locking exceptions in a consistent way
     */
    public RuntimeException handleOptimisticLockingFailure(Exception e, String operationName, 
                                                          String entityType, Long entityId, 
                                                          String userId) {
        log.error("Optimistic locking failure in operation '{}' on {}[{}]: {}", 
                 operationName, entityType, entityId, e.getMessage());
        
        // Audit the failure
        auditTrailService.auditOptimisticLockFailure(entityType, entityId, 
            null, null, userId, operationName);
        
        return new OptimisticLockingFailureException(
            String.format("The %s was modified by another user. Please refresh and try again.", 
                         entityType.toLowerCase()), e);
    }
    
    /**
     * Create a user-friendly message for optimistic locking failures
     */
    public String createUserFriendlyMessage(String entityType, String operation) {
        return String.format("The %s was modified by another user while you were making changes. " +
                           "Your %s could not be completed. Please refresh the page and try again.", 
                           entityType.toLowerCase(), operation.toLowerCase());
    }
    
    /**
     * Check if an exception is an optimistic locking failure
     */
    public boolean isOptimisticLockingFailure(Throwable throwable) {
        return throwable instanceof OptimisticLockingFailureException ||
               throwable instanceof OptimisticLockException ||
               (throwable.getCause() != null && isOptimisticLockingFailure(throwable.getCause()));
    }
    
    /**
     * Get version information from an optimistic locking exception
     */
    public VersionConflictInfo extractVersionConflict(Exception e) {
        if (e instanceof ObjectOptimisticLockingFailureException) {
            ObjectOptimisticLockingFailureException ole = (ObjectOptimisticLockingFailureException) e;
            Object id = ole.getIdentifier();
            return new VersionConflictInfo(
                ole.getPersistentClassName(),
                id != null ? id.toString() : null,
                null, // Expected version not easily extractable
                null  // Actual version not easily extractable
            );
        }
        
        return new VersionConflictInfo(null, null, null, null);
    }
    
    /**
     * Information about version conflicts
     */
    public static class VersionConflictInfo {
        private final String entityType;
        private final String entityId;
        private final Long expectedVersion;
        private final Long actualVersion;
        
        public VersionConflictInfo(String entityType, String entityId, 
                                  Long expectedVersion, Long actualVersion) {
            this.entityType = entityType;
            this.entityId = entityId;
            this.expectedVersion = expectedVersion;
            this.actualVersion = actualVersion;
        }
        
        public String getEntityType() { return entityType; }
        public String getEntityId() { return entityId; }
        public Long getExpectedVersion() { return expectedVersion; }
        public Long getActualVersion() { return actualVersion; }
        
        @Override
        public String toString() {
            return String.format("VersionConflictInfo{entityType='%s', entityId='%s', " +
                               "expectedVersion=%d, actualVersion=%d}", 
                               entityType, entityId, expectedVersion, actualVersion);
        }
    }
}
