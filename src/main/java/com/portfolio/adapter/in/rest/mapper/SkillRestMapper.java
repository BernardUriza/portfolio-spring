package com.portfolio.adapter.in.rest.mapper;

import com.portfolio.adapter.in.rest.dto.SkillRestDto;
import com.portfolio.core.domain.skill.Skill;
import org.springframework.stereotype.Component;

@Component
public class SkillRestMapper {
    
    public SkillRestDto toRestDto(Skill domain) {
        if (domain == null) {
            return null;
        }
        
        return SkillRestDto.builder()
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
}