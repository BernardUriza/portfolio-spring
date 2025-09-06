package com.portfolio.adapter.out.persistence.jpa;

import com.portfolio.core.domain.skill.Skill;
import org.springframework.stereotype.Component;

@Component
public class SkillJpaMapper {
    
    public Skill toDomain(SkillJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return Skill.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .level(entity.getLevel())
                .yearsOfExperience(entity.getYearsOfExperience())
                .isFeatured(entity.getIsFeatured())
                .iconUrl(entity.getIconUrl())
                .documentationUrl(entity.getDocumentationUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    public SkillJpaEntity toEntity(Skill domain) {
        if (domain == null) {
            return null;
        }
        
        return SkillJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .category(domain.getCategory())
                .level(domain.getLevel())
                .yearsOfExperience(domain.getYearsOfExperience())
                .isFeatured(domain.getIsFeatured())
                .iconUrl(domain.getIconUrl())
                .documentationUrl(domain.getDocumentationUrl())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
    
    public SkillJpaEntity updateEntity(SkillJpaEntity entity, Skill domain) {
        if (entity == null || domain == null) {
            return entity;
        }
        
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setCategory(domain.getCategory());
        entity.setLevel(domain.getLevel());
        entity.setYearsOfExperience(domain.getYearsOfExperience());
        entity.setIsFeatured(domain.getIsFeatured());
        entity.setIconUrl(domain.getIconUrl());
        entity.setDocumentationUrl(domain.getDocumentationUrl());
        // createdAt and updatedAt are managed by JPA annotations
        
        return entity;
    }
}