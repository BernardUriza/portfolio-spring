package com.portfolio.core.domain.experience;

/**
 * Creado por Bernard Orozco
 */
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

    public String getDisplayName() {
        return displayName;
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