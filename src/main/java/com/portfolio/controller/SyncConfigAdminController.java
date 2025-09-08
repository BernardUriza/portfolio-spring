package com.portfolio.controller;

import com.portfolio.dto.SyncConfigDto;
import com.portfolio.dto.SyncConfigUpdateDto;
import com.portfolio.dto.SyncStatusResponseDto;
import com.portfolio.service.SyncConfigService;
import com.portfolio.service.SyncSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Slf4j
@RestController
@RequestMapping("/api/admin/sync-config")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Validated
public class SyncConfigAdminController {
    
    private final SyncConfigService syncConfigService;
    private final SyncSchedulerService syncSchedulerService;
    
    @GetMapping
    public ResponseEntity<SyncConfigDto> getSyncConfig() {
        try {
            log.debug("Getting sync configuration");
            SyncConfigDto config = syncConfigService.getOrCreate();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("Error retrieving sync configuration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping
    public ResponseEntity<SyncConfigDto> updateSyncConfig(
            @Valid @RequestBody SyncConfigUpdateDto updateDto) {
        try {
            log.info("Updating sync configuration: enabled={}, intervalHours={}", 
                    updateDto.getEnabled(), updateDto.getIntervalHours());
            
            // Update configuration
            SyncConfigDto updatedConfig = syncConfigService.update(
                    updateDto.getEnabled(), 
                    updateDto.getIntervalHours(), 
                    "admin"
            );
            
            // Reschedule the sync task
            syncSchedulerService.reschedule();
            
            log.info("Sync configuration updated and rescheduled successfully");
            return ResponseEntity.ok(updatedConfig);
            
        } catch (Exception e) {
            log.error("Error updating sync configuration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/run-now")
    public ResponseEntity<Void> runSyncNow() {
        try {
            syncSchedulerService.runOnceNow();
            log.info("Manual sync execution triggered successfully");
            return ResponseEntity.accepted().build();
            
        } catch (IllegalStateException e) {
            log.warn("Cannot trigger manual sync: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
            
        } catch (Exception e) {
            log.error("Error triggering manual sync", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<SyncStatusResponseDto> getSyncStatus() {
        try {
            log.debug("Getting sync status");
            
            SyncStatusResponseDto status = SyncStatusResponseDto.builder()
                    .running(syncSchedulerService.isRunning())
                    .lastRunAt(syncSchedulerService.getLastRunTime())
                    .nextRunAt(syncSchedulerService.getNextRunTime())
                    .build();
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("Error retrieving sync status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}