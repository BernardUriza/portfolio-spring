package com.portfolio.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.portfolio.core.domain.skill.SkillCategory;
import com.portfolio.core.domain.skill.SkillLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@JsonDeserialize(builder = SkillRestDto.Builder.class)
public class SkillRestDto {
    
    private Long id;
    
    @NotBlank(message = "Skill name cannot be blank")
    @Size(max = 100, message = "Skill name cannot exceed 100 characters")
    private String name;
    
    @Size(max = 500, message = "Skill description cannot exceed 500 characters")
    private String description;
    
    @NotNull(message = "Skill category is required")
    private SkillCategory category;
    
    @NotNull(message = "Skill level is required")
    private SkillLevel level;
    
    private Integer yearsOfExperience;
    
    private Boolean isFeatured;
    
    private String iconUrl;
    
    private String documentationUrl;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public SkillRestDto() {}

    public SkillRestDto(Long id, String name, String description, SkillCategory category, SkillLevel level,
                        Integer yearsOfExperience, Boolean isFeatured, String iconUrl, String documentationUrl,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.level = level;
        this.yearsOfExperience = yearsOfExperience;
        this.isFeatured = isFeatured;
        this.iconUrl = iconUrl;
        this.documentationUrl = documentationUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public SkillCategory getCategory() { return category; }
    public void setCategory(SkillCategory category) { this.category = category; }
    public SkillLevel getLevel() { return level; }
    public void setLevel(SkillLevel level) { this.level = level; }
    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }
    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public String getDocumentationUrl() { return documentationUrl; }
    public void setDocumentationUrl(String documentationUrl) { this.documentationUrl = documentationUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @JsonPOJOBuilder(withPrefix = "")
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
        public SkillRestDto build() {
            return new SkillRestDto(id, name, description, category, level, yearsOfExperience, isFeatured,
                    iconUrl, documentationUrl, createdAt, updatedAt);
        }
    }
}
