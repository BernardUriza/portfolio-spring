package com.portfolio.controller;

import com.portfolio.adapter.out.persistence.jpa.SourceRepositoryJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.SourceRepositoryJpaRepository;
import com.portfolio.service.GitHubSourceRepositoryService;
import com.portfolio.service.SyncSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/source-repositories")
@RequiredArgsConstructor
@Slf4j
public class SourceRepositoryAdminController {
    
    private final SourceRepositoryJpaRepository sourceRepositoryRepository;
    private final GitHubSourceRepositoryService gitHubSourceRepositoryService;
    private final SyncSchedulerService syncSchedulerService;
    
    /**
     * Get all source repositories with pagination and filtering
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSourceRepositories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String syncStatus,
            @RequestParam(required = false) String language) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? 
            Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        
        Page<SourceRepositoryJpaEntity> sourcePage;
        
        if (syncStatus != null && language != null) {
            SourceRepositoryJpaEntity.SyncStatus status = SourceRepositoryJpaEntity.SyncStatus.valueOf(syncStatus.toUpperCase());
            sourcePage = sourceRepositoryRepository.findBySyncStatusAndLanguage(status, language, pageRequest);
        } else if (syncStatus != null) {
            SourceRepositoryJpaEntity.SyncStatus status = SourceRepositoryJpaEntity.SyncStatus.valueOf(syncStatus.toUpperCase());
            sourcePage = sourceRepositoryRepository.findBySyncStatus(status, pageRequest);
        } else if (language != null) {
            sourcePage = sourceRepositoryRepository.findByLanguage(language, pageRequest);
        } else {
            sourcePage = sourceRepositoryRepository.findAll(pageRequest);
        }
        
        // Calculate metrics
        long totalRepositories = sourceRepositoryRepository.count();
        long syncedRepositories = sourceRepositoryRepository.countBySyncStatus(SourceRepositoryJpaEntity.SyncStatus.SYNCED);
        long unsyncedRepositories = sourceRepositoryRepository.countBySyncStatus(SourceRepositoryJpaEntity.SyncStatus.UNSYNCED);
        long failedRepositories = sourceRepositoryRepository.countBySyncStatus(SourceRepositoryJpaEntity.SyncStatus.FAILED);
        
        return ResponseEntity.ok(Map.of(
            "repositories", sourcePage.getContent(),
            "pagination", Map.of(
                "page", sourcePage.getNumber(),
                "size", sourcePage.getSize(),
                "totalElements", sourcePage.getTotalElements(),
                "totalPages", sourcePage.getTotalPages()
            ),
            "metrics", Map.of(
                "totalRepositories", totalRepositories,
                "syncedRepositories", syncedRepositories,
                "unsyncedRepositories", unsyncedRepositories,
                "failedRepositories", failedRepositories,
                "syncSuccessRate", totalRepositories > 0 ? 
                    Math.round(((double) syncedRepositories / totalRepositories) * 100.0) / 100.0 : 0.0
            )
        ));
    }
    
    /**
     * Get specific source repository by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SourceRepositoryJpaEntity> getSourceRepository(@PathVariable Long id) {
        Optional<SourceRepositoryJpaEntity> sourceOpt = sourceRepositoryRepository.findById(id);
        return sourceOpt.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Trigger manual sync for specific source repository
     */
    @PostMapping("/{id}/sync")
    public ResponseEntity<Map<String, Object>> syncSourceRepository(@PathVariable Long id) {
        try {
            Optional<SourceRepositoryJpaEntity> sourceOpt = sourceRepositoryRepository.findById(id);
            if (sourceOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            SourceRepositoryJpaEntity source = sourceOpt.get();
            
            // Refresh repository data from GitHub
            gitHubSourceRepositoryService.refreshSingleRepository(source.getGithubRepoUrl());
            
            log.info("Successfully triggered sync for source repository: {}", source.getName());
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Source repository sync completed successfully",
                "repositoryId", id,
                "repositoryName", source.getName(),
                "syncedAt", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            log.error("Failed to sync source repository {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Failed to sync source repository: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Trigger source ingestion phase only
     */
    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> triggerSourceIngest() {
        try {
            syncSchedulerService.runSourceIngest();
            
            log.info("Successfully triggered source ingestion phase");
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Source ingestion phase completed successfully",
                "triggeredAt", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            log.error("Failed to trigger source ingestion: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Failed to trigger source ingestion: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get source repositories that failed sync with error details
     */
    @GetMapping("/failed")
    public ResponseEntity<List<Map<String, Object>>> getFailedRepositories() {
        List<SourceRepositoryJpaEntity> failedRepos = sourceRepositoryRepository
                .findBySyncStatus(SourceRepositoryJpaEntity.SyncStatus.FAILED);
        
        List<Map<String, Object>> failedDetails = failedRepos.stream()
                .map(repo -> {
                    Map<String, Object> details = new java.util.HashMap<>();
                    details.put("id", repo.getId());
                    details.put("name", repo.getName());
                    details.put("githubRepoUrl", repo.getGithubRepoUrl());
                    details.put("language", repo.getLanguage() != null ? repo.getLanguage() : "Unknown");
                    details.put("lastSyncAttempt", repo.getLastSyncAttempt() != null ? repo.getLastSyncAttempt().toString() : "Never");
                    details.put("syncErrorMessage", repo.getSyncErrorMessage() != null ? repo.getSyncErrorMessage() : "Unknown error");
                    details.put("starCount", repo.getStargazersCount() != null ? repo.getStargazersCount() : 0);
                    return details;
                })
                .toList();
        
        return ResponseEntity.ok(failedDetails);
    }
    
    /**
     * Reset sync status for failed repositories
     */
    @PatchMapping("/reset-failed")
    public ResponseEntity<Map<String, Object>> resetFailedRepositories() {
        try {
            List<SourceRepositoryJpaEntity> failedRepos = sourceRepositoryRepository
                    .findBySyncStatus(SourceRepositoryJpaEntity.SyncStatus.FAILED);
            
            int resetCount = 0;
            for (SourceRepositoryJpaEntity repo : failedRepos) {
                repo.setSyncStatus(SourceRepositoryJpaEntity.SyncStatus.UNSYNCED);
                repo.setSyncErrorMessage(null);
                sourceRepositoryRepository.save(repo);
                resetCount++;
            }
            
            log.info("Reset sync status for {} failed repositories", resetCount);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Reset sync status for failed repositories",
                "resetCount", resetCount,
                "resetAt", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            log.error("Failed to reset failed repositories: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Failed to reset failed repositories: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Delete source repository (with cascade checks)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSourceRepository(@PathVariable Long id) {
        try {
            Optional<SourceRepositoryJpaEntity> sourceOpt = sourceRepositoryRepository.findById(id);
            if (sourceOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            SourceRepositoryJpaEntity source = sourceOpt.get();
            
            // Check if any portfolio projects are linked to this source
            long linkedProjects = sourceRepositoryRepository.countLinkedPortfolioProjects(id);
            if (linkedProjects > 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Cannot delete source repository - " + linkedProjects + " portfolio projects are linked to it",
                    "linkedProjectsCount", linkedProjects
                ));
            }
            
            sourceRepositoryRepository.delete(source);
            
            log.info("Successfully deleted source repository: {}", source.getName());
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Source repository deleted successfully",
                "repositoryId", id,
                "repositoryName", source.getName()
            ));
            
        } catch (Exception e) {
            log.error("Failed to delete source repository {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Failed to delete source repository: " + e.getMessage()
            ));
        }
    }
}