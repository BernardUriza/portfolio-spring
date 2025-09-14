package com.portfolio.service;

import com.portfolio.adapter.out.persistence.jpa.SourceRepositoryJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.SourceRepositoryJpaRepository;
import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaEntity;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Two-phase sync scheduler service implementing the new domain pipeline.
 * Phase 1: Source ingest → Phase 2: Portfolio curation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncSchedulerService {
    
    private final GitHubSourceRepositoryService gitHubSourceRepositoryService;
    private final PortfolioService portfolioService;
    private final SourceRepositoryJpaRepository sourceRepositoryRepository;
    private final SyncMonitorService syncMonitorService;
    private final MeterRegistry meterRegistry;
    
    private final AtomicBoolean syncInProgress = new AtomicBoolean(false);
    
    /**
     * Execute complete two-phase sync pipeline:
     * 1. Ingest: GitHubSourceRepositoryService.syncStarred() → upsert in SourceRepository
     * 2. Curate: For each SourceRepository, create/update PortfolioProject with AI analysis
     */
    public void runFullSync() {
        if (!syncInProgress.compareAndSet(false, true)) {
            log.warn("Sync already in progress, skipping");
            return;
        }
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            log.info("Starting two-phase sync pipeline");
            syncMonitorService.appendLog("INFO", "Starting two-phase sync pipeline");
            
            // Phase 1: Source ingestion
            runSourceIngestPhase();
            
            // Phase 2: Portfolio curation
            runPortfolioCurationPhase();
            
            log.info("Two-phase sync pipeline completed successfully");
            syncMonitorService.appendLog("INFO", "Two-phase sync pipeline completed successfully");
            
        } catch (Exception e) {
            log.error("Error during sync pipeline execution", e);
            syncMonitorService.appendLog("ERROR", "Sync pipeline failed: " + e.getMessage());
            throw new RuntimeException("Sync pipeline failed: " + e.getMessage(), e);
        } finally {
            syncInProgress.set(false);
            sample.stop(Timer.builder("sync.pipeline.duration")
                    .description("Duration of complete sync pipeline")
                    .register(meterRegistry));
        }
    }

    /**
     * Fire-and-forget async wrapper to run full sync without blocking caller.
     * Any exception is logged and does not propagate to the caller.
     */
    @org.springframework.scheduling.annotation.Async
    public void runFullSyncAsync() {
        try {
            runFullSync();
        } catch (Exception e) {
            log.error("Async sync failed", e);
            // Swallow exception to avoid bubbling to controller threads
        }
    }
    
    /**
     * Phase 1: Source ingestion from GitHub
     */
    public void runSourceIngestPhase() {
        log.info("Phase 1: Ingesting starred repositories from GitHub");
        syncMonitorService.appendLog("INFO", "Phase 1: Starting source repository ingestion");
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            gitHubSourceRepositoryService.syncStarredRepositories();
            
            // Record metrics
            long totalSources = sourceRepositoryRepository.count();
            long syncedSources = sourceRepositoryRepository.countBySyncStatus(SourceRepositoryJpaEntity.SyncStatus.SYNCED);
            
            meterRegistry.gauge("sync.source.total", totalSources);
            meterRegistry.gauge("sync.source.synced", syncedSources);
            
            log.info("Phase 1 completed: {} total sources, {} synced", totalSources, syncedSources);
            syncMonitorService.appendLog("INFO", 
                String.format("Phase 1 completed: %d total sources, %d synced", totalSources, syncedSources));
            
        } finally {
            sample.stop(Timer.builder("sync.source.ingest.duration")
                    .description("Duration of source ingestion phase")
                    .register(meterRegistry));
        }
    }
    
    /**
     * Phase 2: Portfolio curation using AI analysis
     */
    public void runPortfolioCurationPhase() {
        log.info("Phase 2: Starting portfolio curation with AI analysis");
        syncMonitorService.appendLog("INFO", "Phase 2: Starting portfolio curation");
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            // Get unsynced source repositories
            List<SourceRepositoryJpaEntity> unsyncedSources = sourceRepositoryRepository
                    .findBySyncStatus(SourceRepositoryJpaEntity.SyncStatus.UNSYNCED);
            
            // Also get auto-linked projects that might need re-curation
            List<PortfolioProjectJpaEntity> needsCuration = portfolioService.getProjectsNeedingCuration();
            
            int curated = 0;
            int skipped = 0;
            int failed = 0;
            
            // Process unsynced sources
            for (SourceRepositoryJpaEntity source : unsyncedSources) {
                try {
                    portfolioService.curateFromSource(source.getId());
                    curated++;
                    meterRegistry.counter("sync.portfolio.created").increment();
                } catch (Exception e) {
                    log.warn("Failed to curate from source {}: {}", source.getName(), e.getMessage());
                    failed++;
                    meterRegistry.counter("sync.portfolio.failed").increment();
                }
            }
            
            // Re-curate existing auto-linked projects
            for (PortfolioProjectJpaEntity portfolio : needsCuration) {
                if (portfolio.getSourceRepositoryId() != null) {
                    try {
                        portfolioService.curateFromSource(portfolio.getSourceRepositoryId());
                        curated++;
                        meterRegistry.counter("sync.portfolio.updated").increment();
                    } catch (Exception e) {
                        log.warn("Failed to re-curate portfolio {}: {}", portfolio.getTitle(), e.getMessage());
                        failed++;
                        meterRegistry.counter("sync.portfolio.failed").increment();
                    }
                } else {
                    skipped++;
                    meterRegistry.counter("sync.portfolio.skipped.unlinked").increment();
                }
            }
            
            log.info("Phase 2 completed: {} curated, {} skipped, {} failed", curated, skipped, failed);
            syncMonitorService.appendLog("INFO", 
                String.format("Phase 2 completed: %d curated, %d skipped, %d failed", curated, skipped, failed));
            
        } finally {
            sample.stop(Timer.builder("sync.portfolio.curation.duration")
                    .description("Duration of portfolio curation phase")
                    .register(meterRegistry));
        }
    }
    
    /**
     * Run only source ingestion phase
     */
    public void runSourceIngest() {
        if (!syncInProgress.compareAndSet(false, true)) {
            log.warn("Sync already in progress, skipping source ingest");
            return;
        }
        
        try {
            runSourceIngestPhase();
        } finally {
            syncInProgress.set(false);
        }
    }
    
    /**
     * Run only portfolio curation phase
     */
    public void runPortfolioCuration() {
        if (!syncInProgress.compareAndSet(false, true)) {
            log.warn("Sync already in progress, skipping portfolio curation");
            return;
        }
        
        try {
            runPortfolioCurationPhase();
        } finally {
            syncInProgress.set(false);
        }
    }
    
    /**
     * Get sync status
     */
    public boolean isSyncInProgress() {
        return syncInProgress.get();
    }
    
    /**
     * Resync specific portfolio project (respect protections)
     */
    public void resyncPortfolioProject(Long portfolioProjectId) {
        log.info("Resyncing portfolio project ID: {}", portfolioProjectId);
        
        // This will respect field protections automatically
        PortfolioProjectJpaEntity portfolio = portfolioService.portfolioProjectRepository.findById(portfolioProjectId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio project not found: " + portfolioProjectId));
        
        if (portfolio.getSourceRepositoryId() != null) {
            try {
                portfolioService.curateFromSource(portfolio.getSourceRepositoryId());
                meterRegistry.counter("sync.portfolio.resync.success").increment();
                log.info("Successfully resynced portfolio project: {}", portfolio.getTitle());
            } catch (Exception e) {
                meterRegistry.counter("sync.portfolio.resync.failed").increment();
                log.error("Failed to resync portfolio project {}: {}", portfolio.getTitle(), e.getMessage());
                throw new RuntimeException("Resync failed: " + e.getMessage(), e);
            }
        } else {
            log.warn("Cannot resync portfolio project {} - not linked to source repository", portfolio.getTitle());
            throw new IllegalStateException("Portfolio project not linked to source repository");
        }
    }
}
