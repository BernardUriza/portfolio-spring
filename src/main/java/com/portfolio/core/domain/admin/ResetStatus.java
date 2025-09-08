package com.portfolio.core.domain.admin;

public enum ResetStatus {
    STARTED("Reset operation started"),
    IN_PROGRESS("Reset operation in progress"),
    COMPLETED("Reset operation completed successfully"),
    FAILED("Reset operation failed");
    
    private final String description;
    
    ResetStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}