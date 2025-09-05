package com.portfolio.core.domain.project;

public enum ProjectType {
    PERSONAL("Personal side project"),
    PROFESSIONAL("Professional work project"),
    OPEN_SOURCE("Open source contribution"),
    EDUCATIONAL("Learning or educational project"),
    CLIENT_WORK("Client or freelance work");
    
    private final String description;
    
    ProjectType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}