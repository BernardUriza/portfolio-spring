package com.portfolio.service;

import com.portfolio.adapter.out.persistence.jpa.SyncConfigJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.SyncConfigJpaRepository;
import com.portfolio.dto.SyncConfigDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncConfigService {
    
    private final SyncConfigJpaRepository syncConfigRepository;
    @PersistenceContext
    private EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;
    
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
        SyncConfigJpaEntity defaultConfig = SyncConfigJpaEntity.builder()
                .enabled(false)
                .intervalHours(6)
                .updatedBy("system")
                .updatedAt(Instant.now())
                .singletonKey("X")
                .build();
        return syncConfigRepository.saveAndFlush(defaultConfig);
    }
    
    private SyncConfigDto mapToDto(SyncConfigJpaEntity entity) {
        return SyncConfigDto.builder()
                .enabled(entity.getEnabled())
                .intervalHours(entity.getIntervalHours())
                .lastRunAt(entity.getLastRunAt())
                .nextRunAt(entity.getNextRunAt())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
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
