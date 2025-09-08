package com.portfolio.core.domain.admin;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ResetAuditTest {

    @Test
    void start_ShouldCreateResetAuditWithStartedStatus() {
        // Given
        String jobId = "test-job-id";
        String startedBy = "admin";
        String ipAddress = "192.168.1.1";
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

        // When
        ResetAudit resetAudit = ResetAudit.start(jobId, startedBy, ipAddress);

        // Then
        assertThat(resetAudit.getJobId()).isEqualTo(jobId);
        assertThat(resetAudit.getStartedBy()).isEqualTo(startedBy);
        assertThat(resetAudit.getIpAddress()).isEqualTo(ipAddress);
        assertThat(resetAudit.getStatus()).isEqualTo(ResetStatus.STARTED);
        assertThat(resetAudit.getStartedAt()).isAfter(beforeCreation);
        assertThat(resetAudit.getFinishedAt()).isNull();
        assertThat(resetAudit.getErrorMessage()).isNull();
        assertThat(resetAudit.getTablesCleared()).isNull();
        assertThat(resetAudit.getDurationMs()).isNull();
        assertThat(resetAudit.getCreatedAt()).isAfter(beforeCreation);
        assertThat(resetAudit.getUpdatedAt()).isAfter(beforeCreation);
    }

    @Test
    void complete_ShouldUpdateStatusAndSetCompletionFields() {
        // Given
        ResetAudit resetAudit = ResetAudit.start("job-id", "admin", "192.168.1.1");
        int tablesCleared = 5;
        LocalDateTime beforeCompletion = LocalDateTime.now().minusSeconds(1);

        // When
        ResetAudit completedAudit = resetAudit.complete(tablesCleared);

        // Then
        assertThat(completedAudit.getStatus()).isEqualTo(ResetStatus.COMPLETED);
        assertThat(completedAudit.getTablesCleared()).isEqualTo(tablesCleared);
        assertThat(completedAudit.getFinishedAt()).isAfter(beforeCompletion);
        assertThat(completedAudit.getDurationMs()).isNotNull();
        assertThat(completedAudit.getDurationMs()).isGreaterThanOrEqualTo(0);
        assertThat(completedAudit.getErrorMessage()).isNull();
    }

    @Test
    void fail_ShouldUpdateStatusAndSetErrorMessage() {
        // Given
        ResetAudit resetAudit = ResetAudit.start("job-id", "admin", "192.168.1.1");
        String errorMessage = "Database connection failed";
        LocalDateTime beforeFailure = LocalDateTime.now().minusSeconds(1);

        // When
        ResetAudit failedAudit = resetAudit.fail(errorMessage);

        // Then
        assertThat(failedAudit.getStatus()).isEqualTo(ResetStatus.FAILED);
        assertThat(failedAudit.getErrorMessage()).isEqualTo(errorMessage);
        assertThat(failedAudit.getFinishedAt()).isAfter(beforeFailure);
        assertThat(failedAudit.getDurationMs()).isNotNull();
        assertThat(failedAudit.getDurationMs()).isGreaterThanOrEqualTo(0);
        assertThat(failedAudit.getTablesCleared()).isNull();
    }

    @Test
    void complete_ShouldCalculateDurationCorrectly() throws InterruptedException {
        // Given
        ResetAudit resetAudit = ResetAudit.start("job-id", "admin", "192.168.1.1");
        
        // Simulate some processing time
        Thread.sleep(10);

        // When
        ResetAudit completedAudit = resetAudit.complete(3);

        // Then
        assertThat(completedAudit.getDurationMs()).isGreaterThanOrEqualTo(10);
    }

    @Test
    void fail_ShouldCalculateDurationCorrectly() throws InterruptedException {
        // Given
        ResetAudit resetAudit = ResetAudit.start("job-id", "admin", "192.168.1.1");
        
        // Simulate some processing time
        Thread.sleep(10);

        // When
        ResetAudit failedAudit = resetAudit.fail("Error occurred");

        // Then
        assertThat(failedAudit.getDurationMs()).isGreaterThanOrEqualTo(10);
    }

    @Test
    void builder_ShouldCreateResetAuditWithAllFields() {
        // Given
        String jobId = "test-job-id";
        String startedBy = "admin";
        String ipAddress = "192.168.1.1";
        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime finishedAt = LocalDateTime.now().plusMinutes(1);
        ResetStatus status = ResetStatus.COMPLETED;
        String errorMessage = null;
        Integer tablesCleared = 5;
        Long durationMs = 60000L;
        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(5);
        LocalDateTime updatedAt = LocalDateTime.now();

        // When
        ResetAudit resetAudit = ResetAudit.builder()
                .jobId(jobId)
                .startedBy(startedBy)
                .ipAddress(ipAddress)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .status(status)
                .errorMessage(errorMessage)
                .tablesCleared(tablesCleared)
                .durationMs(durationMs)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // Then
        assertThat(resetAudit.getJobId()).isEqualTo(jobId);
        assertThat(resetAudit.getStartedBy()).isEqualTo(startedBy);
        assertThat(resetAudit.getIpAddress()).isEqualTo(ipAddress);
        assertThat(resetAudit.getStartedAt()).isEqualTo(startedAt);
        assertThat(resetAudit.getFinishedAt()).isEqualTo(finishedAt);
        assertThat(resetAudit.getStatus()).isEqualTo(status);
        assertThat(resetAudit.getErrorMessage()).isEqualTo(errorMessage);
        assertThat(resetAudit.getTablesCleared()).isEqualTo(tablesCleared);
        assertThat(resetAudit.getDurationMs()).isEqualTo(durationMs);
        assertThat(resetAudit.getCreatedAt()).isEqualTo(createdAt);
        assertThat(resetAudit.getUpdatedAt()).isEqualTo(updatedAt);
    }
}