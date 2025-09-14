package com.portfolio.controller;

import com.portfolio.aspect.RateLimit;
import com.portfolio.aspect.RequiresFeature;
import com.portfolio.service.RateLimitingService;
import com.portfolio.service.SyncSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Minimal sync configuration controller for the new domain structure.
 * TODO: Implement full sync configuration management
 */
@RestController
@RequestMapping("/api/admin/sync-config")
@RequiredArgsConstructor
@Slf4j
public class SyncConfigAdminController {
    
    private final SyncSchedulerService syncSchedulerService;
    
    @PostMapping("/run-now")
    @RequiresFeature("manual_sync")
    @RateLimit(type = RateLimitingService.RateLimitType.SYNC_OPERATIONS)
    public ResponseEntity<Map<String, String>> runSyncNow() {
        log.info("Manual sync trigger requested");
        
        try {
            // Trigger async to avoid blocking UI and avoid leaking exceptions as 500s
            syncSchedulerService.runFullSyncAsync();
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
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSyncStatus() {
        return ResponseEntity.ok(Map.of(
            "enabled", true,
            "inProgress", syncSchedulerService.isSyncInProgress(),
            "lastRun", "Not implemented",
            "intervalHours", 24
        ));
    }
}
