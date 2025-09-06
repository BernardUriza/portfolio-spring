package com.portfolio.core.domain.skill;

import lombok.Getter;

@Getter
public enum SkillLevel {
    BEGINNER("Beginner", 1),
    INTERMEDIATE("Intermediate", 2),
    ADVANCED("Advanced", 3),
    EXPERT("Expert", 4);
    
    private final String displayName;
    private final int numericValue;
    
    SkillLevel(String displayName, int numericValue) {
        this.displayName = displayName;
        this.numericValue = numericValue;
    }
    
    public static SkillLevel fromDisplayName(String displayName) {
        for (SkillLevel level : values()) {
            if (level.displayName.equalsIgnoreCase(displayName)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown skill level: " + displayName);
    }
    
    public static SkillLevel fromNumericValue(int value) {
        for (SkillLevel level : values()) {
            if (level.numericValue == value) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown skill level value: " + value);
    }
    
    public boolean isHigherThan(SkillLevel other) {
        return this.numericValue > other.numericValue;
    }
    
    public boolean isLowerThan(SkillLevel other) {
        return this.numericValue < other.numericValue;
    }
}