package com.portfolio.core.domain.shared;

import java.time.LocalDateTime;

public abstract class DomainEntity {
    
    public abstract LocalDateTime getCreatedAt();
    
    public abstract LocalDateTime getUpdatedAt();
    
    protected void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }
    
    protected void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }
    
    protected void validateMaxLength(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " cannot exceed " + maxLength + " characters");
        }
    }
}