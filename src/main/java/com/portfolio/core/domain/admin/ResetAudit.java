package com.portfolio.core.domain.admin;

import com.portfolio.core.domain.shared.DomainEntity;

import java.time.LocalDateTime;

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

    private ResetAudit(Builder b) {
        this.id = b.id;
        this.jobId = b.jobId;
        this.startedAt = b.startedAt;
        this.finishedAt = b.finishedAt;
        this.startedBy = b.startedBy;
        this.status = b.status;
        this.errorMessage = b.errorMessage;
        this.tablesCleared = b.tablesCleared;
        this.durationMs = b.durationMs;
        this.ipAddress = b.ipAddress;
        this.createdAt = b.createdAt;
        this.updatedAt = b.updatedAt;
    }

    public static Builder builder() { return new Builder(); }
    public Builder toBuilder() { return new Builder(this); }

    public Long getId() { return id; }
    public String getJobId() { return jobId; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public String getStartedBy() { return startedBy; }
    public ResetStatus getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public Integer getTablesCleared() { return tablesCleared; }
    public Long getDurationMs() { return durationMs; }
    public String getIpAddress() { return ipAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
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

    public static final class Builder {
        private Long id;
        private String jobId;
        private LocalDateTime startedAt;
        private LocalDateTime finishedAt;
        private String startedBy;
        private ResetStatus status;
        private String errorMessage;
        private Integer tablesCleared;
        private Long durationMs;
        private String ipAddress;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder() {}
        public Builder(ResetAudit a) {
            this.id = a.id;
            this.jobId = a.jobId;
            this.startedAt = a.startedAt;
            this.finishedAt = a.finishedAt;
            this.startedBy = a.startedBy;
            this.status = a.status;
            this.errorMessage = a.errorMessage;
            this.tablesCleared = a.tablesCleared;
            this.durationMs = a.durationMs;
            this.ipAddress = a.ipAddress;
            this.createdAt = a.createdAt;
            this.updatedAt = a.updatedAt;
        }
        public Builder id(Long id) { this.id = id; return this; }
        public Builder jobId(String jobId) { this.jobId = jobId; return this; }
        public Builder startedAt(LocalDateTime startedAt) { this.startedAt = startedAt; return this; }
        public Builder finishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; return this; }
        public Builder startedBy(String startedBy) { this.startedBy = startedBy; return this; }
        public Builder status(ResetStatus status) { this.status = status; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public Builder tablesCleared(Integer tablesCleared) { this.tablesCleared = tablesCleared; return this; }
        public Builder durationMs(Long durationMs) { this.durationMs = durationMs; return this; }
        public Builder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public ResetAudit build() { return new ResetAudit(this); }
    }
}
