package com.portfolio.service;

import com.portfolio.dto.SyncConfigDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncSchedulerService {
    
    private final SyncConfigService syncConfigService;
    private final GitHubSyncService gitHubSyncService;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    
    private ScheduledFuture<?> currentTask;
    
    @PostConstruct
    public void initialize() {
        log.info("Initializing SyncSchedulerService");
        scheduleIfEnabled();
    }
    
    @PreDestroy
    public void cleanup() {
        log.info("Cleaning up SyncSchedulerService");
        cancelIfScheduled();
    }
    
    public synchronized void scheduleIfEnabled() {
        SyncConfigDto config = syncConfigService.getOrCreate();
        
        if (!config.getEnabled()) {
            log.info("Auto-sync is disabled, skipping scheduling");
            return;
        }
        
        if (currentTask != null && !currentTask.isDone()) {
            log.info("Task already scheduled, skipping");
            return;
        }
        
        log.info("Scheduling auto-sync with interval of {} hours", config.getIntervalHours());
        
        Duration interval = Duration.ofHours(config.getIntervalHours());
        PeriodicTrigger trigger = new PeriodicTrigger(interval);
        trigger.setInitialDelay(interval); // Wait one interval before first execution
        
        currentTask = taskScheduler.schedule(this::runSyncTask, trigger);
        
        // Calculate and update next run time
        Instant nextRun = Instant.now().plus(interval);
        syncConfigService.updateNextRun(nextRun);
        
        log.info("Auto-sync scheduled successfully. Next run at: {}", nextRun);
    }
    
    public synchronized void cancelIfScheduled() {
        if (currentTask != null && !currentTask.isDone()) {
            log.info("Cancelling scheduled auto-sync task");
            currentTask.cancel(false);
            currentTask = null;
            syncConfigService.updateNextRun(null);
            log.info("Auto-sync task cancelled successfully");
        }
    }
    
    public synchronized void reschedule() {
        log.info("Rescheduling auto-sync task");
        cancelIfScheduled();
        scheduleIfEnabled();
    }
    
    public void runOnceNow() {
        if (isRunning.get()) {
            log.warn("Sync is already running, skipping manual trigger");
            throw new IllegalStateException("Sync is already in progress");
        }
        
        log.info("Triggering manual sync execution");
        taskScheduler.execute(this::runSyncTask);
    }
    
    public boolean isRunning() {
        return isRunning.get();
    }
    
    public Instant getNextRunTime() {
        SyncConfigDto config = syncConfigService.getOrCreate();
        return config.getNextRunAt();
    }
    
    public Instant getLastRunTime() {
        SyncConfigDto config = syncConfigService.getOrCreate();
        return config.getLastRunAt();
    }
    
    private void runSyncTask() {
        if (!isRunning.compareAndSet(false, true)) {
            log.warn("Sync task skipped: already running");
            return;
        }
        
        try {
            log.info("Starting scheduled sync task");
            Instant startTime = Instant.now();
            
            // Update last run time
            syncConfigService.updateLastRun(startTime);
            
            // Execute the actual sync
            gitHubSyncService.syncStarredProjects();
            
            // Calculate and update next run time based on current config
            SyncConfigDto config = syncConfigService.getOrCreate();
            if (config.getEnabled()) {
                Instant nextRun = startTime.plus(config.getIntervalHours(), ChronoUnit.HOURS);
                syncConfigService.updateNextRun(nextRun);
                log.info("Sync task completed. Next run scheduled for: {}", nextRun);
            }
            
        } catch (Exception e) {
            log.error("Sync task failed with error", e);
        } finally {
            isRunning.set(false);
        }
    }
}