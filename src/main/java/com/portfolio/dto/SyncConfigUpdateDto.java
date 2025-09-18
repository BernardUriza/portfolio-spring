package com.portfolio.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SyncConfigUpdateDto {

    @NotNull(message = "Enabled flag is required")
    private Boolean enabled;

    @NotNull(message = "Interval hours is required")
    @Min(value = 1, message = "Interval must be at least 1 hour")
    @Max(value = 168, message = "Interval must not exceed 168 hours (7 days)")
    private Integer intervalHours;

    public SyncConfigUpdateDto() {}

    public SyncConfigUpdateDto(Boolean enabled, Integer intervalHours) {
        this.enabled = enabled;
        this.intervalHours = intervalHours;
    }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Integer getIntervalHours() { return intervalHours; }
    public void setIntervalHours(Integer intervalHours) { this.intervalHours = intervalHours; }
}
