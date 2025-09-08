package com.portfolio.core.domain.admin;

import com.portfolio.core.domain.shared.DomainEntity;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class ResetAudit extends DomainEntity {
    
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
    
    public static ResetAudit start(String jobId, String startedBy, String ipAddress) {
        LocalDateTime now = LocalDateTime.now();
        return ResetAudit.builder()
                .jobId(jobId)
                .startedAt(now)
                .startedBy(startedBy)
                .ipAddress(ipAddress)
                .status(ResetStatus.STARTED)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
    
    public ResetAudit complete(Integer tablesCleared) {
        LocalDateTime now = LocalDateTime.now();
        long duration = java.time.Duration.between(this.startedAt, now).toMillis();
        
        return this.toBuilder()
                .finishedAt(now)
                .status(ResetStatus.COMPLETED)
                .tablesCleared(tablesCleared)
                .durationMs(duration)
                .updatedAt(now)
                .build();
    }
    
    public ResetAudit fail(String errorMessage) {
        LocalDateTime now = LocalDateTime.now();
        long duration = java.time.Duration.between(this.startedAt, now).toMillis();
        
        return this.toBuilder()
                .finishedAt(now)
                .status(ResetStatus.FAILED)
                .errorMessage(errorMessage)
                .durationMs(duration)
                .updatedAt(now)
                .build();
    }
    
    public boolean isInProgress() {
        return ResetStatus.STARTED.equals(this.status);
    }
    
    public boolean isCompleted() {
        return ResetStatus.COMPLETED.equals(this.status) || ResetStatus.FAILED.equals(this.status);
    }
}