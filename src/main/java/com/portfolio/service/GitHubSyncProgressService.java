package com.portfolio.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service for real-time GitHub sync progress updates via Server-Sent Events (SSE)
 * Created by Bernard Orozco
 */
@Service
public class GitHubSyncProgressService {

    private static final Logger log = LoggerFactory.getLogger(GitHubSyncProgressService.class);
    private static final long SSE_TIMEOUT = 30_000L; // 30 seconds

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final Map<String, SyncProgress> progressMap = new ConcurrentHashMap<>();

    /**
     * Create a new SSE emitter for sync progress
     */
    public SseEmitter createEmitter(String syncId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitter.onCompletion(() -> {
            log.debug("SSE completed for sync: {}", syncId);
            emitters.remove(emitter);
        });

        emitter.onTimeout(() -> {
            log.debug("SSE timeout for sync: {}", syncId);
            emitters.remove(emitter);
            emitter.complete();
        });

        emitter.onError(ex -> {
            log.warn("SSE error for sync {}: {}", syncId, ex.getMessage());
            emitters.remove(emitter);
        });

        emitters.add(emitter);
        log.info("New SSE emitter created for sync: {} (total: {})", syncId, emitters.size());

        return emitter;
    }

    /**
     * Broadcast progress update to all connected clients
     */
    public void broadcastProgress(String syncId, SyncProgressEvent event) {
        // Update progress map
        SyncProgress progress = progressMap.computeIfAbsent(syncId, k -> new SyncProgress());
        progress.updateFromEvent(event);

        // Broadcast to all emitters
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                    .name("sync-progress")
                    .data(event)
                    .id(syncId)
                );
            } catch (IOException e) {
                log.debug("Failed to send SSE event, marking emitter as dead: {}", e.getMessage());
                deadEmitters.add(emitter);
            }
        }

        // Remove dead emitters
        emitters.removeAll(deadEmitters);
        deadEmitters.forEach(SseEmitter::complete);
    }

    /**
     * Mark sync as completed
     */
    public void markCompleted(String syncId, int totalProcessed, int successCount, int failureCount) {
        SyncProgressEvent event = new SyncProgressEvent(
            syncId,
            SyncPhase.COMPLETED,
            100,
            totalProcessed,
            successCount,
            failureCount,
            "Sync completed successfully",
            null
        );

        broadcastProgress(syncId, event);
        completeAllEmitters();
    }

    /**
     * Mark sync as failed
     */
    public void markFailed(String syncId, String errorMessage) {
        SyncProgress progress = progressMap.get(syncId);
        int totalProcessed = progress != null ? progress.totalProcessed : 0;

        SyncProgressEvent event = new SyncProgressEvent(
            syncId,
            SyncPhase.FAILED,
            0,
            totalProcessed,
            0,
            totalProcessed,
            "Sync failed: " + errorMessage,
            errorMessage
        );

        broadcastProgress(syncId, event);
        completeAllEmitters();
    }

    /**
     * Get current sync progress
     */
    public SyncProgress getProgress(String syncId) {
        return progressMap.get(syncId);
    }

    /**
     * Complete all active emitters
     */
    private void completeAllEmitters() {
        emitters.forEach(SseEmitter::complete);
        emitters.clear();
    }

    /**
     * Sync progress event for SSE
     */
    public static class SyncProgressEvent {
        public String syncId;
        public SyncPhase phase;
        public int progressPercentage;
        public int totalProcessed;
        public int successCount;
        public int failureCount;
        public String message;
        public String errorDetail;
        public LocalDateTime timestamp;

        public SyncProgressEvent(String syncId, SyncPhase phase, int progressPercentage,
                               int totalProcessed, int successCount, int failureCount,
                               String message, String errorDetail) {
            this.syncId = syncId;
            this.phase = phase;
            this.progressPercentage = progressPercentage;
            this.totalProcessed = totalProcessed;
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.message = message;
            this.errorDetail = errorDetail;
            this.timestamp = LocalDateTime.now();
        }
    }

    /**
     * Sync phase enum
     */
    public enum SyncPhase {
        STARTING,
        FETCHING_REPOS,
        PROCESSING_REPOS,
        FETCHING_README,
        AI_CURATION,
        COMPLETED,
        FAILED
    }

    /**
     * Aggregate sync progress
     */
    public static class SyncProgress {
        public int totalProcessed = 0;
        public int successCount = 0;
        public int failureCount = 0;
        public int progressPercentage = 0;
        public SyncPhase currentPhase = SyncPhase.STARTING;
        public LocalDateTime lastUpdate = LocalDateTime.now();

        public void updateFromEvent(SyncProgressEvent event) {
            this.totalProcessed = event.totalProcessed;
            this.successCount = event.successCount;
            this.failureCount = event.failureCount;
            this.progressPercentage = event.progressPercentage;
            this.currentPhase = event.phase;
            this.lastUpdate = LocalDateTime.now();
        }
    }
}
