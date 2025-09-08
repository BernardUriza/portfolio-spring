package com.portfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.dto.SyncConfigDto;
import com.portfolio.dto.SyncConfigUpdateDto;
import com.portfolio.dto.SyncStatusResponseDto;
import com.portfolio.service.SyncConfigService;
import com.portfolio.service.SyncSchedulerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SyncConfigAdminController.class)
class SyncConfigAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SyncConfigService syncConfigService;

    @MockBean
    private SyncSchedulerService syncSchedulerService;

    @Test
    void getSyncConfig_ShouldReturnConfig() throws Exception {
        // Given
        SyncConfigDto config = SyncConfigDto.builder()
                .enabled(true)
                .intervalHours(6)
                .lastRunAt(Instant.now().minusSeconds(3600))
                .nextRunAt(Instant.now().plusSeconds(3600))
                .build();
        
        when(syncConfigService.getOrCreate()).thenReturn(config);

        // When & Then
        mockMvc.perform(get("/api/admin/sync-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.intervalHours").value(6))
                .andExpect(jsonPath("$.lastRunAt").exists())
                .andExpect(jsonPath("$.nextRunAt").exists());
    }

    @Test
    void updateSyncConfig_WithValidData_ShouldUpdateAndReschedule() throws Exception {
        // Given
        SyncConfigUpdateDto updateDto = SyncConfigUpdateDto.builder()
                .enabled(true)
                .intervalHours(12)
                .build();

        SyncConfigDto updatedConfig = SyncConfigDto.builder()
                .enabled(true)
                .intervalHours(12)
                .build();

        when(syncConfigService.update(eq(true), eq(12), eq("admin")))
                .thenReturn(updatedConfig);

        // When & Then
        mockMvc.perform(put("/api/admin/sync-config")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.intervalHours").value(12));

        verify(syncConfigService).update(true, 12, "admin");
        verify(syncSchedulerService).reschedule();
    }

    @Test
    void updateSyncConfig_WithInvalidHours_ShouldReturn400() throws Exception {
        // Given
        SyncConfigUpdateDto invalidDto = SyncConfigUpdateDto.builder()
                .enabled(true)
                .intervalHours(200) // Invalid: > 168
                .build();

        // When & Then
        mockMvc.perform(put("/api/admin/sync-config")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(syncConfigService, never()).update(anyBoolean(), anyInt(), anyString());
        verify(syncSchedulerService, never()).reschedule();
    }

    @Test
    void updateSyncConfig_WithNullEnabled_ShouldReturn400() throws Exception {
        // Given
        SyncConfigUpdateDto invalidDto = SyncConfigUpdateDto.builder()
                .intervalHours(6)
                // enabled is null
                .build();

        // When & Then
        mockMvc.perform(put("/api/admin/sync-config")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(syncConfigService, never()).update(anyBoolean(), anyInt(), anyString());
        verify(syncSchedulerService, never()).reschedule();
    }

    @Test
    void runSyncNow_WhenNotRunning_ShouldAccept() throws Exception {
        // Given
        // Nothing to mock - service will execute normally

        // When & Then
        mockMvc.perform(post("/api/admin/sync-config/run-now"))
                .andExpect(status().isAccepted());

        verify(syncSchedulerService).runOnceNow();
    }

    @Test
    void runSyncNow_WhenAlreadyRunning_ShouldReturn409() throws Exception {
        // Given
        doThrow(new IllegalStateException("Sync is already in progress"))
                .when(syncSchedulerService).runOnceNow();

        // When & Then
        mockMvc.perform(post("/api/admin/sync-config/run-now"))
                .andExpect(status().isConflict());
    }

    @Test
    void getSyncStatus_ShouldReturnStatus() throws Exception {
        // Given
        Instant lastRun = Instant.now().minusSeconds(3600);
        Instant nextRun = Instant.now().plusSeconds(3600);
        
        when(syncSchedulerService.isRunning()).thenReturn(false);
        when(syncSchedulerService.getLastRunTime()).thenReturn(lastRun);
        when(syncSchedulerService.getNextRunTime()).thenReturn(nextRun);

        // When & Then
        mockMvc.perform(get("/api/admin/sync-config/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.running").value(false))
                .andExpect(jsonPath("$.lastRunAt").exists())
                .andExpect(jsonPath("$.nextRunAt").exists());
    }
}