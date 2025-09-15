/**
 * Creado por Bernard Orozco
 * Tests for bootstrap controller
 */
package com.portfolio.controller;

import com.portfolio.service.BootstrapSyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BootstrapControllerTest {

    @InjectMocks
    private BootstrapController bootstrapController;

    @Mock
    private BootstrapSyncService bootstrapSyncService;

    @Test
    public void testSyncIfEmptyWhenTriggered() {
        BootstrapSyncService.BootstrapSyncResult mockResult = 
            new BootstrapSyncService.BootstrapSyncResult(true, "portfolio-empty");
        
        when(bootstrapSyncService.maybeTrigger()).thenReturn(mockResult);

        ResponseEntity<BootstrapSyncService.BootstrapSyncResult> response = 
            bootstrapController.syncIfEmpty();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        BootstrapSyncService.BootstrapSyncResult body1 = java.util.Objects.requireNonNull(response.getBody());
        assertTrue(body1.triggered());
        assertEquals("portfolio-empty", body1.reason());
        
        verify(bootstrapSyncService).maybeTrigger();
    }

    @Test
    public void testSyncIfEmptyWhenSkipped() {
        BootstrapSyncService.BootstrapSyncResult mockResult = 
            new BootstrapSyncService.BootstrapSyncResult(false, "portfolio-has-projects");
        
        when(bootstrapSyncService.maybeTrigger()).thenReturn(mockResult);

        ResponseEntity<BootstrapSyncService.BootstrapSyncResult> response = 
            bootstrapController.syncIfEmpty();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        BootstrapSyncService.BootstrapSyncResult body2 = java.util.Objects.requireNonNull(response.getBody());
        assertFalse(body2.triggered());
        assertEquals("portfolio-has-projects", body2.reason());
        
        verify(bootstrapSyncService).maybeTrigger();
    }

    @Test
    public void testSyncIfEmptyDuringCooldown() {
        BootstrapSyncService.BootstrapSyncResult mockResult = 
            new BootstrapSyncService.BootstrapSyncResult(false, "cooldown-active");
        
        when(bootstrapSyncService.maybeTrigger()).thenReturn(mockResult);

        ResponseEntity<BootstrapSyncService.BootstrapSyncResult> response = 
            bootstrapController.syncIfEmpty();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        BootstrapSyncService.BootstrapSyncResult body3 = java.util.Objects.requireNonNull(response.getBody());
        assertFalse(body3.triggered());
        assertEquals("cooldown-active", body3.reason());
        
        verify(bootstrapSyncService).maybeTrigger();
    }

    @Test
    public void testSyncIfEmptyDuringInProgress() {
        BootstrapSyncService.BootstrapSyncResult mockResult = 
            new BootstrapSyncService.BootstrapSyncResult(false, "sync-in-progress");
        
        when(bootstrapSyncService.maybeTrigger()).thenReturn(mockResult);

        ResponseEntity<BootstrapSyncService.BootstrapSyncResult> response = 
            bootstrapController.syncIfEmpty();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        BootstrapSyncService.BootstrapSyncResult body4 = java.util.Objects.requireNonNull(response.getBody());
        assertFalse(body4.triggered());
        assertEquals("sync-in-progress", body4.reason());
        
        verify(bootstrapSyncService).maybeTrigger();
    }
}
