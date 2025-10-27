package com.portfolio.controller;

import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaRepository;
import com.portfolio.adapter.out.persistence.jpa.ProjectHistoryJpaEntity;
import com.portfolio.service.ProjectHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin controller for project history and versioning endpoints
 * Provides API for viewing history, comparing versions, and rolling back projects
 *
 * @author Bernard Uriza Orozco
 * @since 2025-10-27
 */
@RestController
@RequestMapping("/api/admin/portfolio/{projectId}/history")
public class ProjectHistoryAdminController {
    private static final Logger log = LoggerFactory.getLogger(ProjectHistoryAdminController.class);

    private final ProjectHistoryService historyService;
    private final PortfolioProjectJpaRepository portfolioRepository;

    public ProjectHistoryAdminController(ProjectHistoryService historyService,
                                         PortfolioProjectJpaRepository portfolioRepository) {
        this.historyService = historyService;
        this.portfolioRepository = portfolioRepository;
    }

    /**
     * DTO for history entry response
     */
    public record HistoryEntryResponse(
            Long id,
            Long projectId,
            Integer versionNumber,
            String changeType,
            List<String> changedFields,
            String changedBy,
            LocalDateTime createdAt,
            boolean isUserInitiated,
            boolean isSyncGenerated
    ) {
        public static HistoryEntryResponse from(ProjectHistoryJpaEntity entity) {
            return new HistoryEntryResponse(
                    entity.getId(),
                    entity.getProjectId(),
                    entity.getVersionNumber(),
                    entity.getChangeType().name(),
                    entity.getChangedFields(),
                    entity.getChangedBy(),
                    entity.getCreatedAt(),
                    entity.isUserInitiated(),
                    entity.isSyncGenerated()
            );
        }
    }

    /**
     * DTO for version detail response (includes snapshot)
     */
    public record VersionDetailResponse(
            Long id,
            Long projectId,
            Integer versionNumber,
            String changeType,
            List<String> changedFields,
            String changedBy,
            LocalDateTime createdAt,
            String snapshotData
    ) {
        public static VersionDetailResponse from(ProjectHistoryJpaEntity entity) {
            return new VersionDetailResponse(
                    entity.getId(),
                    entity.getProjectId(),
                    entity.getVersionNumber(),
                    entity.getChangeType().name(),
                    entity.getChangedFields(),
                    entity.getChangedBy(),
                    entity.getCreatedAt(),
                    entity.getSnapshotData()
            );
        }
    }

    /**
     * DTO for field difference
     */
    public record FieldDiff(
            String fieldName,
            Object oldValue,
            Object newValue,
            boolean changed
    ) {}

    /**
     * DTO for version comparison response
     */
    public record VersionComparisonResponse(
            Integer version1,
            Integer version2,
            List<String> changedFields,
            Map<String, Object> changes
    ) {}

    /**
     * Get all history entries for a project
     * Returns paginated list ordered by version descending
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProjectHistory(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        log.info("Fetching history for project {} (page={}, size={})", projectId, page, size);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("versionNumber").descending());
        Page<ProjectHistoryJpaEntity> historyPage = historyService.getProjectHistory(projectId, pageRequest);

        List<HistoryEntryResponse> entries = historyPage.getContent().stream()
                .map(HistoryEntryResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "entries", entries,
                "pagination", Map.of(
                        "page", historyPage.getNumber(),
                        "size", historyPage.getSize(),
                        "totalElements", historyPage.getTotalElements(),
                        "totalPages", historyPage.getTotalPages()
                ),
                "totalVersions", historyService.countVersions(projectId)
        ));
    }

    /**
     * Get a specific version of a project
     * Returns the complete snapshot for that version
     */
    @GetMapping("/{versionNumber}")
    public ResponseEntity<VersionDetailResponse> getVersion(
            @PathVariable Long projectId,
            @PathVariable Integer versionNumber) {

        log.info("Fetching version {} for project {}", versionNumber, projectId);

        return historyService.getProjectVersion(projectId, versionNumber)
                .map(VersionDetailResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get the latest version from history
     */
    @GetMapping("/latest")
    public ResponseEntity<VersionDetailResponse> getLatestVersion(@PathVariable Long projectId) {
        log.info("Fetching latest version for project {}", projectId);

        return historyService.getLatestVersion(projectId)
                .map(VersionDetailResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Compare two versions of a project
     * Returns list of changed fields and their differences
     */
    @GetMapping("/compare/{version1}/to/{version2}")
    public ResponseEntity<VersionComparisonResponse> compareVersions(
            @PathVariable Long projectId,
            @PathVariable Integer version1,
            @PathVariable Integer version2) {

        log.info("Comparing versions {} and {} for project {}", version1, version2, projectId);

        var v1 = historyService.getProjectVersion(projectId, version1);
        var v2 = historyService.getProjectVersion(projectId, version2);

        if (v1.isEmpty() || v2.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PortfolioProjectJpaEntity snapshot1 = historyService.deserializeSnapshot(v1.get().getSnapshotData());
        PortfolioProjectJpaEntity snapshot2 = historyService.deserializeSnapshot(v2.get().getSnapshotData());

        List<String> changedFields = historyService.calculateChangedFields(snapshot1, snapshot2);

        return ResponseEntity.ok(new VersionComparisonResponse(
                version1,
                version2,
                changedFields,
                Map.of(
                        "message", String.format("%d fields changed between versions", changedFields.size()),
                        "fields", changedFields
                )
        ));
    }

    /**
     * Rollback a project to a previous version
     * Creates a new version with ROLLBACK change type
     */
    @PostMapping("/{versionNumber}/rollback")
    public ResponseEntity<Map<String, Object>> rollbackToVersion(
            @PathVariable Long projectId,
            @PathVariable Integer versionNumber) {

        log.info("Rolling back project {} to version {}", projectId, versionNumber);

        try {
            // Get the target version
            var targetVersion = historyService.getProjectVersion(projectId, versionNumber);
            if (targetVersion.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Version not found"));
            }

            // Get current project
            var currentProject = portfolioRepository.findById(projectId);
            if (currentProject.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Project not found"));
            }

            // Deserialize target version snapshot
            PortfolioProjectJpaEntity targetSnapshot = historyService.deserializeSnapshot(
                    targetVersion.get().getSnapshotData()
            );

            // Calculate what fields will change
            List<String> changedFields = historyService.calculateChangedFields(
                    currentProject.get(),
                    targetSnapshot
            );

            // Apply the rollback by copying all fields from snapshot to current project
            PortfolioProjectJpaEntity current = currentProject.get();
            applySnapshot(current, targetSnapshot);

            // Save the rolled-back project (this will trigger @PostUpdate listener)
            PortfolioProjectJpaEntity saved = portfolioRepository.save(current);

            // Create explicit rollback history entry
            ProjectHistoryJpaEntity rollbackHistory = historyService.createHistoryEntry(
                    saved,
                    ProjectHistoryJpaEntity.ChangeType.ROLLBACK,
                    changedFields,
                    "ADMIN"  // Could be enhanced to track actual admin user
            );

            log.info("Successfully rolled back project {} to version {}", projectId, versionNumber);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", String.format("Rolled back to version %d", versionNumber),
                    "newVersion", rollbackHistory.getVersionNumber(),
                    "changedFields", changedFields
            ));

        } catch (Exception e) {
            log.error("Failed to rollback project {} to version {}", projectId, versionNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Rollback failed: " + e.getMessage()));
        }
    }

    /**
     * Get statistics about project history
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getHistoryStats(@PathVariable Long projectId) {
        log.info("Fetching history statistics for project {}", projectId);

        List<ProjectHistoryJpaEntity> allHistory = historyService.getProjectHistory(projectId);

        long totalVersions = historyService.countVersions(projectId);
        long createCount = allHistory.stream().filter(h -> h.getChangeType() == ProjectHistoryJpaEntity.ChangeType.CREATE).count();
        long updateCount = allHistory.stream().filter(h -> h.getChangeType() == ProjectHistoryJpaEntity.ChangeType.UPDATE).count();
        long syncCount = allHistory.stream().filter(h -> h.getChangeType() == ProjectHistoryJpaEntity.ChangeType.SYNC).count();
        long manualCount = allHistory.stream().filter(h -> h.getChangeType() == ProjectHistoryJpaEntity.ChangeType.MANUAL).count();
        long rollbackCount = allHistory.stream().filter(h -> h.getChangeType() == ProjectHistoryJpaEntity.ChangeType.ROLLBACK).count();

        return ResponseEntity.ok(Map.of(
                "totalVersions", totalVersions,
                "changeTypeCounts", Map.of(
                        "CREATE", createCount,
                        "UPDATE", updateCount,
                        "SYNC", syncCount,
                        "MANUAL", manualCount,
                        "ROLLBACK", rollbackCount
                ),
                "firstVersion", allHistory.isEmpty() ? null : allHistory.get(allHistory.size() - 1).getCreatedAt(),
                "latestVersion", allHistory.isEmpty() ? null : allHistory.get(0).getCreatedAt()
        ));
    }

    /**
     * Helper method to apply snapshot data to current project
     * Copies all relevant fields while preserving ID and timestamps
     */
    private void applySnapshot(PortfolioProjectJpaEntity current, PortfolioProjectJpaEntity snapshot) {
        // Copy all editable fields from snapshot to current
        current.setTitle(snapshot.getTitle());
        current.setDescription(snapshot.getDescription());
        current.setLink(snapshot.getLink());
        current.setGithubRepo(snapshot.getGithubRepo());
        current.setStatus(snapshot.getStatus());
        current.setType(snapshot.getType());
        current.setCompletionStatus(snapshot.getCompletionStatus());
        current.setPriority(snapshot.getPriority());
        current.setMainTechnologies(snapshot.getMainTechnologies());
        current.setSkillIds(snapshot.getSkillIds());
        current.setExperienceIds(snapshot.getExperienceIds());
        current.setSourceRepositoryId(snapshot.getSourceRepositoryId());
        current.setLinkType(snapshot.getLinkType());
        current.setProtectDescription(snapshot.getProtectDescription());
        current.setProtectLiveDemoUrl(snapshot.getProtectLiveDemoUrl());
        current.setProtectSkills(snapshot.getProtectSkills());
        current.setProtectExperiences(snapshot.getProtectExperiences());
        current.setManualDescriptionOverride(snapshot.getManualDescriptionOverride());
        current.setManualLinkOverride(snapshot.getManualLinkOverride());
        current.setManualSkillsOverride(snapshot.getManualSkillsOverride());
        current.setManualExperiencesOverride(snapshot.getManualExperiencesOverride());

        // Note: We intentionally do NOT copy:
        // - id (must remain the same)
        // - createdAt (historical value)
        // - updatedAt (will be set by @UpdateTimestamp)
        // - version (will be incremented by @Version)
    }
}
