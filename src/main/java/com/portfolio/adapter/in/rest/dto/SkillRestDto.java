package com.portfolio.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.portfolio.core.domain.skill.SkillCategory;
import com.portfolio.core.domain.skill.SkillLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Data
@Builder
@Jacksonized
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
}