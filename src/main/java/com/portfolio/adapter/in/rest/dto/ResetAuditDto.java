package com.portfolio.adapter.in.rest.dto;

import com.portfolio.core.domain.admin.ResetStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ResetAuditDto {
    private final Long id;
    private final String jobId;
    private final LocalDateTime startedAt;
    private final LocalDateTime finishedAt;
    private final String startedBy;
    private final ResetStatus status;
    private final String errorMessage;
    private final Integer tablesCleared;
    private final Long durationMs;
    private final String ipAddress;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}