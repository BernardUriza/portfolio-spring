package com.portfolio.service;

import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Bootstrap sync service to trigger initial portfolio sync when empty.
 * Prevents spamming by implementing cooldown and single-flight pattern.
 *
 * Creado por Bernard Orozco
 */
@Service
public class BootstrapSyncService {

    private static final Logger log = LoggerFactory.getLogger(BootstrapSyncService.class);

    private final PortfolioProjectJpaRepository portfolioProjectRepository;
    private final SyncSchedulerService syncSchedulerService;

    public BootstrapSyncService(PortfolioProjectJpaRepository portfolioProjectRepository,
                               SyncSchedulerService syncSchedulerService) {
        this.portfolioProjectRepository = portfolioProjectRepository;
        this.syncSchedulerService = syncSchedulerService;
    }
    
    private final AtomicBoolean inFlight = new AtomicBoolean(false);
    private volatile Instant lastAttempt = Instant.EPOCH;
    private final Duration cooldown = Duration.ofMinutes(10);
    
    /**
     * Bootstrap sync response data class
     */
    public record BootstrapSyncResult(boolean triggered, String reason) {}
    
    /**
     * Attempt to trigger bootstrap sync if portfolio is empty and cooldown has passed.
     * Uses atomic operations to prevent race conditions and duplicate syncs.
     * 
     * @return BootstrapSyncResult indicating if sync was triggered and why
     */
    public BootstrapSyncResult maybeTrigger() {
        long portfolioCount = portfolioProjectRepository.count();
        
        // If portfolio has projects, no bootstrap needed
        if (portfolioCount > 0) {
            return new BootstrapSyncResult(false, "portfolio-has-projects");
        }
        
        // If already in flight, don't trigger again
        if (inFlight.get()) {
            return new BootstrapSyncResult(false, "sync-in-progress");
        }
        
        // Check cooldown
        Instant now = Instant.now();
        if (Duration.between(lastAttempt, now).compareTo(cooldown) < 0) {
            long remainingSeconds = cooldown.minusSeconds(Duration.between(lastAttempt, now).getSeconds()).getSeconds();
            return new BootstrapSyncResult(false, "cooldown-active-" + remainingSeconds + "s");
        }
        
        // Try to acquire flight lock atomically
        if (!inFlight.compareAndSet(false, true)) {
            return new BootstrapSyncResult(false, "sync-in-progress");
        }
        
        try {
            lastAttempt = now;
            log.info("Triggering bootstrap sync for empty portfolio (count={})", portfolioCount);
            
            // Trigger async full sync (ingest + curate)
            triggerAsyncSync();
            
            return new BootstrapSyncResult(true, "bootstrap-triggered");
            
        } catch (Exception e) {
            log.error("Failed to trigger bootstrap sync", e);
            inFlight.set(false); // Release lock on failure
            return new BootstrapSyncResult(false, "trigger-failed: " + e.getMessage());
        }
    }
    
    /**
     * Trigger asynchronous sync and release flight lock when complete.
     * Uses CompletableFuture to avoid blocking the calling thread.
     */
    @Async
    public CompletableFuture<Void> triggerAsyncSync() {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Executing bootstrap sync pipeline");
                syncSchedulerService.runFullSync();
                log.info("Bootstrap sync completed successfully");
            } catch (Exception e) {
                log.error("Bootstrap sync failed", e);
            } finally {
                inFlight.set(false);
            }
        });
    }
    
    /**
     * Get current status for monitoring/debugging
     */
    public record BootstrapStatus(
            boolean inFlight,
            Instant lastAttempt,
            Duration cooldown,
            long portfolioCount
    ) {}
    
    public BootstrapStatus getStatus() {
        return new BootstrapStatus(
                inFlight.get(),
                lastAttempt,
                cooldown,
                portfolioProjectRepository.count()
        );
    }
}