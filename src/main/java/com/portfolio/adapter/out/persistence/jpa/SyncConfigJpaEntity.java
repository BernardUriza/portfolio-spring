package com.portfolio.adapter.out.persistence.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;

@Entity
@Table(name = "sync_config",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_sync_config_singleton", columnNames = {"singleton_key"})
       })
public class SyncConfigJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean enabled = false;

    @Column(name = "interval_hours", nullable = false)
    @Min(1)
    @Max(168)
    private Integer intervalHours = 6;

    @Column(name = "last_run_at")
    private Instant lastRunAt;

    @Column(name = "next_run_at")
    private Instant nextRunAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy = "admin";

    @Column(name = "singleton_key", nullable = false, length = 1)
    private String singletonKey = "X";

    public SyncConfigJpaEntity() {
    }

    public SyncConfigJpaEntity(Long id, Boolean enabled, Integer intervalHours, Instant lastRunAt, Instant nextRunAt,
                               Instant updatedAt, String updatedBy, String singletonKey) {
        this.id = id;
        this.enabled = enabled;
        this.intervalHours = intervalHours;
        this.lastRunAt = lastRunAt;
        this.nextRunAt = nextRunAt;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.singletonKey = singletonKey;
    }

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = Instant.now();
        if (this.singletonKey == null || this.singletonKey.isBlank()) {
            this.singletonKey = "X";
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Integer getIntervalHours() { return intervalHours; }
    public void setIntervalHours(Integer intervalHours) { this.intervalHours = intervalHours; }

    public Instant getLastRunAt() { return lastRunAt; }
    public void setLastRunAt(Instant lastRunAt) { this.lastRunAt = lastRunAt; }

    public Instant getNextRunAt() { return nextRunAt; }
    public void setNextRunAt(Instant nextRunAt) { this.nextRunAt = nextRunAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public String getSingletonKey() { return singletonKey; }
    public void setSingletonKey(String singletonKey) { this.singletonKey = singletonKey; }

    public static class Builder {
        private final SyncConfigJpaEntity e = new SyncConfigJpaEntity();
        public Builder enabled(Boolean v){ e.setEnabled(v); return this; }
        public Builder intervalHours(Integer v){ e.setIntervalHours(v); return this; }
        public Builder lastRunAt(Instant v){ e.setLastRunAt(v); return this; }
        public Builder nextRunAt(Instant v){ e.setNextRunAt(v); return this; }
        public Builder updatedAt(Instant v){ e.setUpdatedAt(v); return this; }
        public Builder updatedBy(String v){ e.setUpdatedBy(v); return this; }
        public Builder singletonKey(String v){ e.setSingletonKey(v); return this; }
        public SyncConfigJpaEntity build(){ return e; }
    }
    public static Builder builder(){ return new Builder(); }
}
