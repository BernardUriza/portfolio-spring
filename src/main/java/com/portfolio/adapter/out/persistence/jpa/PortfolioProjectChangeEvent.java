package com.portfolio.adapter.out.persistence.jpa;

import org.springframework.context.ApplicationEvent;

/**
 * Spring Application Event fired when a portfolio project is created or updated
 * Used to trigger project history tracking without JPA listener static fields
 *
 * @author Bernard Uriza Orozco
 * @since 2025-10-27
 */
public class PortfolioProjectChangeEvent extends ApplicationEvent {

    private final PortfolioProjectJpaEntity project;
    private final ProjectHistoryJpaEntity.ChangeType changeType;
    private final String changedBy;

    /**
     * Create a new project change event
     * @param source The source object that triggered the event
     * @param project The project entity that was changed
     * @param changeType The type of change (CREATE, UPDATE, MANUAL, etc.)
     * @param changedBy Who made the change (e.g., "SYSTEM", username)
     */
    public PortfolioProjectChangeEvent(Object source,
                                      PortfolioProjectJpaEntity project,
                                      ProjectHistoryJpaEntity.ChangeType changeType,
                                      String changedBy) {
        super(source);
        this.project = project;
        this.changeType = changeType;
        this.changedBy = changedBy != null ? changedBy : "SYSTEM";
    }

    public PortfolioProjectJpaEntity getProject() {
        return project;
    }

    public ProjectHistoryJpaEntity.ChangeType getChangeType() {
        return changeType;
    }

    public String getChangedBy() {
        return changedBy;
    }
}
