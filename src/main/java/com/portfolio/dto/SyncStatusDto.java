package com.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncStatusDto {
    private LocalDateTime lastSync;
    private long timeUntilNextSync;
    private int totalGitHubProjects;
    private int totalDatabaseProjects;
    private List<UnsyncedProjectDto> unsyncedProjects;
    private boolean syncInProgress;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnsyncedProjectDto {
        private String id;
        private String name;
        private String reason;
    }
}