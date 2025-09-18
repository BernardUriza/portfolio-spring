package com.portfolio.core.domain.skill;

import com.portfolio.core.domain.shared.DomainEntity;

import java.time.LocalDateTime;

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

    private Skill(Builder b) {
        this.id = b.id;
        this.name = b.name;
        this.description = b.description;
        this.category = b.category;
        this.level = b.level;
        this.yearsOfExperience = b.yearsOfExperience;
        this.isFeatured = b.isFeatured;
        this.iconUrl = b.iconUrl;
        this.documentationUrl = b.documentationUrl;
        this.createdAt = b.createdAt;
        this.updatedAt = b.updatedAt;
    }

    public static Builder builder() { return new Builder(); }
    public Builder toBuilder() { return new Builder(this); }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public SkillCategory getCategory() { return category; }
    public SkillLevel getLevel() { return level; }
    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public Boolean getIsFeatured() { return isFeatured; }
    public String getIconUrl() { return iconUrl; }
    public String getDocumentationUrl() { return documentationUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
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

    public static final class Builder {
        private Long id;
        private String name;
        private String description;
        private SkillCategory category;
        private SkillLevel level;
        private Integer yearsOfExperience;
        private Boolean isFeatured;
        private String iconUrl;
        private String documentationUrl;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder() {}
        public Builder(Skill s) {
            this.id = s.id;
            this.name = s.name;
            this.description = s.description;
            this.category = s.category;
            this.level = s.level;
            this.yearsOfExperience = s.yearsOfExperience;
            this.isFeatured = s.isFeatured;
            this.iconUrl = s.iconUrl;
            this.documentationUrl = s.documentationUrl;
            this.createdAt = s.createdAt;
            this.updatedAt = s.updatedAt;
        }
        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder category(SkillCategory category) { this.category = category; return this; }
        public Builder level(SkillLevel level) { this.level = level; return this; }
        public Builder yearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; return this; }
        public Builder isFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; return this; }
        public Builder iconUrl(String iconUrl) { this.iconUrl = iconUrl; return this; }
        public Builder documentationUrl(String documentationUrl) { this.documentationUrl = documentationUrl; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Skill build() { return new Skill(this); }
    }
}
