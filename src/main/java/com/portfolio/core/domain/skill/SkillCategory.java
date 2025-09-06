package com.portfolio.core.domain.skill;

import lombok.Getter;

@Getter
public enum SkillCategory {
    PROGRAMMING_LANGUAGE("Programming Language"),
    FRAMEWORK("Framework"),
    DATABASE("Database"),
    TOOL("Tool"),
    CLOUD_PLATFORM("Cloud Platform"),
    DEVOPS("DevOps"),
    FRONTEND("Frontend"),
    BACKEND("Backend"),
    MOBILE("Mobile"),
    DESIGN("Design"),
    METHODOLOGY("Methodology"),
    SOFT_SKILL("Soft Skill");
    
    private final String displayName;
    
    SkillCategory(String displayName) {
        this.displayName = displayName;
    }
    
    public static SkillCategory fromDisplayName(String displayName) {
        for (SkillCategory category : values()) {
            if (category.displayName.equalsIgnoreCase(displayName)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown skill category: " + displayName);
    }
}