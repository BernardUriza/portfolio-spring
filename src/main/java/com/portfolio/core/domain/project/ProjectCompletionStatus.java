package com.portfolio.core.domain.project;

public enum ProjectCompletionStatus {
    BACKLOG("Project is in the backlog"),
    IN_PROGRESS("Project is currently being developed"),
    LIVE("Project is live and accessible"),
    ARCHIVED("Project has been archived");
    
    private final String description;
    
    ProjectCompletionStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}