package com.portfolio.adapter.in.rest.mapper;

import com.portfolio.adapter.in.rest.dto.ExperienceRestDto;
import com.portfolio.core.domain.experience.Experience;
import org.springframework.stereotype.Component;

@Component
public class ExperienceRestMapper {
    
    public ExperienceRestDto toRestDto(Experience domain) {
        if (domain == null) {
            return null;
        }
        
        return ExperienceRestDto.builder()
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
                .achievements(domain.getAchievements())
                .technologies(domain.getTechnologies())
                .skillIds(domain.getSkillIds())
                .companyLogoUrl(domain.getCompanyLogoUrl())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}