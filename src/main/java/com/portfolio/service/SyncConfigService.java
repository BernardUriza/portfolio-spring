package com.portfolio.service;

import com.portfolio.adapter.out.persistence.jpa.SyncConfigJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.SyncConfigJpaRepository;
import com.portfolio.dto.SyncConfigDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

@Service
public class SyncConfigService {
    private static final Logger log = LoggerFactory.getLogger(SyncConfigService.class);

    private final SyncConfigJpaRepository syncConfigRepository;
    private final TransactionTemplate transactionTemplate;

    public SyncConfigService(SyncConfigJpaRepository syncConfigRepository,
                             TransactionTemplate transactionTemplate) {
        this.syncConfigRepository = syncConfigRepository;
        this.transactionTemplate = transactionTemplate;
    }
    
    public SyncConfigDto getOrCreate() {
        return mapToDto(getOrCreateEntity());
    }
    
    @Transactional
    public SyncConfigDto update(Boolean enabled, Integer intervalHours, String updatedBy) {
        log.info("Updating sync config: enabled={}, intervalHours={}, updatedBy={}", 
                enabled, intervalHours, updatedBy);
        
        SyncConfigJpaEntity entity = getOrCreateEntity();
        
        entity.setEnabled(enabled);
        entity.setIntervalHours(intervalHours);
        entity.setUpdatedBy(updatedBy != null ? updatedBy : "admin");
        entity.setUpdatedAt(Instant.now());
        
        // Clear next run time if disabled, will be recalculated by scheduler
        if (!enabled) {
            entity.setNextRunAt(null);
        }
        
        entity = syncConfigRepository.save(entity);
        log.debug("Sync config updated successfully with ID: {}", entity.getId());
        
        return mapToDto(entity);
    }
    
    @Transactional
    public void updateLastRun(Instant lastRunAt) {
        SyncConfigJpaEntity entity = getOrCreateEntity();
        
        entity.setLastRunAt(lastRunAt);
        syncConfigRepository.save(entity);
    }
    
    @Transactional
    public void updateNextRun(Instant nextRunAt) {
        SyncConfigJpaEntity entity = getOrCreateEntity();
        
        entity.setNextRunAt(nextRunAt);
        syncConfigRepository.save(entity);
    }
    
    private SyncConfigJpaEntity createDefaultConfig() {
        log.info("Creating default sync configuration");
        SyncConfigJpaEntity defaultConfig = new SyncConfigJpaEntity();
        defaultConfig.setEnabled(false);
        defaultConfig.setIntervalHours(6);
        defaultConfig.setUpdatedBy("system");
        defaultConfig.setUpdatedAt(Instant.now());
        defaultConfig.setSingletonKey("X");
        return syncConfigRepository.saveAndFlush(defaultConfig);
    }
    
    private SyncConfigDto mapToDto(SyncConfigJpaEntity entity) {
        return SyncConfigDto.of(entity);
    }

    public SyncConfigJpaEntity getOrCreateEntity() {
        // First, try to lock the singleton row inside a transaction
        SyncConfigJpaEntity locked = transactionTemplate.execute(status ->
                syncConfigRepository.lockSingleton().orElse(null)
        );
        if (locked != null) return locked;

        // Not found: try to create in a new transaction
        SyncConfigJpaEntity created = tryCreateDefaultInNewTransaction();
        if (created != null) return created;

        // Collision: fetch existing in a transaction
        return transactionTemplate.execute(status ->
                syncConfigRepository.findBySingletonKey("X")
                        .orElseGet(() -> syncConfigRepository.findFirstByOrderByIdAsc()
                                .orElseThrow(() -> new IllegalStateException("sync_config singleton not found after constraint collision")))
        );
    }

    private SyncConfigJpaEntity tryCreateDefaultInNewTransaction() {
        try {
            return transactionTemplate.execute(status -> {
                try {
                    return createDefaultConfig();
                } catch (DataIntegrityViolationException e) {
                    status.setRollbackOnly();
                    return null;
                }
            });
        } catch (Exception e) {
            return null;
        }
    }
}
