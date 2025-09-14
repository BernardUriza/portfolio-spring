package com.portfolio.service;

import com.portfolio.adapter.out.persistence.jpa.*;
import com.portfolio.core.application.usecase.FactoryResetUseCase;
import com.portfolio.repository.ContactMessageRepository;
import com.portfolio.repository.VisitorInsightRepository;
import com.portfolio.core.domain.admin.ResetAudit;
import com.portfolio.core.domain.admin.ResetStatus;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryResetService implements FactoryResetUseCase {
    
    private final ResetAuditJpaRepository resetAuditRepository;
    private final ResetAuditJpaMapper resetAuditMapper;
    private final EntityManager entityManager;
    
    // All the repositories that need to be cleared
    private final PortfolioProjectJpaRepository portfolioProjectRepository;
    private final SkillJpaRepository skillRepository;
    private final ExperienceJpaRepository experienceRepository;
    private final SourceRepositoryJpaRepository sourceRepositoryRepository;
    // Contact/Visitor features repositories
    private final ContactMessageRepository contactMessageRepository;
    private final VisitorInsightRepository visitorInsightRepository;
    
    @Value("${spring.jpa.database-platform:}")
    private String databasePlatform;
    
    @Value("${spring.datasource.url:}")
    private String datasourceUrl;
    
    // SSE emitters for streaming progress
    private final Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);
    
    private boolean isPostgres;
    private boolean isH2;
    
    @PostConstruct
    public void init() {
        // Detect database type
        isPostgres = databasePlatform.toLowerCase().contains("postgres") || 
                     datasourceUrl.toLowerCase().contains("postgres");
        isH2 = databasePlatform.toLowerCase().contains("h2") || 
               datasourceUrl.toLowerCase().contains("h2");
        
        log.info("Factory Reset Service initialized - Database type: {} (Postgres: {}, H2: {})", 
                 databasePlatform, isPostgres, isH2);
    }
    
    @Override
    @Transactional
    public ResetAudit startFactoryReset(String startedBy, String ipAddress) {
        // Check for active jobs
        List<ResetAuditJpaEntity> activeJobs = resetAuditRepository.findActiveJobs();
        if (!activeJobs.isEmpty()) {
            ResetAudit activeJob = resetAuditMapper.toDomain(activeJobs.get(0));
            throw new IllegalStateException("Factory reset already in progress with job ID: " + activeJob.getJobId());
        }
        
        // Create new reset audit
        String jobId = UUID.randomUUID().toString();
        ResetAudit resetAudit = ResetAudit.start(jobId, startedBy, ipAddress);
        
        // Save audit record
        ResetAuditJpaEntity auditEntity = resetAuditMapper.toJpaEntity(resetAudit);
        auditEntity = resetAuditRepository.save(auditEntity);
        
        // Start async reset process
        performFactoryResetAsync(jobId);
        
        return resetAuditMapper.toDomain(auditEntity);
    }
    
    @Override
    public List<ResetAudit> getActiveJobs() {
        return resetAuditRepository.findActiveJobs()
                .stream()
                .map(resetAuditMapper::toDomain)
                .toList();
    }
    
    @Override
    public ResetAudit getResetAuditByJobId(String jobId) {
        return resetAuditRepository.findByJobId(jobId)
                .map(resetAuditMapper::toDomain)
                .orElse(null);
    }
    
    @Override
    public List<ResetAudit> getResetHistory(int limit) {
        PageRequest pageRequest = PageRequest.of(0, Math.max(1, Math.min(100, limit)));
        return resetAuditRepository.findAllOrderByStartedAtDesc(pageRequest)
                .stream()
                .map(resetAuditMapper::toDomain)
                .toList();
    }
    
    public SseEmitter streamResetProgress(String jobId) {
        SseEmitter emitter = new SseEmitter(300000L); // 5 minutes timeout
        sseEmitters.put(jobId, emitter);
        
        emitter.onCompletion(() -> sseEmitters.remove(jobId));
        emitter.onTimeout(() -> sseEmitters.remove(jobId));
        emitter.onError((throwable) -> sseEmitters.remove(jobId));
        
        return emitter;
    }
    
    @Async
    public void performFactoryResetAsync(String jobId) {
        try {
            log.info("Starting factory reset process for job: {}", jobId);
            sendSseMessage(jobId, "STARTED", "Factory reset process started");
            
            int tablesCleared = performDatabaseReset(jobId);
            
            // Update audit record as completed
            updateAuditRecord(jobId, audit -> audit.complete(tablesCleared));
            
            sendSseMessage(jobId, "COMPLETED", 
                String.format("Factory reset completed successfully. %d tables cleared.", tablesCleared));
            
            log.info("Factory reset completed successfully for job: {}", jobId);
            
        } catch (Exception e) {
            log.error("Factory reset failed for job: {}", jobId, e);
            
            // Update audit record as failed
            updateAuditRecord(jobId, audit -> audit.fail(e.getMessage()));
            
            sendSseMessage(jobId, "ERROR", "Factory reset failed: " + e.getMessage());
        } finally {
            // Clean up SSE emitter
            SseEmitter emitter = sseEmitters.remove(jobId);
            if (emitter != null) {
                emitter.complete();
            }
        }
    }
    
    private int performDatabaseReset(String jobId) {
        if (isPostgres) {
            return performPostgresReset(jobId);
        } else if (isH2) {
            return performH2Reset(jobId);
        } else {
            throw new UnsupportedOperationException("Unsupported database platform: " + databasePlatform);
        }
    }
    
    @Transactional
    protected int performPostgresReset(String jobId) {
        try {
            sendSseMessage(jobId, "STEP", "Fetching table list from database schema");
            
            // Get all tables from public schema (excluding reset_audit)
            Query tableQuery = entityManager.createNativeQuery(
                "SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND tablename != 'reset_audit'"
            );
            
            @SuppressWarnings("unchecked")
            List<String> tableNames = tableQuery.getResultList();
            
            if (tableNames.isEmpty()) {
                log.warn("No tables found to reset for job: {}", jobId);
                return 0;
            }
            
            sendSseMessage(jobId, "STEP", 
                String.format("Found %d tables to reset: %s", tableNames.size(), String.join(", ", tableNames)));
            
            // Build TRUNCATE statement
            String truncateStatement = "TRUNCATE " + String.join(", ", tableNames) + " RESTART IDENTITY CASCADE";
            
            sendSseMessage(jobId, "STEP", "Executing TRUNCATE with RESTART IDENTITY CASCADE");
            log.info("Executing: {}", truncateStatement);
            
            // Execute truncate
            Query truncateQuery = entityManager.createNativeQuery(truncateStatement);
            truncateQuery.executeUpdate();
            
            sendSseMessage(jobId, "STEP", "Database reset completed successfully");
            
            return tableNames.size();
            
        } catch (Exception e) {
            log.error("Failed to perform Postgres reset for job: {}", jobId, e);
            throw new RuntimeException("Postgres reset failed: " + e.getMessage(), e);
        }
    }
    
    @Transactional
    protected int performH2Reset(String jobId) {
        try {
            sendSseMessage(jobId, "STEP", "Clearing H2 database using repository methods");
            
            int tablesCleared = 0;
            
            // Clear tables in dependency order (children first)
            // Visitor insights depend on contact messages via optional FK, so clear insights first
            sendSseMessage(jobId, "STEP", "Clearing visitor insights");
            visitorInsightRepository.deleteAllInBatch();
            tablesCleared++;
            
            sendSseMessage(jobId, "STEP", "Clearing contact messages");
            contactMessageRepository.deleteAllInBatch();
            tablesCleared++;
            
            sendSseMessage(jobId, "STEP", "Clearing portfolio projects");
            portfolioProjectRepository.deleteAllInBatch();
            tablesCleared++;
            
            sendSseMessage(jobId, "STEP", "Clearing source repositories");
            sourceRepositoryRepository.deleteAllInBatch();
            tablesCleared++;
            
            sendSseMessage(jobId, "STEP", "Clearing experiences");
            experienceRepository.deleteAllInBatch();
            tablesCleared++;
            
            sendSseMessage(jobId, "STEP", "Clearing skills");
            skillRepository.deleteAllInBatch();
            tablesCleared++;
            
            // Reset H2 sequences if possible
            try {
                sendSseMessage(jobId, "STEP", "Resetting H2 sequences");
                resetH2Sequences(jobId);
            } catch (Exception e) {
                log.warn("Failed to reset H2 sequences for job: {}, continuing anyway", jobId, e);
                sendSseMessage(jobId, "STEP", "Warning: Could not reset sequences, but tables cleared successfully");
            }
            
            sendSseMessage(jobId, "STEP", "H2 database reset completed successfully");
            
            return tablesCleared;
            
        } catch (Exception e) {
            log.error("Failed to perform H2 reset for job: {}", jobId, e);
            throw new RuntimeException("H2 reset failed: " + e.getMessage(), e);
        }
    }
    
    private void resetH2Sequences(String jobId) {
        try {
            // Reset identity columns to start from 1
            String[] resetStatements = {
                "ALTER TABLE portfolio_projects ALTER COLUMN id RESTART WITH 1",
                "ALTER TABLE source_repositories ALTER COLUMN id RESTART WITH 1",
                "ALTER TABLE skills ALTER COLUMN id RESTART WITH 1", 
                "ALTER TABLE experiences ALTER COLUMN id RESTART WITH 1",
                "ALTER TABLE contact_messages ALTER COLUMN id RESTART WITH 1",
                "ALTER TABLE visitor_insights ALTER COLUMN id RESTART WITH 1"
            };
            
            for (String statement : resetStatements) {
                try {
                    entityManager.createNativeQuery(statement).executeUpdate();
                } catch (Exception e) {
                    log.debug("Could not execute: {} (table may not exist)", statement);
                }
            }
            
        } catch (Exception e) {
            log.warn("Failed to reset H2 sequences: {}", e.getMessage());
        }
    }
    
    private void sendSseMessage(String jobId, String type, String message) {
        SseEmitter emitter = sseEmitters.get(jobId);
        if (emitter != null) {
            try {
                String eventData = String.format("{\"type\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}", 
                    type, message, java.time.Instant.now().toString());
                emitter.send(SseEmitter.event().name("reset-progress").data(eventData));
                log.debug("Sent SSE message for job {}: {} - {}", jobId, type, message);
            } catch (Exception e) {
                log.warn("Failed to send SSE message for job: {}", jobId, e);
                sseEmitters.remove(jobId);
            }
        }
    }
    
    @Transactional
    protected void updateAuditRecord(String jobId, java.util.function.Function<ResetAudit, ResetAudit> updateFunction) {
        resetAuditRepository.findByJobId(jobId)
            .ifPresent(auditEntity -> {
                ResetAudit currentAudit = resetAuditMapper.toDomain(auditEntity);
                ResetAudit updatedAudit = updateFunction.apply(currentAudit);
                ResetAuditJpaEntity updatedEntity = resetAuditMapper.toJpaEntity(updatedAudit);
                updatedEntity.setId(auditEntity.getId()); // Preserve JPA ID
                resetAuditRepository.save(updatedEntity);
            });
    }
}
