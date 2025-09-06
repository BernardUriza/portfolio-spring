package com.portfolio.adapter.out.persistence.jpa;

import com.portfolio.core.domain.experience.Experience;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;

@Component
public class ExperienceJpaMapper {
    
    public Experience toDomain(ExperienceJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return Experience.builder()
                .id(entity.getId())
                .jobTitle(entity.getJobTitle())
                .companyName(entity.getCompanyName())
                .companyUrl(entity.getCompanyUrl())
                .location(entity.getLocation())
                .type(entity.getType())
                .description(entity.getDescription())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .isCurrentPosition(entity.getIsCurrentPosition())
                .achievements(entity.getAchievements() != null ? new ArrayList<>(entity.getAchievements()) : new ArrayList<>())
                .technologies(entity.getTechnologies() != null ? new ArrayList<>(entity.getTechnologies()) : new ArrayList<>())
                .skillIds(entity.getSkillIds() != null ? new HashSet<>(entity.getSkillIds()) : new HashSet<>())
                .companyLogoUrl(entity.getCompanyLogoUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    public ExperienceJpaEntity toEntity(Experience domain) {
        if (domain == null) {
            return null;
        }
        
        return ExperienceJpaEntity.builder()
                .id(domain.getId())
                .jobTitle(domain.getJobTitle())
                .companyName(domain.getCompanyName())
                .companyUrl(domain.getCompanyUrl())
                .location(domain.getLocation())
                .type(domain.getType())
                .description(domain.getDescription())
                .startDate(domain.getStartDate())
                .endDate(domain.getEndDate())
                .isCurrentPosition(domain.getIsCurrentPosition())
                .achievements(domain.getAchievements() != null ? new ArrayList<>(domain.getAchievements()) : new ArrayList<>())
                .technologies(domain.getTechnologies() != null ? new ArrayList<>(domain.getTechnologies()) : new ArrayList<>())
                .skillIds(domain.getSkillIds() != null ? new HashSet<>(domain.getSkillIds()) : new HashSet<>())
                .companyLogoUrl(domain.getCompanyLogoUrl())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
    
    public ExperienceJpaEntity updateEntity(ExperienceJpaEntity entity, Experience domain) {
        if (entity == null || domain == null) {
            return entity;
        }
        
        entity.setJobTitle(domain.getJobTitle());
        entity.setCompanyName(domain.getCompanyName());
        entity.setCompanyUrl(domain.getCompanyUrl());
        entity.setLocation(domain.getLocation());
        entity.setType(domain.getType());
        entity.setDescription(domain.getDescription());
        entity.setStartDate(domain.getStartDate());
        entity.setEndDate(domain.getEndDate());
        entity.setIsCurrentPosition(domain.getIsCurrentPosition());
        entity.setAchievements(domain.getAchievements() != null ? new ArrayList<>(domain.getAchievements()) : new ArrayList<>());
        entity.setTechnologies(domain.getTechnologies() != null ? new ArrayList<>(domain.getTechnologies()) : new ArrayList<>());
        entity.setSkillIds(domain.getSkillIds() != null ? new HashSet<>(domain.getSkillIds()) : new HashSet<>());
        entity.setCompanyLogoUrl(domain.getCompanyLogoUrl());
        // createdAt and updatedAt are managed by JPA annotations
        
        return entity;
    }
}