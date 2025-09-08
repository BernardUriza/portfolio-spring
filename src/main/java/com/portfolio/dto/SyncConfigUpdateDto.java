package com.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncConfigUpdateDto {
    
    @NotNull(message = "Enabled flag is required")
    private Boolean enabled;
    
    @NotNull(message = "Interval hours is required")
    @Min(value = 1, message = "Interval must be at least 1 hour")
    @Max(value = 168, message = "Interval must not exceed 168 hours (7 days)")
    private Integer intervalHours;
}