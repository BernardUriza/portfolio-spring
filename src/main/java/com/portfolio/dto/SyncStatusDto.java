/**
 * Creado por Bernard Orozco
 * DTO for sync status information
 */
package com.portfolio.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SyncStatusDto {
    private LocalDateTime lastSync;
    private long timeUntilNextSync;
    private int totalGitHubProjects;
    private int totalDatabaseProjects;
    private List<UnsyncedProjectDto> unsyncedProjects;
    private boolean syncInProgress;

    // Default constructor
    public SyncStatusDto() {
    }

    // All args constructor
    public SyncStatusDto(LocalDateTime lastSync, long timeUntilNextSync, int totalGitHubProjects,
                         int totalDatabaseProjects, List<UnsyncedProjectDto> unsyncedProjects,
                         boolean syncInProgress) {
        this.lastSync = lastSync;
        this.timeUntilNextSync = timeUntilNextSync;
        this.totalGitHubProjects = totalGitHubProjects;
        this.totalDatabaseProjects = totalDatabaseProjects;
        this.unsyncedProjects = unsyncedProjects;
        this.syncInProgress = syncInProgress;
    }

    // Builder pattern
    public static SyncStatusDtoBuilder builder() {
        return new SyncStatusDtoBuilder();
    }

    // Getters and Setters
    public LocalDateTime getLastSync() {
        return lastSync;
    }

    public void setLastSync(LocalDateTime lastSync) {
        this.lastSync = lastSync;
    }

    public long getTimeUntilNextSync() {
        return timeUntilNextSync;
    }

    public void setTimeUntilNextSync(long timeUntilNextSync) {
        this.timeUntilNextSync = timeUntilNextSync;
    }

    public int getTotalGitHubProjects() {
        return totalGitHubProjects;
    }

    public void setTotalGitHubProjects(int totalGitHubProjects) {
        this.totalGitHubProjects = totalGitHubProjects;
    }

    public int getTotalDatabaseProjects() {
        return totalDatabaseProjects;
    }

    public void setTotalDatabaseProjects(int totalDatabaseProjects) {
        this.totalDatabaseProjects = totalDatabaseProjects;
    }

    public List<UnsyncedProjectDto> getUnsyncedProjects() {
        return unsyncedProjects;
    }

    public void setUnsyncedProjects(List<UnsyncedProjectDto> unsyncedProjects) {
        this.unsyncedProjects = unsyncedProjects;
    }

    public boolean isSyncInProgress() {
        return syncInProgress;
    }

    public void setSyncInProgress(boolean syncInProgress) {
        this.syncInProgress = syncInProgress;
    }

    // Builder class
    public static class SyncStatusDtoBuilder {
        private LocalDateTime lastSync;
        private long timeUntilNextSync;
        private int totalGitHubProjects;
        private int totalDatabaseProjects;
        private List<UnsyncedProjectDto> unsyncedProjects;
        private boolean syncInProgress;

        public SyncStatusDtoBuilder lastSync(LocalDateTime lastSync) {
            this.lastSync = lastSync;
            return this;
        }

        public SyncStatusDtoBuilder timeUntilNextSync(long timeUntilNextSync) {
            this.timeUntilNextSync = timeUntilNextSync;
            return this;
        }

        public SyncStatusDtoBuilder totalGitHubProjects(int totalGitHubProjects) {
            this.totalGitHubProjects = totalGitHubProjects;
            return this;
        }

        public SyncStatusDtoBuilder totalDatabaseProjects(int totalDatabaseProjects) {
            this.totalDatabaseProjects = totalDatabaseProjects;
            return this;
        }

        public SyncStatusDtoBuilder unsyncedProjects(List<UnsyncedProjectDto> unsyncedProjects) {
            this.unsyncedProjects = unsyncedProjects;
            return this;
        }

        public SyncStatusDtoBuilder syncInProgress(boolean syncInProgress) {
            this.syncInProgress = syncInProgress;
            return this;
        }

        public SyncStatusDto build() {
            return new SyncStatusDto(lastSync, timeUntilNextSync, totalGitHubProjects,
                    totalDatabaseProjects, unsyncedProjects, syncInProgress);
        }
    }

    public static class UnsyncedProjectDto {
        private String id;
        private String name;
        private String reason;

        // Default constructor
        public UnsyncedProjectDto() {
        }

        // All args constructor
        public UnsyncedProjectDto(String id, String name, String reason) {
            this.id = id;
            this.name = name;
            this.reason = reason;
        }

        // Builder pattern
        public static UnsyncedProjectDtoBuilder builder() {
            return new UnsyncedProjectDtoBuilder();
        }

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        // Builder class
        public static class UnsyncedProjectDtoBuilder {
            private String id;
            private String name;
            private String reason;

            public UnsyncedProjectDtoBuilder id(String id) {
                this.id = id;
                return this;
            }

            public UnsyncedProjectDtoBuilder name(String name) {
                this.name = name;
                return this;
            }

            public UnsyncedProjectDtoBuilder reason(String reason) {
                this.reason = reason;
                return this;
            }

            public UnsyncedProjectDto build() {
                return new UnsyncedProjectDto(id, name, reason);
            }
        }
    }
}