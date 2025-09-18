package com.portfolio.adapter.out.persistence.jpa;

import com.portfolio.core.domain.admin.ResetStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reset_audit",
       indexes = {
           @Index(name = "idx_reset_job_id", columnList = "job_id"),
           @Index(name = "idx_reset_status", columnList = "status"),
           @Index(name = "idx_reset_started_at", columnList = "started_at"),
           @Index(name = "idx_reset_status_started", columnList = "status, started_at")
       })
public class ResetAuditJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "job_id", nullable = false, unique = true, length = 36)
    private String jobId;
    
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;
    
    @Column(name = "started_by", nullable = false, length = 100)
    private String startedBy;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ResetStatus status;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @Column(name = "tables_cleared")
    private Integer tablesCleared;
    
    @Column(name = "duration_ms")
    private Long durationMs;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public ResetAuditJpaEntity() {}

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
        private final ResetAuditJpaEntity e = new ResetAuditJpaEntity();
        public Builder id(Long v) { e.setId(v); return this; }
        public Builder jobId(String v) { e.setJobId(v); return this; }
        public Builder startedAt(LocalDateTime v) { e.setStartedAt(v); return this; }
        public Builder finishedAt(LocalDateTime v) { e.setFinishedAt(v); return this; }
        public Builder startedBy(String v) { e.setStartedBy(v); return this; }
        public Builder status(ResetStatus v) { e.setStatus(v); return this; }
        public Builder errorMessage(String v) { e.setErrorMessage(v); return this; }
        public Builder tablesCleared(Integer v) { e.setTablesCleared(v); return this; }
        public Builder durationMs(Long v) { e.setDurationMs(v); return this; }
        public Builder ipAddress(String v) { e.setIpAddress(v); return this; }
        public Builder createdAt(LocalDateTime v) { e.setCreatedAt(v); return this; }
        public Builder updatedAt(LocalDateTime v) { e.setUpdatedAt(v); return this; }
        public ResetAuditJpaEntity build() { return e; }
    }
}
