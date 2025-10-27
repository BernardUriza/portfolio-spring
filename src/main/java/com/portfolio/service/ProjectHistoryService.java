package com.portfolio.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.ProjectHistoryJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.ProjectHistoryJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
