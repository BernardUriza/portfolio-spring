package com.portfolio.core.domain.experience;

import lombok.Getter;

@Getter
public enum ExperienceType {
    FULL_TIME("Full Time"),
    PART_TIME("Part Time"),
    CONTRACT("Contract"),
    INTERNSHIP("Internship"),
    FREELANCE("Freelance"),
    VOLUNTEER("Volunteer"),
    SELF_EMPLOYED("Self Employed");
    
    private final String displayName;
    
    ExperienceType(String displayName) {
        this.displayName = displayName;
    }
    
    public static ExperienceType fromDisplayName(String displayName) {
        for (ExperienceType type : values()) {
            if (type.displayName.equalsIgnoreCase(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown experience type: " + displayName);
    }
}