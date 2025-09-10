package com.portfolio.service;

import com.portfolio.adapter.out.persistence.jpa.SourceRepositoryJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.SourceRepositoryJpaRepository;
import com.portfolio.dto.SourceRepositoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SourceRepositoryService {
    
    private final SourceRepositoryJpaRepository sourceRepositoryRepository;
    private final SyncMonitorService syncMonitorService;
    
    public List<SourceRepositoryDto> getAllSourceRepositories() {
        List<SourceRepositoryJpaEntity> entities = sourceRepositoryRepository.findAllOrderByUpdatedAtDesc();
        return entities.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    public Optional<SourceRepositoryDto> getSourceRepository(Long id) {
        return sourceRepositoryRepository.findById(id)
                .map(this::mapToDto);
    }
    
    @Transactional
    public Optional<SourceRepositoryDto> updateRepositoryHomepage(Long id, String homepage) {
        Optional<SourceRepositoryJpaEntity> repositoryOpt = sourceRepositoryRepository.findById(id);
        
        if (repositoryOpt.isEmpty()) {
            return Optional.empty();
        }
        
        SourceRepositoryJpaEntity repository = repositoryOpt.get();
        repository.setHomepage(homepage);
        repository.setUpdatedAt(LocalDateTime.now());
        
        SourceRepositoryJpaEntity updated = sourceRepositoryRepository.save(repository);
        
        syncMonitorService.appendLog("INFO", 
            String.format("Updated homepage for repository '%s': %s", repository.getName(), homepage));
        
        return Optional.of(mapToDto(updated));
    }
    
    @Transactional
    public boolean deleteSourceRepository(Long id) {
        Optional<SourceRepositoryJpaEntity> repositoryOpt = sourceRepositoryRepository.findById(id);
        
        if (repositoryOpt.isEmpty()) {
            return false;
        }
        
        SourceRepositoryJpaEntity repository = repositoryOpt.get();
        String repositoryName = repository.getName();
        
        try {
            syncMonitorService.appendLog("INFO", "Deleting source repository: " + repositoryName + " (ID: " + id + ")");
            
            // Note: Related PortfolioProject entities will be handled separately
            // as they should remain as curated portfolio items even if source is deleted
            
            sourceRepositoryRepository.delete(repository);
            
            syncMonitorService.appendLog("INFO", 
                String.format("Successfully deleted source repository '%s'", repositoryName));
            
            return true;
            
        } catch (Exception e) {
            log.error("Error deleting source repository with ID {}: {}", id, e.getMessage(), e);
            syncMonitorService.appendLog("ERROR", 
                String.format("Failed to delete source repository '%s': %s", repositoryName, e.getMessage()));
            throw new RuntimeException("Failed to delete source repository: " + e.getMessage(), e);
        }
    }
    
    public List<SourceRepositoryDto> getSourceRepositoriesByStatus(SourceRepositoryJpaEntity.SyncStatus status) {
        List<SourceRepositoryJpaEntity> entities = sourceRepositoryRepository.findBySyncStatusOrderByUpdatedAtDesc(status);
        return entities.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    public long countByStatus(SourceRepositoryJpaEntity.SyncStatus status) {
        return sourceRepositoryRepository.countBySyncStatus(status);
    }
    
    @Transactional
    public void resetAllRepositoriesToUnsynced() {
        List<SourceRepositoryJpaEntity> allRepositories = sourceRepositoryRepository.findAll();
        for (SourceRepositoryJpaEntity repository : allRepositories) {
            repository.setSyncStatus(SourceRepositoryJpaEntity.SyncStatus.UNSYNCED);
            repository.setLastSyncAttempt(null);
            repository.setSyncErrorMessage(null);
        }
        sourceRepositoryRepository.saveAll(allRepositories);
        
        syncMonitorService.appendLog("INFO", String.format("Reset %d repositories to UNSYNCED status", allRepositories.size()));
    }
    
    public List<SourceRepositoryDto> getUnsyncedRepositories() {
        return getSourceRepositoriesByStatus(SourceRepositoryJpaEntity.SyncStatus.UNSYNCED);
    }
    
    public Optional<SourceRepositoryJpaEntity> findByGithubId(Long githubId) {
        return sourceRepositoryRepository.findByGithubId(githubId);
    }
    
    public Optional<SourceRepositoryJpaEntity> findByFullName(String fullName) {
        return sourceRepositoryRepository.findByFullName(fullName);
    }
    
    @Transactional
    public SourceRepositoryJpaEntity save(SourceRepositoryJpaEntity entity) {
        return sourceRepositoryRepository.save(entity);
    }
    
    private SourceRepositoryDto mapToDto(SourceRepositoryJpaEntity entity) {
        return SourceRepositoryDto.builder()
                .id(entity.getId())
                .githubId(entity.getGithubId())
                .name(entity.getName())
                .fullName(entity.getFullName())
                .description(entity.getDescription())
                .githubRepoUrl(entity.getGithubRepoUrl())
                .homepage(entity.getHomepage())
                .language(entity.getLanguage())
                .fork(entity.getFork())
                .stargazersCount(entity.getStargazersCount())
                .topics(entity.getTopics())
                .githubCreatedAt(entity.getGithubCreatedAt())
                .githubUpdatedAt(entity.getGithubUpdatedAt())
                .readmeMarkdown(entity.getReadmeMarkdown())
                .syncStatus(entity.getSyncStatus())
                .lastSyncAttempt(entity.getLastSyncAttempt())
                .syncErrorMessage(entity.getSyncErrorMessage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}