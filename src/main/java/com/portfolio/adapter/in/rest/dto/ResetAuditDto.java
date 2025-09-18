package com.portfolio.adapter.in.rest.dto;

import com.portfolio.core.domain.admin.ResetStatus;

import java.time.LocalDateTime;

public class ResetAuditDto {
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

    public ResetAuditDto() {}

    public ResetAuditDto(Long id, String jobId, LocalDateTime startedAt, LocalDateTime finishedAt, String startedBy,
                         ResetStatus status, String errorMessage, Integer tablesCleared, Long durationMs,
                         String ipAddress, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.jobId = jobId;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.startedBy = startedBy;
        this.status = status;
        this.errorMessage = errorMessage;
        this.tablesCleared = tablesCleared;
        this.durationMs = durationMs;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }
    public String getStartedBy() { return startedBy; }
    public void setStartedBy(String startedBy) { this.startedBy = startedBy; }
    public ResetStatus getStatus() { return status; }
    public void setStatus(ResetStatus status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Integer getTablesCleared() { return tablesCleared; }
    public void setTablesCleared(Integer tablesCleared) { this.tablesCleared = tablesCleared; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

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
        public ResetAuditDto build() {
            return new ResetAuditDto(id, jobId, startedAt, finishedAt, startedBy, status, errorMessage,
                    tablesCleared, durationMs, ipAddress, createdAt, updatedAt);
        }
    }
}
