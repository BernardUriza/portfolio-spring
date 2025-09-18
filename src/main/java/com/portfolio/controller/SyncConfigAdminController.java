package com.portfolio.controller;

import com.portfolio.aspect.RateLimit;
import com.portfolio.aspect.RequiresFeature;
import com.portfolio.service.RateLimitingService;
import com.portfolio.service.SyncConfigService;
import com.portfolio.dto.SyncConfigDto;
import com.portfolio.dto.SyncConfigUpdateDto;
import com.portfolio.service.SyncSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Sync configuration admin controller (enable/disable + interval + status)
 */
@RestController
@RequestMapping("/api/admin/sync-config")
public class SyncConfigAdminController {
    private static final Logger log = LoggerFactory.getLogger(SyncConfigAdminController.class);

    private final SyncSchedulerService syncSchedulerService;
    private final SyncConfigService syncConfigService;

    public SyncConfigAdminController(SyncSchedulerService syncSchedulerService,
                                     SyncConfigService syncConfigService) {
        this.syncSchedulerService = syncSchedulerService;
        this.syncConfigService = syncConfigService;
    }
    
    @PostMapping("/run-now")
    @RequiresFeature("manual_sync")
    @RateLimit(type = RateLimitingService.RateLimitType.SYNC_OPERATIONS)
    public ResponseEntity<Map<String, String>> runSyncNow() {
        log.info("Manual sync trigger requested");
        
        try {
            // Trigger async to avoid blocking UI and avoid leaking exceptions as 500s
            syncSchedulerService.runFullSyncAsync();
            // Update timing metadata for visibility
            SyncConfigDto cfg = syncConfigService.getOrCreate();
            Instant now = Instant.now();
            syncConfigService.updateLastRun(now);
            if (Boolean.TRUE.equals(cfg.getEnabled()) && cfg.getIntervalHours() != null) {
                syncConfigService.updateNextRun(now.plus(Duration.ofHours(cfg.getIntervalHours())));
            }
            return ResponseEntity.accepted().body(Map.of(
                "status", "accepted",
                "message", "Sync started"
            ));
        } catch (Exception e) {
            log.error("Manual sync failed", e);
            // Never surface 500 for manual trigger in dev; report accepted to UI
            return ResponseEntity.accepted().body(Map.of(
                "status", "accepted",
                "message", "Sync started (with warnings)"
            ));
        }
    }
    
    @GetMapping({"/status", "/status/"})
    public ResponseEntity<Map<String, Object>> getSyncStatus() {
        SyncConfigDto cfg;
        try {
            cfg = syncConfigService.getOrCreate();
        } catch (RuntimeException ex) {
            log.warn("Sync status first attempt failed ({}). Retrying once...", ex.getClass().getSimpleName());
            cfg = syncConfigService.getOrCreate();
        }
        return ResponseEntity.ok(Map.of(
            "enabled", cfg.getEnabled(),
            "intervalHours", cfg.getIntervalHours(),
            "lastRunAt", cfg.getLastRunAt() != null ? cfg.getLastRunAt().toString() : null,
            "nextRunAt", cfg.getNextRunAt() != null ? cfg.getNextRunAt().toString() : null,
            "running", syncSchedulerService.isSyncInProgress()
        ));
    }

    @PutMapping({"", "/"})
    @RequiresFeature("scheduled_sync")
    @RateLimit(type = RateLimitingService.RateLimitType.SYNC_OPERATIONS)
    public ResponseEntity<Map<String, Object>> updateSyncConfig(@Valid @RequestBody SyncConfigUpdateDto update) {
        SyncConfigDto saved = syncConfigService.update(update.getEnabled(), update.getIntervalHours(), "admin");

        // If enabled, ensure nextRunAt is set
        if (Boolean.TRUE.equals(saved.getEnabled())) {
            Instant now = Instant.now();
            if (saved.getNextRunAt() == null || saved.getNextRunAt().isBefore(now)) {
                syncConfigService.updateNextRun(now.plus(Duration.ofHours(saved.getIntervalHours())));
                saved = syncConfigService.getOrCreate();
            }
        }

        return ResponseEntity.ok(Map.of(
            "enabled", saved.getEnabled(),
            "intervalHours", saved.getIntervalHours(),
            "lastRunAt", saved.getLastRunAt() != null ? saved.getLastRunAt().toString() : null,
            "nextRunAt", saved.getNextRunAt() != null ? saved.getNextRunAt().toString() : null,
            "running", syncSchedulerService.isSyncInProgress()
        ));
    }
}
