package com.portfolio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Simplified sync scheduler service for the new domain structure.
 * TODO: Implement two-phase sync pipeline (source ingest → portfolio curation)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncSchedulerService {
    
    private final GitHubSourceRepositoryService gitHubSourceRepositoryService;
    
    /**
     * Execute two-phase sync pipeline:
     * 1. Ingest: GitHubSourceRepositoryService.syncStarred() → upsert in SourceRepository
     * 2. Curate: For each SourceRepository, create/update PortfolioProject (TODO)
     */
    public void runFullSync() {
        log.info("Starting two-phase sync pipeline");
        
        try {
            // Phase 1: Source ingestion
            log.info("Phase 1: Ingesting starred repositories from GitHub");
            gitHubSourceRepositoryService.syncStarredRepositories();
            
            // Phase 2: Portfolio curation (TODO)
            log.info("Phase 2: Portfolio curation not yet implemented");
            // TODO: Implement portfolio curation service
            
            log.info("Two-phase sync pipeline completed successfully");
        } catch (Exception e) {
            log.error("Error during sync pipeline execution", e);
            throw new RuntimeException("Sync pipeline failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Run only source ingestion phase
     */
    public void runSourceIngest() {
        log.info("Running source ingestion only");
        gitHubSourceRepositoryService.syncStarredRepositories();
    }
    
    /**
     * Get sync status (placeholder)
     */
    public boolean isSyncInProgress() {
        // TODO: Implement proper sync status tracking
        return false;
    }
}