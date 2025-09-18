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
import java.util.HashMap;

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
        try {
            SyncConfigDto cfg = syncConfigService.getOrCreate();
            // Use HashMap to avoid NPE with null values (Map.of doesn't accept nulls)
            Map<String, Object> response = new HashMap<>();
            response.put("enabled", cfg.getEnabled() != null ? cfg.getEnabled() : false);
            response.put("intervalHours", cfg.getIntervalHours() != null ? cfg.getIntervalHours() : 6);
            response.put("lastRunAt", cfg.getLastRunAt() != null ? cfg.getLastRunAt().toString() : null);
            response.put("nextRunAt", cfg.getNextRunAt() != null ? cfg.getNextRunAt().toString() : null);
            response.put("running", syncSchedulerService.isSyncInProgress());

            log.info("Sync status retrieved successfully: enabled={}, intervalHours={}",
                    response.get("enabled"), response.get("intervalHours"));
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.warn("Sync status failed, returning default empty state. Error: {}", ex.getMessage(), ex);
            // Return default empty state when no config exists or any error occurs
            Map<String, Object> defaultResponse = new HashMap<>();
            defaultResponse.put("enabled", false);
            defaultResponse.put("intervalHours", 6);
            defaultResponse.put("lastRunAt", null);
            defaultResponse.put("nextRunAt", null);
            defaultResponse.put("running", false);
            return ResponseEntity.ok(defaultResponse);
        }
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
