package com.portfolio.service;

import com.portfolio.adapter.out.persistence.jpa.SyncConfigJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.SyncConfigJpaRepository;
import com.portfolio.dto.SyncConfigDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncConfigService {
    
    private final SyncConfigJpaRepository syncConfigRepository;
    
    @Transactional(readOnly = true)
    public SyncConfigDto getOrCreate() {
        SyncConfigJpaEntity entity = syncConfigRepository.findFirstByOrderByIdAsc()
                .orElseGet(this::createDefaultConfig);
        
        return mapToDto(entity);
    }
    
    @Transactional
    public SyncConfigDto update(Boolean enabled, Integer intervalHours, String updatedBy) {
        log.info("Updating sync config: enabled={}, intervalHours={}, updatedBy={}", 
                enabled, intervalHours, updatedBy);
        
        SyncConfigJpaEntity entity = syncConfigRepository.findFirstByOrderByIdAsc()
                .orElseGet(this::createDefaultConfig);
        
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
        SyncConfigJpaEntity entity = syncConfigRepository.findFirstByOrderByIdAsc()
                .orElseGet(this::createDefaultConfig);
        
        entity.setLastRunAt(lastRunAt);
        syncConfigRepository.save(entity);
    }
    
    @Transactional
    public void updateNextRun(Instant nextRunAt) {
        SyncConfigJpaEntity entity = syncConfigRepository.findFirstByOrderByIdAsc()
                .orElseGet(this::createDefaultConfig);
        
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
                .build();
        
        return syncConfigRepository.save(defaultConfig);
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
}