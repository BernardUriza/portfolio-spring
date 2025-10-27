package com.portfolio.adapter.out.persistence.jpa;

import com.portfolio.service.ProjectHistoryService;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity Listener for automatic project history tracking
 * Automatically creates history snapshots when projects are created or updated
 *
 * @author Bernard Uriza Orozco
 * @since 2025-10-27
 */
@Component
public class PortfolioProjectHistoryListener {

    private static final Logger log = LoggerFactory.getLogger(PortfolioProjectHistoryListener.class);
    private static ProjectHistoryService historyService;

    /**
     * Spring-managed setter injection for the history service
     * Static field needed because JPA creates listener instances directly
     */
    @Autowired
    public void setHistoryService(ProjectHistoryService service) {
        PortfolioProjectHistoryListener.historyService = service;
    }

    /**
     * Called after a new project is persisted to the database
     * Creates initial history entry with CREATE change type
     */
    @PostPersist
    public void afterCreate(PortfolioProjectJpaEntity project) {
        try {
            if (historyService != null && project.getId() != null) {
                historyService.createHistoryEntry(
                        project,
                        ProjectHistoryJpaEntity.ChangeType.CREATE,
                        new ArrayList<>(),  // No changed fields for initial creation
                        "SYSTEM"
                );
                log.debug("Created initial history entry for project {}", project.getId());
            }
        } catch (Exception e) {
            log.error("Failed to create history entry after project creation: {}", project.getId(), e);
            // Don't rethrow - history failure shouldn't break project creation
        }
    }

    /**
     * Called after a project is updated in the database
     * Creates history entry with UPDATE or MANUAL change type based on manual override flags
     */
    @PostUpdate
    public void afterUpdate(PortfolioProjectJpaEntity project) {
        try {
            if (historyService != null && project.getId() != null) {
                // Determine change type based on manual override flags
                ProjectHistoryJpaEntity.ChangeType changeType = determineChangeType(project);

                // Calculate changed fields by comparing with previous version
                List<String> changedFields = calculateChangedFields(project);

                historyService.createHistoryEntry(
                        project,
                        changeType,
                        changedFields,
                        "SYSTEM"  // Could be enhanced to track actual user
                );
                log.debug("Created history entry for project {} update", project.getId());
            }
        } catch (Exception e) {
            log.error("Failed to create history entry after project update: {}", project.getId(), e);
            // Don't rethrow - history failure shouldn't break project update
        }
    }

    /**
     * Determine the type of change based on manual override flags
     */
    private ProjectHistoryJpaEntity.ChangeType determineChangeType(PortfolioProjectJpaEntity project) {
        // If any manual override flag is set, this is a manual change
        if (Boolean.TRUE.equals(project.getManualDescriptionOverride()) ||
            Boolean.TRUE.equals(project.getManualLinkOverride()) ||
            Boolean.TRUE.equals(project.getManualSkillsOverride()) ||
            Boolean.TRUE.equals(project.getManualExperiencesOverride())) {
            return ProjectHistoryJpaEntity.ChangeType.MANUAL;
        }

        // Default to UPDATE for normal changes
        return ProjectHistoryJpaEntity.ChangeType.UPDATE;
    }

    /**
     * Calculate which fields changed in this update
     * Note: This is a simplified version that marks all updatable fields as changed
     * A full implementation would require storing the previous state for comparison
     */
    private List<String> calculateChangedFields(PortfolioProjectJpaEntity project) {
        List<String> changedFields = new ArrayList<>();

        // For now, we'll retrieve the previous version from history and compare
        // If no history exists yet, mark common fields as potentially changed
        try {
            if (historyService != null) {
                var latestHistory = historyService.getLatestVersion(project.getId());
                if (latestHistory.isPresent()) {
                    PortfolioProjectJpaEntity oldVersion = historyService.deserializeSnapshot(
                            latestHistory.get().getSnapshotData()
                    );
                    return historyService.calculateChangedFields(oldVersion, project);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to calculate changed fields for project {}, marking all as changed", project.getId(), e);
        }

        // Fallback: if we can't determine changes, return empty list
        return changedFields;
    }
}
