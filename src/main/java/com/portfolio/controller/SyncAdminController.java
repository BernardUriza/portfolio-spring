package com.portfolio.controller;

import com.portfolio.dto.SyncStatusDto;
import com.portfolio.dto.StarredProjectDto;
import com.portfolio.service.GitHubSyncService;
import com.portfolio.service.StarredProjectService;
import com.portfolio.service.SyncMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/sync")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class SyncAdminController {
    
    private final SyncMonitorService syncMonitorService;
    private final GitHubSyncService gitHubSyncService;
    private final StarredProjectService starredProjectService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ConcurrentHashMap<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    
    @GetMapping("/status")
    public ResponseEntity<SyncStatusDto> getSyncStatus() {
        List<SyncStatusDto.UnsyncedProjectDto> unsyncedDtos = syncMonitorService.getUnsyncedProjects()
            .stream()
            .map(p -> SyncStatusDto.UnsyncedProjectDto.builder()
                .id(p.getId())
                .name(p.getName())
                .reason(p.getReason())
                .build())
            .collect(Collectors.toList());
        
        SyncStatusDto status = SyncStatusDto.builder()
            .lastSync(syncMonitorService.getLastSyncTime())
            .timeUntilNextSync(syncMonitorService.getSecondsUntilNextSync())
            .totalGitHubProjects(syncMonitorService.getTotalGitHubProjects())
            .totalDatabaseProjects(syncMonitorService.getTotalDatabaseProjects())
            .unsyncedProjects(unsyncedDtos)
            .syncInProgress(syncMonitorService.isSyncInProgress())
            .build();
        
        return ResponseEntity.ok(status);
    }
    
    @PostMapping("/run")
    public ResponseEntity<Void> triggerManualSync() {
        if (syncMonitorService.isSyncInProgress()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        // Run sync asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                syncMonitorService.appendLog("INFO", "Manual sync triggered");
                gitHubSyncService.syncStarredProjects();
            } catch (Exception e) {
                log.error("Manual sync failed", e);
                syncMonitorService.markSyncFailed(e.getMessage());
            }
        }, executorService);
        
        return ResponseEntity.accepted().build();
    }
    
    @GetMapping("/log")
    public ResponseEntity<List<SyncMonitorService.LogEntry>> getLogs(
            @RequestParam(defaultValue = "0") long offset) {
        List<SyncMonitorService.LogEntry> logs = offset == 0 
            ? syncMonitorService.getAllLogs()
            : syncMonitorService.getLogsSince(offset);
        
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping(value = "/log/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamLogs() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        String emitterId = String.valueOf(System.currentTimeMillis());
        
        sseEmitters.put(emitterId, emitter);
        
        // Send initial logs
        try {
            List<SyncMonitorService.LogEntry> logs = syncMonitorService.getAllLogs();
            for (SyncMonitorService.LogEntry log : logs) {
                emitter.send(SseEmitter.event()
                    .name("log")
                    .data(log));
            }
        } catch (IOException e) {
            log.error("Error sending initial logs", e);
            emitter.completeWithError(e);
            return emitter;
        }
        
        // Register listener for new logs
        Consumer<SyncMonitorService.LogEntry> listener = logEntry -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("log")
                    .data(logEntry));
            } catch (IOException e) {
                log.error("Error sending log via SSE", e);
                emitter.completeWithError(e);
                sseEmitters.remove(emitterId);
            }
        };
        
        syncMonitorService.addSseListener(listener);
        
        // Clean up on completion
        emitter.onCompletion(() -> {
            syncMonitorService.removeSseListener(listener);
            sseEmitters.remove(emitterId);
        });
        
        emitter.onTimeout(() -> {
            syncMonitorService.removeSseListener(listener);
            sseEmitters.remove(emitterId);
        });
        
        emitter.onError(e -> {
            syncMonitorService.removeSseListener(listener);
            sseEmitters.remove(emitterId);
        });
        
        return emitter;
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Sync admin API is running");
    }
    
    // StarredProject Management Endpoints
    
    @GetMapping("/projects")
    public ResponseEntity<List<StarredProjectDto>> getAllStarredProjects() {
        List<StarredProjectDto> projects = starredProjectService.getAllStarredProjects();
        return ResponseEntity.ok(projects);
    }
    
    @GetMapping("/projects/{id}")
    public ResponseEntity<StarredProjectDto> getStarredProject(@PathVariable Long id) {
        return starredProjectService.getStarredProject(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/project/{id}")
    public ResponseEntity<Void> deleteStarredProject(@PathVariable Long id) {
        try {
            boolean deleted = starredProjectService.deleteStarredProject(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting starred project with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PatchMapping("/projects/{id}/homepage")
    public ResponseEntity<StarredProjectDto> updateProjectHomepage(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        try {
            String homepage = payload.get("homepage");
            if (homepage == null) {
                return ResponseEntity.badRequest().build();
            }
            
            Optional<StarredProjectDto> updated = starredProjectService.updateProjectHomepage(id, homepage);
            return updated.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error updating project homepage for ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/process")
    public ResponseEntity<Void> processUnsyncedProjects() {
        try {
            syncMonitorService.appendLog("INFO", "Manual Claude processing triggered");
            
            // Run processing asynchronously
            CompletableFuture.runAsync(() -> {
                try {
                    starredProjectService.processUnsyncedProjects();
                } catch (Exception e) {
                    log.error("Manual Claude processing failed", e);
                    syncMonitorService.markSyncFailed(e.getMessage());
                }
            }, executorService);
            
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error triggering Claude processing", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/process/{id}")
    public ResponseEntity<Void> processSpecificProject(@PathVariable Long id) {
        try {
            syncMonitorService.appendLog("INFO", "Manual Claude processing triggered for project ID: " + id);
            
            // Run processing asynchronously  
            CompletableFuture.runAsync(() -> {
                try {
                    starredProjectService.processStarredProject(id);
                } catch (Exception e) {
                    log.error("Manual Claude processing failed for project ID: " + id, e);
                    syncMonitorService.appendLog("ERROR", "Processing failed for project " + id + ": " + e.getMessage());
                }
            }, executorService);
            
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error triggering Claude processing for project ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/reset-unsynced")
    public ResponseEntity<Void> resetToUnsynced() {
        try {
            syncMonitorService.appendLog("INFO", "Resetting all projects to UNSYNCED status");
            starredProjectService.resetAllProjectsToUnsynced();
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error resetting projects to unsynced", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/test-claude")
    public ResponseEntity<Map<String, Object>> testClaudeAPI() {
        try {
            log.info("Testing Claude API connection");
            Map<String, Object> result = starredProjectService.testClaudeConnection();
            
            // Add debug info about API key
            String apiKey = System.getProperty("anthropic.api.key");
            if (apiKey == null) {
                apiKey = System.getenv("ANTHROPIC_API_KEY");
            }
            
            if (apiKey != null && apiKey.length() > 10) {
                result.put("apiKeyPresent", true);
                result.put("apiKeyPreview", apiKey.substring(0, 10) + "...");
            } else {
                result.put("apiKeyPresent", false);
                result.put("apiKeyValue", apiKey);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error testing Claude API", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}