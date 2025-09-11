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
        assertNotNull(response.getBody());
        assertTrue(response.getBody().triggered());
        assertEquals("portfolio-empty", response.getBody().reason());
        
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
        assertNotNull(response.getBody());
        assertFalse(response.getBody().triggered());
        assertEquals("portfolio-has-projects", response.getBody().reason());
        
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
        assertNotNull(response.getBody());
        assertFalse(response.getBody().triggered());
        assertEquals("cooldown-active", response.getBody().reason());
        
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
        assertNotNull(response.getBody());
        assertFalse(response.getBody().triggered());
        assertEquals("sync-in-progress", response.getBody().reason());
        
        verify(bootstrapSyncService).maybeTrigger();
    }
}