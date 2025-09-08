package com.portfolio.adapter.out.persistence.jpa;

import com.portfolio.core.domain.admin.ResetStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reset_audit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}