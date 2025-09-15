/**
 * Creado por Bernard Orozco
 * Tests for bootstrap sync service
 */
package com.portfolio.service;

import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BootstrapSyncServiceTest {

    @InjectMocks
    private BootstrapSyncService bootstrapSyncService;

    @Mock
    private PortfolioProjectJpaRepository portfolioProjectRepository;

    @Mock
    private SyncSchedulerService syncSchedulerService;

    @BeforeEach
    public void setUp() {
        // Reset cooldown state for each test
        ReflectionTestUtils.setField(bootstrapSyncService, "lastAttempt", java.time.Instant.EPOCH);
        ReflectionTestUtils.setField(bootstrapSyncService, "inFlight", new AtomicBoolean(false));
    }

    @Test
    public void testSkipSyncWhenPortfolioHasProjects() {
        when(portfolioProjectRepository.count()).thenReturn(5L);

        BootstrapSyncService.BootstrapSyncResult result = bootstrapSyncService.maybeTrigger();

        assertFalse(result.triggered());
        assertEquals("portfolio-has-projects", result.reason());
        verify(portfolioProjectRepository).count();
        verifyNoInteractions(syncSchedulerService);
    }

    @Test
    public void testTriggerSyncWhenPortfolioEmpty() {
        when(portfolioProjectRepository.count()).thenReturn(0L);

        BootstrapSyncService.BootstrapSyncResult result = bootstrapSyncService.maybeTrigger();

        assertTrue(result.triggered());
        assertEquals("bootstrap-triggered", result.reason());
        verify(portfolioProjectRepository).count();
        // Note: syncSchedulerService.runFullSync() is called async, so we can't verify it directly in this test
    }

    @Test
    public void testCooldownPreventsFrequentSyncs() {
        when(portfolioProjectRepository.count()).thenReturn(0L);

        // First call should trigger
        BootstrapSyncService.BootstrapSyncResult result1 = bootstrapSyncService.maybeTrigger();
        assertTrue(result1.triggered());
        assertEquals("bootstrap-triggered", result1.reason());

        // Wait a moment for async operation to start and set lastAttempt
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Second call should be blocked by cooldown
        BootstrapSyncService.BootstrapSyncResult result2 = bootstrapSyncService.maybeTrigger();
        assertFalse(result2.triggered());
        assertTrue(result2.reason().startsWith("cooldown-active-"));

        verify(portfolioProjectRepository, times(2)).count();
        // Note: Can't verify async calls directly
    }

    @Test
    public void testPreventsConcurrentSyncs() {
        when(portfolioProjectRepository.count()).thenReturn(0L);
        
        // Simulate sync in progress
        ReflectionTestUtils.setField(bootstrapSyncService, "inFlight", new AtomicBoolean(true));

        BootstrapSyncService.BootstrapSyncResult result = bootstrapSyncService.maybeTrigger();

        assertFalse(result.triggered());
        assertEquals("sync-in-progress", result.reason());
        verify(portfolioProjectRepository).count();
        verifyNoInteractions(syncSchedulerService);
    }

    @Test
    public void testSyncAfterCooldownExpires() {
        when(portfolioProjectRepository.count()).thenReturn(0L);

        // Set last sync attempt to 11 minutes ago (beyond 10-minute cooldown)
        java.time.Instant elevenMinutesAgo = java.time.Instant.now().minus(java.time.Duration.ofMinutes(11));
        ReflectionTestUtils.setField(bootstrapSyncService, "lastAttempt", elevenMinutesAgo);

        BootstrapSyncService.BootstrapSyncResult result = bootstrapSyncService.maybeTrigger();

        assertTrue(result.triggered());
        assertEquals("bootstrap-triggered", result.reason());
        verify(portfolioProjectRepository).count();
        // Note: Can't verify async calls directly
    }

    @Test
    public void testSyncBlockedWithinCooldownPeriod() {
        when(portfolioProjectRepository.count()).thenReturn(0L);

        // Set last sync attempt to 5 minutes ago (within 10-minute cooldown)
        java.time.Instant fiveMinutesAgo = java.time.Instant.now().minus(java.time.Duration.ofMinutes(5));
        ReflectionTestUtils.setField(bootstrapSyncService, "lastAttempt", fiveMinutesAgo);

        BootstrapSyncService.BootstrapSyncResult result = bootstrapSyncService.maybeTrigger();

        assertFalse(result.triggered());
        assertTrue(result.reason().startsWith("cooldown-active-"));
        verify(portfolioProjectRepository).count();
        verifyNoInteractions(syncSchedulerService);
    }
}
