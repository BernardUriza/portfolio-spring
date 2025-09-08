package com.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncConfigDto {
    
    private Boolean enabled;
    private Integer intervalHours;
    private Instant lastRunAt;
    private Instant nextRunAt;
    private Instant updatedAt;
    private String updatedBy;
}