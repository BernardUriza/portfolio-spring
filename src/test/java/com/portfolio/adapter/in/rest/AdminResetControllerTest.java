package com.portfolio.adapter.in.rest;

import com.portfolio.adapter.in.rest.mapper.ResetAuditRestMapper;
import com.portfolio.core.domain.admin.ResetAudit;
import com.portfolio.core.domain.admin.ResetStatus;
import com.portfolio.service.FactoryResetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminResetController.class)
@TestPropertySource(properties = {
        "app.admin.factory-reset.enabled=true",
        "app.admin.factory-reset.token=test-token",
        "portfolio.admin.token=admin-test-token",
        "portfolio.admin.security.enabled=false"  // Disable security for @WebMvcTest
})
class AdminResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FactoryResetService factoryResetService;

    @MockitoBean
    private ResetAuditRestMapper resetAuditMapper;

    @Test
    void startFactoryReset_WithValidTokenAndHeaders_ShouldReturn202() throws Exception {
        // Given
        String jobId = "test-job-id";
        ResetAudit resetAudit = ResetAudit.builder()
                .jobId(jobId)
                .startedBy("admin")
                .ipAddress("192.168.1.1")
                .status(ResetStatus.STARTED)
                .startedAt(LocalDateTime.now())
                .build();

        when(factoryResetService.getActiveJobs()).thenReturn(new ArrayList<>());
        when(factoryResetService.startFactoryReset(eq("admin"), any(String.class))).thenReturn(resetAudit);

        // When & Then
        mockMvc.perform(post("/api/admin/factory-reset")
                .header("X-Admin-Token", "admin-test-token")
                .header("X-Admin-Reset-Token", "test-token")
                .header("X-Admin-Confirm", "DELETE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value(jobId))
                .andExpect(jsonPath("$.message").value("Factory reset started successfully"))
                .andExpect(jsonPath("$.streamUrl").value("/api/admin/factory-reset/stream/" + jobId));

        verify(factoryResetService).getActiveJobs();
        verify(factoryResetService).startFactoryReset(eq("admin"), any(String.class));
    }

    @Test
    void startFactoryReset_WithInvalidToken_ShouldReturn403() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/admin/factory-reset")
                .header("X-Admin-Reset-Token", "invalid-token")
                .header("X-Admin-Confirm", "DELETE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(factoryResetService, never()).startFactoryReset(any(), any());
    }

    @Test
    void startFactoryReset_WithMissingToken_ShouldReturn403() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/admin/factory-reset")
                .header("X-Admin-Confirm", "DELETE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(factoryResetService, never()).startFactoryReset(any(), any());
    }

    @Test
    void startFactoryReset_WithInvalidConfirmHeader_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/admin/factory-reset")
                .header("X-Admin-Token", "admin-test-token")
                .header("X-Admin-Reset-Token", "test-token")
                .header("X-Admin-Confirm", "INVALID")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(factoryResetService, never()).startFactoryReset(any(), any());
    }

    @Test
    void startFactoryReset_WithMissingConfirmHeader_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/admin/factory-reset")
                .header("X-Admin-Token", "admin-test-token")
                .header("X-Admin-Reset-Token", "test-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(factoryResetService, never()).startFactoryReset(any(), any());
    }

    @Test
    void startFactoryReset_WithActiveJob_ShouldReturn409() throws Exception {
        // Given
        ResetAudit activeJob = ResetAudit.builder()
                .jobId("active-job-id")
                .status(ResetStatus.STARTED)
                .build();

        when(factoryResetService.getActiveJobs()).thenReturn(List.of(activeJob));

        // When & Then
        mockMvc.perform(post("/api/admin/factory-reset")
                .header("X-Admin-Token", "admin-test-token")
                .header("X-Admin-Reset-Token", "test-token")
                .header("X-Admin-Confirm", "DELETE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.jobId").value("active-job-id"))
                .andExpect(jsonPath("$.message").value("Factory reset already in progress"));

        verify(factoryResetService).getActiveJobs();
        verify(factoryResetService, never()).startFactoryReset(any(), any());
    }

    @Test
    void startFactoryReset_WithRateLimitExceeded_ShouldReturn429() throws Exception {
        // Given - Make multiple requests quickly to trigger rate limiting
        when(factoryResetService.getActiveJobs()).thenReturn(new ArrayList<>());

        String jobId = "test-job-id";
        ResetAudit resetAudit = ResetAudit.builder()
                .jobId(jobId)
                .startedBy("admin")
                .status(ResetStatus.STARTED)
                .build();
        when(factoryResetService.startFactoryReset(eq("admin"), any(String.class))).thenReturn(resetAudit);

        // First request should succeed
        mockMvc.perform(post("/api/admin/factory-reset")
                .header("X-Admin-Token", "admin-test-token")
                .header("X-Admin-Reset-Token", "test-token")
                .header("X-Admin-Confirm", "DELETE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        // Second request should be rate limited
        mockMvc.perform(post("/api/admin/factory-reset")
                .header("X-Admin-Token", "admin-test-token")
                .header("X-Admin-Reset-Token", "test-token")
                .header("X-Admin-Confirm", "DELETE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void streamFactoryResetProgress_WithValidJobId_ShouldReturnSseEmitter() throws Exception {
        // Given
        String jobId = "test-job-id";
        ResetAudit resetAudit = ResetAudit.builder()
                .jobId(jobId)
                .status(ResetStatus.STARTED)
                .build();

        SseEmitter mockEmitter = new SseEmitter();

        when(factoryResetService.getResetAuditByJobId(jobId)).thenReturn(resetAudit);
        when(factoryResetService.streamResetProgress(jobId)).thenReturn(mockEmitter);

        // When & Then
        mockMvc.perform(get("/api/admin/factory-reset/stream/" + jobId)
                .header("X-Admin-Token", "admin-test-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/event-stream;charset=UTF-8"));

        verify(factoryResetService).getResetAuditByJobId(jobId);
        verify(factoryResetService).streamResetProgress(jobId);
    }

    @Test
    void streamFactoryResetProgress_WithInvalidJobId_ShouldReturn404() throws Exception {
        // Given
        String jobId = "invalid-job-id";
        when(factoryResetService.getResetAuditByJobId(jobId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/admin/factory-reset/stream/" + jobId)
                .header("X-Admin-Token", "admin-test-token"))
                .andExpect(status().isNotFound());

        verify(factoryResetService).getResetAuditByJobId(jobId);
        verify(factoryResetService, never()).streamResetProgress(any());
    }

    @Test
    void getFactoryResetAudit_ShouldReturnAuditHistory() throws Exception {
        // Given
        ResetAudit audit1 = ResetAudit.builder()
                .jobId("job1")
                .status(ResetStatus.COMPLETED)
                .build();

        ResetAudit audit2 = ResetAudit.builder()
                .jobId("job2")
                .status(ResetStatus.FAILED)
                .build();

        when(factoryResetService.getResetHistory(20)).thenReturn(List.of(audit1, audit2));

        // When & Then
        mockMvc.perform(get("/api/admin/factory-reset/audit")
                .header("X-Admin-Token", "admin-test-token"))
                .andExpect(status().isOk());

        verify(factoryResetService).getResetHistory(20);
        verify(resetAuditMapper, times(2)).toRestDto(any(ResetAudit.class));
    }

    @Test
    void getFactoryResetAudit_WithCustomLimit_ShouldUseProvidedLimit() throws Exception {
        // Given
        int customLimit = 10;
        when(factoryResetService.getResetHistory(customLimit)).thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(get("/api/admin/factory-reset/audit")
                .header("X-Admin-Token", "admin-test-token")
                .param("limit", String.valueOf(customLimit)))
                .andExpect(status().isOk());

        verify(factoryResetService).getResetHistory(customLimit);
    }
}

@WebMvcTest(AdminResetController.class)
@TestPropertySource(properties = {
        "app.admin.factory-reset.enabled=false",
        "portfolio.admin.token=admin-test-token",
        "portfolio.admin.security.enabled=false"  // Disable security for @WebMvcTest
})
class AdminResetControllerDisabledTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FactoryResetService factoryResetService;

    @MockitoBean
    private ResetAuditRestMapper resetAuditMapper;

    @Test
    void startFactoryReset_WhenDisabled_ShouldReturn404() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/admin/factory-reset")
                .header("X-Admin-Token", "admin-test-token")
                .header("X-Admin-Reset-Token", "any-token")
                .header("X-Admin-Confirm", "DELETE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(factoryResetService, never()).startFactoryReset(any(), any());
    }

    @Test
    void streamFactoryResetProgress_WhenDisabled_ShouldReturn404() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/factory-reset/stream/any-job-id")
                .header("X-Admin-Token", "admin-test-token"))
                .andExpect(status().isNotFound());

        verify(factoryResetService, never()).streamResetProgress(any());
    }

    @Test
    void getFactoryResetAudit_WhenDisabled_ShouldReturn404() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/factory-reset/audit")
                .header("X-Admin-Token", "admin-test-token"))
                .andExpect(status().isNotFound());

        verify(factoryResetService, never()).getResetHistory(any(Integer.class));
    }
}
