package com.portfolio.core.domain.project;

public enum ProjectStatus {
    ACTIVE("Active project in development"),
    COMPLETED("Project has been completed"),
    ON_HOLD("Project is temporarily paused"),
    ARCHIVED("Project is no longer maintained");
    
    private final String description;
    
    ProjectStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean canTransitionTo(ProjectStatus newStatus) {
        return switch (this) {
            case ACTIVE -> newStatus == COMPLETED || newStatus == ON_HOLD || newStatus == ARCHIVED;
            case ON_HOLD -> newStatus == ACTIVE || newStatus == ARCHIVED;
            case COMPLETED -> newStatus == ARCHIVED;
            case ARCHIVED -> false;
        };
    }
}