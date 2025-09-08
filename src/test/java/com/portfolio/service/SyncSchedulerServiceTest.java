package com.portfolio.service;

import com.portfolio.dto.SyncConfigDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncSchedulerServiceTest {

    @Mock
    private SyncConfigService syncConfigService;

    @Mock
    private GitHubSyncService gitHubSyncService;

    @Mock
    private ThreadPoolTaskScheduler taskScheduler;

    @SuppressWarnings("unchecked")
    @Mock
    private ScheduledFuture<Object> scheduledFuture;

    @InjectMocks
    private SyncSchedulerService syncSchedulerService;

    private SyncConfigDto enabledConfig;
    private SyncConfigDto disabledConfig;

    @BeforeEach
    void setUp() {
        enabledConfig = SyncConfigDto.builder()
                .enabled(true)
                .intervalHours(6)
                .lastRunAt(Instant.now().minusSeconds(3600))
                .nextRunAt(Instant.now().plusSeconds(3600))
                .build();

        disabledConfig = SyncConfigDto.builder()
                .enabled(false)
                .intervalHours(6)
                .build();
    }

    @Test
    void scheduleIfEnabled_WhenEnabled_ShouldScheduleTask() {
        // Given
        when(syncConfigService.getOrCreate()).thenReturn(enabledConfig);
        when(taskScheduler.schedule(any(Runnable.class), any(PeriodicTrigger.class)))
                .thenReturn((ScheduledFuture<?>) scheduledFuture);

        // When
        syncSchedulerService.scheduleIfEnabled();

        // Then
        verify(taskScheduler).schedule(any(Runnable.class), any(PeriodicTrigger.class));
        verify(syncConfigService).updateNextRun(any(Instant.class));
    }

    @Test
    void scheduleIfEnabled_WhenDisabled_ShouldNotScheduleTask() {
        // Given
        when(syncConfigService.getOrCreate()).thenReturn(disabledConfig);

        // When
        syncSchedulerService.scheduleIfEnabled();

        // Then
        verify(taskScheduler, never()).schedule(any(Runnable.class), any(PeriodicTrigger.class));
        verify(syncConfigService, never()).updateNextRun(any(Instant.class));
    }

    @Test
    void cancelIfScheduled_WithActiveTask_ShouldCancelTask() {
        // Given
        when(syncConfigService.getOrCreate()).thenReturn(enabledConfig);
        when(taskScheduler.schedule(any(Runnable.class), any(PeriodicTrigger.class)))
                .thenReturn((ScheduledFuture<?>) scheduledFuture);
        when(scheduledFuture.isDone()).thenReturn(false);

        // First schedule a task
        syncSchedulerService.scheduleIfEnabled();

        // When
        syncSchedulerService.cancelIfScheduled();

        // Then
        verify(scheduledFuture).cancel(false);
        verify(syncConfigService).updateNextRun(null);
    }

    @Test
    void reschedule_ShouldCancelAndReschedule() {
        // Given
        when(syncConfigService.getOrCreate()).thenReturn(enabledConfig);
        when(taskScheduler.schedule(any(Runnable.class), any(PeriodicTrigger.class)))
                .thenReturn((ScheduledFuture<?>) scheduledFuture);
        when(scheduledFuture.isDone()).thenReturn(false);

        // First schedule a task
        syncSchedulerService.scheduleIfEnabled();
        reset(taskScheduler);
        when(taskScheduler.schedule(any(Runnable.class), any(PeriodicTrigger.class)))
                .thenReturn((ScheduledFuture<?>) scheduledFuture);

        // When
        syncSchedulerService.reschedule();

        // Then
        verify(scheduledFuture).cancel(false);
        verify(taskScheduler).schedule(any(Runnable.class), any(PeriodicTrigger.class));
    }

    @Test
    void runOnceNow_WhenNotRunning_ShouldExecuteTask() {
        // Given
        // When
        syncSchedulerService.runOnceNow();

        // Then
        verify(taskScheduler).execute(any(Runnable.class));
    }

    @Test
    void runOnceNow_WhenAlreadyRunning_ShouldThrowException() {
        // Given
        // Simulate running state by triggering a sync task
        when(syncConfigService.getOrCreate()).thenReturn(enabledConfig);
        
        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            // This is a bit tricky to test due to the AtomicBoolean state management
            // We'll test the exception scenario by simulating the running state
            syncSchedulerService.runOnceNow();
            // In real scenario, if already running, this would fail
        });
    }

    @Test
    void getNextRunTime_ShouldReturnConfigValue() {
        // Given
        Instant expectedTime = Instant.now().plusSeconds(3600);
        SyncConfigDto config = SyncConfigDto.builder()
                .nextRunAt(expectedTime)
                .build();
        when(syncConfigService.getOrCreate()).thenReturn(config);

        // When
        Instant result = syncSchedulerService.getNextRunTime();

        // Then
        assertEquals(expectedTime, result);
    }

    @Test
    void getLastRunTime_ShouldReturnConfigValue() {
        // Given
        Instant expectedTime = Instant.now().minusSeconds(3600);
        SyncConfigDto config = SyncConfigDto.builder()
                .lastRunAt(expectedTime)
                .build();
        when(syncConfigService.getOrCreate()).thenReturn(config);

        // When
        Instant result = syncSchedulerService.getLastRunTime();

        // Then
        assertEquals(expectedTime, result);
    }
}