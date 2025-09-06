package com.portfolio.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncMonitorService {
    
    private static final int MAX_LOG_ENTRIES = 1000;
    private static final long SYNC_INTERVAL_SECONDS = 300; // 5 minutes
    
    private final Queue<LogEntry> logBuffer = new ConcurrentLinkedQueue<>();
    private final List<Consumer<LogEntry>> sseListeners = new CopyOnWriteArrayList<>();
    private final AtomicLong logIdCounter = new AtomicLong(0);
    
    @Getter
    private volatile LocalDateTime lastSyncTime = LocalDateTime.now();
    
    @Getter
    private volatile boolean syncInProgress = false;
    
    @Getter
    private volatile int totalGitHubProjects = 0;
    
    @Getter
    private volatile int totalDatabaseProjects = 0;
    
    @Getter
    private volatile List<UnsyncedProject> unsyncedProjects = new LinkedList<>();
    
    public void markSyncStarted() {
        this.syncInProgress = true;
        appendLog("INFO", "Sync operation started");
    }
    
    public void markSyncCompleted(int gitHubCount, int dbCount, List<UnsyncedProject> unsynced) {
        this.syncInProgress = false;
        this.lastSyncTime = LocalDateTime.now();
        this.totalGitHubProjects = gitHubCount;
        this.totalDatabaseProjects = dbCount;
        this.unsyncedProjects = unsynced != null ? unsynced : new LinkedList<>();
        
        appendLog("INFO", String.format("Sync completed: %d GitHub projects, %d database projects, %d unsynced", 
            gitHubCount, dbCount, this.unsyncedProjects.size()));
    }
    
    public void markSyncFailed(String error) {
        this.syncInProgress = false;
        appendLog("ERROR", "Sync failed: " + error);
    }
    
    public long getSecondsUntilNextSync() {
        if (syncInProgress) {
            return 0;
        }
        LocalDateTime nextSync = lastSyncTime.plusSeconds(SYNC_INTERVAL_SECONDS);
        long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), nextSync);
        return Math.max(0, seconds);
    }
    
    public void appendLog(String level, String message) {
        LogEntry entry = new LogEntry(
            logIdCounter.incrementAndGet(),
            LocalDateTime.now(),
            level,
            message
        );
        
        logBuffer.offer(entry);
        
        // Maintain max buffer size
        while (logBuffer.size() > MAX_LOG_ENTRIES) {
            logBuffer.poll();
        }
        
        // Notify SSE listeners
        notifyListeners(entry);
        
        // Also log to standard logger
        switch (level) {
            case "ERROR":
                log.error("SYNC: {}", message);
                break;
            case "WARN":
                log.warn("SYNC: {}", message);
                break;
            case "DEBUG":
                log.debug("SYNC: {}", message);
                break;
            default:
                log.info("SYNC: {}", message);
        }
    }
    
    public List<LogEntry> getLogsSince(long lastId) {
        return logBuffer.stream()
            .filter(entry -> entry.getId() > lastId)
            .toList();
    }
    
    public List<LogEntry> getAllLogs() {
        return new LinkedList<>(logBuffer);
    }
    
    public void addSseListener(Consumer<LogEntry> listener) {
        sseListeners.add(listener);
        log.debug("SSE listener added. Total listeners: {}", sseListeners.size());
    }
    
    public void removeSseListener(Consumer<LogEntry> listener) {
        sseListeners.remove(listener);
        log.debug("SSE listener removed. Total listeners: {}", sseListeners.size());
    }
    
    private void notifyListeners(LogEntry entry) {
        for (Consumer<LogEntry> listener : sseListeners) {
            try {
                listener.accept(entry);
            } catch (Exception e) {
                log.error("Error notifying SSE listener", e);
                sseListeners.remove(listener);
            }
        }
    }
    
    @Getter
    public static class LogEntry {
        private final long id;
        private final LocalDateTime timestamp;
        private final String level;
        private final String message;
        
        public LogEntry(long id, LocalDateTime timestamp, String level, String message) {
            this.id = id;
            this.timestamp = timestamp;
            this.level = level;
            this.message = message;
        }
    }
    
    @Getter
    public static class UnsyncedProject {
        private final String id;
        private final String name;
        private final String reason;
        
        public UnsyncedProject(String id, String name, String reason) {
            this.id = id;
            this.name = name;
            this.reason = reason;
        }
    }
}