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
            syncSchedulerService.runFullSync();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Sync completed successfully"
            ));
        } catch (Exception e) {
            log.error("Manual sync failed", e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Sync failed: " + e.getMessage()
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