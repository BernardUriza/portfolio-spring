package com.portfolio.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectChangeEvent;
import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.ProjectHistoryJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.ProjectHistoryJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing project history and versioning
 * Handles creating snapshots, retrieving history, and rolling back projects
 *
 * @author Bernard Uriza Orozco
 * @since 2025-10-27
 */
@Service
public class ProjectHistoryService {

    private static final Logger log = LoggerFactory.getLogger(ProjectHistoryService.class);

    private final ProjectHistoryJpaRepository historyRepository;
    private final ObjectMapper objectMapper;

    public ProjectHistoryService(ProjectHistoryJpaRepository historyRepository, ObjectMapper objectMapper) {
        this.historyRepository = historyRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Event listener for portfolio project changes
     * Listens for PortfolioProjectChangeEvent and creates history entries
     *
     * Uses REQUIRES_NEW propagation to ensure history is saved in a separate transaction
     * This prevents history failures from rolling back the main project transaction
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onProjectChanged(PortfolioProjectChangeEvent event) {
        try {
            PortfolioProjectJpaEntity project = event.getProject();
            ProjectHistoryJpaEntity.ChangeType changeType = event.getChangeType();

            // Calculate changed fields by comparing with previous version
            List<String> changedFields = new ArrayList<>();
            if (changeType != ProjectHistoryJpaEntity.ChangeType.CREATE) {
                changedFields = calculateChangedFieldsForEvent(project);
            }

            createHistoryEntry(
                    project,
                    changeType,
                    changedFields,
                    event.getChangedBy()
            );

            log.debug("Processed project change event for project {} with change type {}",
                     project.getId(), changeType);

        } catch (Exception e) {
            log.error("CRITICAL: Failed to record project history for project {}: {}",
                     event.getProject().getId(), e.getMessage(), e);
            // Note: We don't rethrow because we're in AFTER_COMMIT phase
            // The project save has already succeeded
        }
    }

    /**
     * Calculate changed fields for event handling
     * Retrieves the latest history version and compares with current state
     */
    private List<String> calculateChangedFieldsForEvent(PortfolioProjectJpaEntity project) {
        try {
            Optional<ProjectHistoryJpaEntity> latestHistory = getLatestVersion(project.getId());
            if (latestHistory.isPresent()) {
                PortfolioProjectJpaEntity oldVersion = deserializeSnapshot(
                        latestHistory.get().getSnapshotData()
                );
                return calculateChangedFields(oldVersion, project);
            }
        } catch (Exception e) {
            log.warn("Failed to calculate changed fields for project {}: {}",
                    project.getId(), e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * Create a new history entry for a project
     */
    @Transactional
    public ProjectHistoryJpaEntity createHistoryEntry(
            PortfolioProjectJpaEntity project,
            ProjectHistoryJpaEntity.ChangeType changeType,
            List<String> changedFields,
            String changedBy) {

        try {
            // Get next version number
            Integer nextVersion = historyRepository.findLatestVersionNumber(project.getId()) + 1;

            // Serialize project to JSON
            String snapshot = objectMapper.writeValueAsString(project);

            // Create history entry
            ProjectHistoryJpaEntity history = ProjectHistoryJpaEntity.builder()
                    .projectId(project.getId())
                    .versionNumber(nextVersion)
                    .snapshotData(snapshot)
                    .changeType(changeType)
                    .changedFields(changedFields)
                    .changedBy(changedBy)
                    .build();

            ProjectHistoryJpaEntity saved = historyRepository.save(history);
            log.info("Created history entry for project {} version {}", project.getId(), nextVersion);

            return saved;

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize project {} to JSON", project.getId(), e);
            throw new RuntimeException("Failed to create history entry", e);
        }
    }

    /**
     * Get all history for a project
     */
    @Transactional(readOnly = true)
    public List<ProjectHistoryJpaEntity> getProjectHistory(Long projectId) {
        return historyRepository.findByProjectIdOrderByVersionNumberDesc(projectId);
    }

    /**
     * Get paginated history for a project
     */
    @Transactional(readOnly = true)
    public Page<ProjectHistoryJpaEntity> getProjectHistory(Long projectId, Pageable pageable) {
        return historyRepository.findByProjectIdOrderByVersionNumberDesc(projectId, pageable);
    }

    /**
     * Get a specific version of a project
     */
    @Transactional(readOnly = true)
    public Optional<ProjectHistoryJpaEntity> getProjectVersion(Long projectId, Integer versionNumber) {
        return historyRepository.findByProjectIdAndVersionNumber(projectId, versionNumber);
    }

    /**
     * Get the latest version of a project from history
     */
    @Transactional(readOnly = true)
    public Optional<ProjectHistoryJpaEntity> getLatestVersion(Long projectId) {
        return historyRepository.findTopByProjectIdOrderByVersionNumberDesc(projectId);
    }

    /**
     * Deserialize a snapshot back to a PortfolioProjectJpaEntity
     */
    public PortfolioProjectJpaEntity deserializeSnapshot(String snapshotData) {
        try {
            return objectMapper.readValue(snapshotData, PortfolioProjectJpaEntity.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize project snapshot", e);
            throw new RuntimeException("Failed to deserialize snapshot", e);
        }
    }

    /**
     * Calculate diff between two versions
     */
    public List<String> calculateChangedFields(PortfolioProjectJpaEntity oldVersion, PortfolioProjectJpaEntity newVersion) {
        List<String> changedFields = new java.util.ArrayList<>();

        // Compare all fields
        if (!java.util.Objects.equals(oldVersion.getTitle(), newVersion.getTitle())) {
            changedFields.add("title");
        }
        if (!java.util.Objects.equals(oldVersion.getDescription(), newVersion.getDescription())) {
            changedFields.add("description");
        }
        if (!java.util.Objects.equals(oldVersion.getLink(), newVersion.getLink())) {
            changedFields.add("link");
        }
        if (!java.util.Objects.equals(oldVersion.getGithubRepo(), newVersion.getGithubRepo())) {
            changedFields.add("githubRepo");
        }
        if (!java.util.Objects.equals(oldVersion.getStatus(), newVersion.getStatus())) {
            changedFields.add("status");
        }
        if (!java.util.Objects.equals(oldVersion.getType(), newVersion.getType())) {
            changedFields.add("type");
        }
        if (!java.util.Objects.equals(oldVersion.getCompletionStatus(), newVersion.getCompletionStatus())) {
            changedFields.add("completionStatus");
        }
        if (!java.util.Objects.equals(oldVersion.getPriority(), newVersion.getPriority())) {
            changedFields.add("priority");
        }
        if (!java.util.Objects.equals(oldVersion.getMainTechnologies(), newVersion.getMainTechnologies())) {
            changedFields.add("mainTechnologies");
        }
        if (!java.util.Objects.equals(oldVersion.getSkillIds(), newVersion.getSkillIds())) {
            changedFields.add("skillIds");
        }
        if (!java.util.Objects.equals(oldVersion.getExperienceIds(), newVersion.getExperienceIds())) {
            changedFields.add("experienceIds");
        }
        if (!java.util.Objects.equals(oldVersion.getSourceRepositoryId(), newVersion.getSourceRepositoryId())) {
            changedFields.add("sourceRepositoryId");
        }

        return changedFields;
    }

    /**
     * Count total versions for a project
     */
    @Transactional(readOnly = true)
    public Long countVersions(Long projectId) {
        return historyRepository.countByProjectId(projectId);
    }

    /**
     * Delete all history for a project
     */
    @Transactional
    public void deleteProjectHistory(Long projectId) {
        historyRepository.deleteByProjectId(projectId);
        log.info("Deleted all history for project {}", projectId);
    }
}
