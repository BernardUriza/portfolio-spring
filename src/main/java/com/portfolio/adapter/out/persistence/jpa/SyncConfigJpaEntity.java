package com.portfolio.adapter.out.persistence.jpa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;

@Entity
@Table(name = "sync_config",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_sync_config_singleton", columnNames = {"singleton_key"})
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncConfigJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = false;
    
    @Column(name = "interval_hours", nullable = false)
    @Min(1)
    @Max(168)
    @Builder.Default
    private Integer intervalHours = 6;
    
    @Column(name = "last_run_at")
    private Instant lastRunAt;
    
    @Column(name = "next_run_at")
    private Instant nextRunAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Column(name = "updated_by", nullable = false)
    @Builder.Default
    private String updatedBy = "admin";

    @Column(name = "singleton_key", nullable = false, length = 1)
    @Builder.Default
    private String singletonKey = "X";
    
    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = Instant.now();
        if (this.singletonKey == null || this.singletonKey.isBlank()) {
            this.singletonKey = "X";
        }
    }
}
