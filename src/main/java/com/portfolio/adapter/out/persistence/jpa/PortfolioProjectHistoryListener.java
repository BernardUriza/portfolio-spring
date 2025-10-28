package com.portfolio.adapter.out.persistence.jpa;

import com.portfolio.config.SpringContext;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

/**
 * JPA Entity Listener for automatic project history tracking
 * Publishes Spring events when projects are created or updated
 *
 * NOTE: This class is instantiated by JPA, not Spring, so we use SpringContext
 * to access the ApplicationEventPublisher bean instead of static fields.
 *
 * @author Bernard Uriza Orozco
 * @since 2025-10-27
 */
public class PortfolioProjectHistoryListener {

    private static final Logger log = LoggerFactory.getLogger(PortfolioProjectHistoryListener.class);

    /**
     * Called after a new project is persisted to the database
     * Publishes a Spring event to trigger history creation
     */
    @PostPersist
    public void afterCreate(PortfolioProjectJpaEntity project) {
        try {
            if (project.getId() != null) {
                publishProjectChangeEvent(project, ProjectHistoryJpaEntity.ChangeType.CREATE);
                log.debug("Published CREATE event for project {}", project.getId());
            }
        } catch (Exception e) {
            log.error("Failed to publish CREATE event for project: {}", project.getId(), e);
            // Don't rethrow - history failure shouldn't break project creation
        }
    }

    /**
     * Called after a project is updated in the database
     * Publishes a Spring event to trigger history creation
     */
    @PostUpdate
    public void afterUpdate(PortfolioProjectJpaEntity project) {
        try {
            if (project.getId() != null) {
                // Determine change type based on manual override flags
                ProjectHistoryJpaEntity.ChangeType changeType = determineChangeType(project);
                publishProjectChangeEvent(project, changeType);
                log.debug("Published {} event for project {}", changeType, project.getId());
            }
        } catch (Exception e) {
            log.error("Failed to publish UPDATE event for project: {}", project.getId(), e);
            // Don't rethrow - history failure shouldn't break project update
        }
    }

    /**
     * Publish a project change event using Spring's ApplicationEventPublisher
     * Uses SpringContext to access Spring beans from this JPA-managed listener
     */
    private void publishProjectChangeEvent(PortfolioProjectJpaEntity project,
                                           ProjectHistoryJpaEntity.ChangeType changeType) {
        try {
            ApplicationEventPublisher eventPublisher = SpringContext.getBean(ApplicationEventPublisher.class);
            PortfolioProjectChangeEvent event = new PortfolioProjectChangeEvent(
                    this,
                    project,
                    changeType,
                    "SYSTEM"
            );
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.error("Failed to publish project change event: {}", e.getMessage(), e);
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
}
