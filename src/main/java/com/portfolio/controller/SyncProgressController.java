package com.portfolio.controller;

import com.portfolio.service.GitHubSyncProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Controller for GitHub sync progress via Server-Sent Events
 * Provides real-time updates on sync operations
 *
 * Created by Bernard Orozco
 */
@RestController
@RequestMapping("/api/admin/sync/progress")
@CrossOrigin(origins = "*")
public class SyncProgressController {

    private static final Logger log = LoggerFactory.getLogger(SyncProgressController.class);

    private final GitHubSyncProgressService progressService;

    public SyncProgressController(GitHubSyncProgressService progressService) {
        this.progressService = progressService;
    }

    /**
     * Stream sync progress via Server-Sent Events
     * GET /api/admin/sync/progress/stream/{syncId}
     */
    @GetMapping(value = "/stream/{syncId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamProgress(@PathVariable String syncId) {
        log.info("Client connected to sync progress stream: {}", syncId);
        return progressService.createEmitter(syncId);
    }

    /**
     * Get current sync progress (polling fallback)
     * GET /api/admin/sync/progress/{syncId}
     */
    @GetMapping("/{syncId}")
    public ResponseEntity<GitHubSyncProgressService.SyncProgress> getProgress(@PathVariable String syncId) {
        GitHubSyncProgressService.SyncProgress progress = progressService.getProgress(syncId);

        if (progress == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(progress);
    }
}
