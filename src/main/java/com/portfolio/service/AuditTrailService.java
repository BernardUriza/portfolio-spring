package com.portfolio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditTrailService {
    
    private final CorrelationIdService correlationIdService;
    
    /**
     * Audit entity creation
     */
    public CompletableFuture<Void> auditCreate(String entityType, Long entityId, Object entity, String userId) {
        return CompletableFuture.runAsync(correlationIdService.wrapWithCorrelationId(() -> {
            try {
                correlationIdService.logOperation("AUDIT", "INFO", 
                    "AUDIT_CREATE: entity_type={}, entity_id={}, user={}, timestamp={}", 
                    entityType, entityId, userId != null ? userId : "system", LocalDateTime.now());
                
                // Log detailed creation data
                correlationIdService.logOperation("AUDIT", "DEBUG", 
                    "AUDIT_CREATE_DETAILS: entity_type={}, entity_id={}, data={}", 
                    entityType, entityId, sanitizeForLogging(entity));
                         
            } catch (Exception e) {
                correlationIdService.logOperation("AUDIT", "ERROR", 
                    "Failed to audit entity creation: entity_type={}, entity_id={}, error={}", 
                    entityType, entityId, e.getMessage());
            }
        }));
    }
    
    /**
     * Audit entity update with before/after comparison
     */
    public CompletableFuture<Void> auditUpdate(String entityType, Long entityId, 
                                              Object beforeEntity, Object afterEntity, 
                                              String userId, String operation) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("AUDIT_UPDATE: entity_type={}, entity_id={}, user={}, operation={}, timestamp={}", 
                        entityType, entityId, userId != null ? userId : "system", operation, LocalDateTime.now());
                
                // Log what changed (simplified comparison for now)
                log.debug("AUDIT_UPDATE_DETAILS: entity_type={}, entity_id={}, before={}, after={}", 
                         entityType, entityId, 
                         sanitizeForLogging(beforeEntity), 
                         sanitizeForLogging(afterEntity));
                         
            } catch (Exception e) {
                log.error("Failed to audit entity update: entity_type={}, entity_id={}", 
                         entityType, entityId, e);
            }
        });
    }
    
    /**
     * Audit entity deletion
     */
    public CompletableFuture<Void> auditDelete(String entityType, Long entityId, Object entity, String userId) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("AUDIT_DELETE: entity_type={}, entity_id={}, user={}, timestamp={}", 
                        entityType, entityId, userId != null ? userId : "system", LocalDateTime.now());
                
                // Log what was deleted
                log.debug("AUDIT_DELETE_DETAILS: entity_type={}, entity_id={}, deleted_data={}", 
                         entityType, entityId, sanitizeForLogging(entity));
                         
            } catch (Exception e) {
                log.error("Failed to audit entity deletion: entity_type={}, entity_id={}", 
                         entityType, entityId, e);
            }
        });
    }
    
    /**
     * Audit sensitive operations like AI curation, sync, etc.
     */
    public CompletableFuture<Void> auditOperation(String operation, Map<String, Object> metadata, String userId) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("AUDIT_OPERATION: operation={}, user={}, timestamp={}", 
                        operation, userId != null ? userId : "system", LocalDateTime.now());
                
                // Log operation metadata
                if (metadata != null && !metadata.isEmpty()) {
                    log.debug("AUDIT_OPERATION_DETAILS: operation={}, metadata={}", 
                             operation, sanitizeForLogging(metadata));
                }
                         
            } catch (Exception e) {
                log.error("Failed to audit operation: operation={}", operation, e);
            }
        });
    }
    
    /**
     * Audit failed operations for security monitoring
     */
    public CompletableFuture<Void> auditFailure(String operation, String reason, String userId, String ipAddress) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.warn("AUDIT_FAILURE: operation={}, user={}, reason={}, ip={}, timestamp={}", 
                        operation, userId != null ? userId : "anonymous", reason, 
                        ipAddress != null ? ipAddress : "unknown", LocalDateTime.now());
                         
            } catch (Exception e) {
                log.error("Failed to audit operation failure: operation={}", operation, e);
            }
        });
    }
    
    /**
     * Audit optimistic locking conflicts
     */
    public CompletableFuture<Void> auditOptimisticLockFailure(String entityType, Long entityId, 
                                                            Long expectedVersion, Long actualVersion, 
                                                            String userId, String operation) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.warn("AUDIT_OPTIMISTIC_LOCK_FAILURE: entity_type={}, entity_id={}, " +
                        "expected_version={}, actual_version={}, user={}, operation={}, timestamp={}", 
                        entityType, entityId, expectedVersion, actualVersion, 
                        userId != null ? userId : "system", operation, LocalDateTime.now());
                         
            } catch (Exception e) {
                log.error("Failed to audit optimistic lock failure: entity_type={}, entity_id={}", 
                         entityType, entityId, e);
            }
        });
    }
    
    /**
     * Audit admin operations with elevated privileges
     */
    public CompletableFuture<Void> auditAdminOperation(String operation, String description, 
                                                      String userId, String ipAddress, 
                                                      Map<String, Object> metadata) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.warn("AUDIT_ADMIN_OPERATION: operation={}, description={}, user={}, ip={}, timestamp={}", 
                        operation, description, userId != null ? userId : "system", 
                        ipAddress != null ? ipAddress : "unknown", LocalDateTime.now());
                
                if (metadata != null && !metadata.isEmpty()) {
                    log.info("AUDIT_ADMIN_DETAILS: operation={}, metadata={}", 
                            operation, sanitizeForLogging(metadata));
                }
                         
            } catch (Exception e) {
                log.error("Failed to audit admin operation: operation={}", operation, e);
            }
        });
    }
    
    /**
     * Audit security-sensitive operations
     */
    public CompletableFuture<Void> auditSecurityEvent(String eventType, String description, 
                                                     String userId, String ipAddress, 
                                                     String userAgent, boolean successful) {
        return CompletableFuture.runAsync(() -> {
            try {
                String logLevel = successful ? "INFO" : "WARN";
                String message = String.format("AUDIT_SECURITY_%s: event={}, description={}, user={}, " +
                        "ip={}, user_agent={}, successful={}, timestamp={}", logLevel);
                
                if (successful) {
                    log.info(message, eventType, description, userId != null ? userId : "anonymous", 
                            ipAddress != null ? ipAddress : "unknown", 
                            userAgent != null ? userAgent : "unknown", successful, LocalDateTime.now());
                } else {
                    log.warn(message, eventType, description, userId != null ? userId : "anonymous", 
                            ipAddress != null ? ipAddress : "unknown", 
                            userAgent != null ? userAgent : "unknown", successful, LocalDateTime.now());
                }
                         
            } catch (Exception e) {
                log.error("Failed to audit security event: event_type={}", eventType, e);
            }
        });
    }
    
    /**
     * Sanitize objects for logging (remove sensitive data)
     */
    private Object sanitizeForLogging(Object obj) {
        if (obj == null) {
            return null;
        }
        
        // For now, just return string representation with length limit
        String str = obj.toString();
        if (str.length() > 1000) {
            return str.substring(0, 1000) + "... [truncated]";
        }
        return str;
    }
}