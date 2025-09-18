package com.portfolio.dto;

import com.portfolio.adapter.out.persistence.jpa.SyncConfigJpaEntity;
import java.time.Instant;

public class SyncConfigDto {

    private Boolean enabled;
    private Integer intervalHours;
    private Instant lastRunAt;
    private Instant nextRunAt;
    private Instant updatedAt;
    private String updatedBy;

    public SyncConfigDto() {}

    public SyncConfigDto(Boolean enabled, Integer intervalHours, Instant lastRunAt,
                         Instant nextRunAt, Instant updatedAt, String updatedBy) {
        this.enabled = enabled;
        this.intervalHours = intervalHours;
        this.lastRunAt = lastRunAt;
        this.nextRunAt = nextRunAt;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public static SyncConfigDto of(SyncConfigJpaEntity e) {
        return new SyncConfigDto(
            e.getEnabled(),
            e.getIntervalHours(),
            e.getLastRunAt(),
            e.getNextRunAt(),
            e.getUpdatedAt(),
            e.getUpdatedBy()
        );
    }

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
}
