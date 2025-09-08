package com.portfolio.core.domain.project;

public enum ProjectPriority {
    LOW("Low priority"),
    MEDIUM("Medium priority"), 
    HIGH("High priority");
    
    private final String description;
    
    ProjectPriority(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}