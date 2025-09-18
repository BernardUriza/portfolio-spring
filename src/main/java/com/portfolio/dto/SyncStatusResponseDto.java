/**
 * Creado por Bernard Orozco
 * DTO for sync status response data
 */
package com.portfolio.dto;

import java.time.Instant;

public class SyncStatusResponseDto {

    private Boolean running;
    private Instant lastRunAt;
    private Instant nextRunAt;

    // Default constructor
    public SyncStatusResponseDto() {
    }

    // All args constructor
    public SyncStatusResponseDto(Boolean running, Instant lastRunAt, Instant nextRunAt) {
        this.running = running;
        this.lastRunAt = lastRunAt;
        this.nextRunAt = nextRunAt;
    }

    // Builder pattern
    public static SyncStatusResponseDtoBuilder builder() {
        return new SyncStatusResponseDtoBuilder();
    }

    // Getters and Setters
    public Boolean getRunning() {
        return running;
    }

    public void setRunning(Boolean running) {
        this.running = running;
    }

    public Instant getLastRunAt() {
        return lastRunAt;
    }

    public void setLastRunAt(Instant lastRunAt) {
        this.lastRunAt = lastRunAt;
    }

    public Instant getNextRunAt() {
        return nextRunAt;
    }

    public void setNextRunAt(Instant nextRunAt) {
        this.nextRunAt = nextRunAt;
    }

    // Builder class
    public static class SyncStatusResponseDtoBuilder {
        private Boolean running;
        private Instant lastRunAt;
        private Instant nextRunAt;

        public SyncStatusResponseDtoBuilder running(Boolean running) {
            this.running = running;
            return this;
        }

        public SyncStatusResponseDtoBuilder lastRunAt(Instant lastRunAt) {
            this.lastRunAt = lastRunAt;
            return this;
        }

        public SyncStatusResponseDtoBuilder nextRunAt(Instant nextRunAt) {
            this.nextRunAt = nextRunAt;
            return this;
        }

        public SyncStatusResponseDto build() {
            return new SyncStatusResponseDto(running, lastRunAt, nextRunAt);
        }
    }
}