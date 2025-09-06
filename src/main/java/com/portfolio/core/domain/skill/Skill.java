package com.portfolio.core.domain.skill;

import com.portfolio.core.domain.shared.DomainEntity;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class Skill extends DomainEntity {
    
    private final Long id;
    private final String name;
    private final String description;
    private final SkillCategory category;
    private final SkillLevel level;
    private final Integer yearsOfExperience;
    private final Boolean isFeatured;
    private final String iconUrl;
    private final String documentationUrl;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    
    public static Skill create(String name, String description, SkillCategory category, SkillLevel level) {
        validateName(name);
        validateCategory(category);
        validateLevel(level);
        
        return Skill.builder()
                .name(name)
                .description(description)
                .category(category)
                .level(level)
                .yearsOfExperience(0)
                .isFeatured(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Skill updateInfo(String name, String description, SkillCategory category, SkillLevel level) {
        validateName(name);
        validateCategory(category);
        validateLevel(level);
        
        return this.toBuilder()
                .name(name)
                .description(description)
                .category(category)
                .level(level)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Skill updateExperience(Integer yearsOfExperience) {
        if (yearsOfExperience != null && yearsOfExperience < 0) {
            throw new IllegalArgumentException("Years of experience cannot be negative");
        }
        
        return this.toBuilder()
                .yearsOfExperience(yearsOfExperience)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Skill setFeatured(Boolean featured) {
        return this.toBuilder()
                .isFeatured(featured != null ? featured : false)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Skill updateUrls(String iconUrl, String documentationUrl) {
        return this.toBuilder()
                .iconUrl(iconUrl)
                .documentationUrl(documentationUrl)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public boolean isExperienced() {
        return yearsOfExperience != null && yearsOfExperience >= 2;
    }
    
    public boolean isExpert() {
        return SkillLevel.EXPERT.equals(this.level);
    }
    
    public boolean hasIcon() {
        return iconUrl != null && !iconUrl.trim().isEmpty();
    }
    
    public boolean hasDocumentation() {
        return documentationUrl != null && !documentationUrl.trim().isEmpty();
    }
    
    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Skill name cannot be null or empty");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Skill name cannot exceed 100 characters");
        }
    }
    
    private static void validateCategory(SkillCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("Skill category cannot be null");
        }
    }
    
    private static void validateLevel(SkillLevel level) {
        if (level == null) {
            throw new IllegalArgumentException("Skill level cannot be null");
        }
    }
}