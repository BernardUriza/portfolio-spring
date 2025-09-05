package com.portfolio.adapter.out.persistence.jpa;

import com.portfolio.core.domain.project.Project;
import com.portfolio.core.domain.project.ProjectStatus;
import com.portfolio.core.domain.project.ProjectType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;

@Component
public class ProjectJpaMapper {
    
    public Project toDomainEntity(ProjectJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;
        
        return Project.builder()
                .id(jpaEntity.getId())
                .title(jpaEntity.getTitle())
                .description(jpaEntity.getDescription())
                .link(jpaEntity.getLink())
                .githubRepo(jpaEntity.getGithubRepo())
                .createdDate(jpaEntity.getCreatedDate())
                .estimatedDurationWeeks(jpaEntity.getEstimatedDurationWeeks())
                .status(toDomainStatus(jpaEntity.getStatus()))
                .type(toDomainType(jpaEntity.getType()))
                .mainTechnologies(jpaEntity.getMainTechnologies() != null ? 
                        new ArrayList<>(jpaEntity.getMainTechnologies()) : new ArrayList<>())
                .skillIds(jpaEntity.getSkillIds() != null ? 
                        new HashSet<>(jpaEntity.getSkillIds()) : new HashSet<>())
                .experienceIds(jpaEntity.getExperienceIds() != null ? 
                        new HashSet<>(jpaEntity.getExperienceIds()) : new HashSet<>())
                .sourceStarredProjectId(jpaEntity.getSourceStarredProjectId())
                .createdAt(jpaEntity.getCreatedAt())
                .updatedAt(jpaEntity.getUpdatedAt())
                .build();
    }
    
    public ProjectJpaEntity toJpaEntity(Project domain) {
        if (domain == null) return null;
        
        return ProjectJpaEntity.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .link(domain.getLink())
                .githubRepo(domain.getGithubRepo())
                .createdDate(domain.getCreatedDate())
                .estimatedDurationWeeks(domain.getEstimatedDurationWeeks())
                .status(toJpaStatus(domain.getStatus()))
                .type(toJpaType(domain.getType()))
                .mainTechnologies(domain.getMainTechnologies() != null ? 
                        new ArrayList<>(domain.getMainTechnologies()) : new ArrayList<>())
                .skillIds(domain.getSkillIds() != null ? 
                        new HashSet<>(domain.getSkillIds()) : new HashSet<>())
                .experienceIds(domain.getExperienceIds() != null ? 
                        new HashSet<>(domain.getExperienceIds()) : new HashSet<>())
                .sourceStarredProjectId(domain.getSourceStarredProjectId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
    
    public ProjectStatus toDomainStatus(ProjectJpaEntity.ProjectStatusJpa jpaStatus) {
        if (jpaStatus == null) return ProjectStatus.ACTIVE;
        
        return switch (jpaStatus) {
            case ACTIVE -> ProjectStatus.ACTIVE;
            case COMPLETED -> ProjectStatus.COMPLETED;
            case ON_HOLD -> ProjectStatus.ON_HOLD;
            case ARCHIVED -> ProjectStatus.ARCHIVED;
        };
    }
    
    public ProjectJpaEntity.ProjectStatusJpa toJpaStatus(ProjectStatus domainStatus) {
        if (domainStatus == null) return ProjectJpaEntity.ProjectStatusJpa.ACTIVE;
        
        return switch (domainStatus) {
            case ACTIVE -> ProjectJpaEntity.ProjectStatusJpa.ACTIVE;
            case COMPLETED -> ProjectJpaEntity.ProjectStatusJpa.COMPLETED;
            case ON_HOLD -> ProjectJpaEntity.ProjectStatusJpa.ON_HOLD;
            case ARCHIVED -> ProjectJpaEntity.ProjectStatusJpa.ARCHIVED;
        };
    }
    
    public ProjectType toDomainType(ProjectJpaEntity.ProjectTypeJpa jpaType) {
        if (jpaType == null) return ProjectType.PERSONAL;
        
        return switch (jpaType) {
            case PERSONAL -> ProjectType.PERSONAL;
            case PROFESSIONAL -> ProjectType.PROFESSIONAL;
            case OPEN_SOURCE -> ProjectType.OPEN_SOURCE;
            case EDUCATIONAL -> ProjectType.EDUCATIONAL;
            case CLIENT_WORK -> ProjectType.CLIENT_WORK;
        };
    }
    
    public ProjectJpaEntity.ProjectTypeJpa toJpaType(ProjectType domainType) {
        if (domainType == null) return ProjectJpaEntity.ProjectTypeJpa.PERSONAL;
        
        return switch (domainType) {
            case PERSONAL -> ProjectJpaEntity.ProjectTypeJpa.PERSONAL;
            case PROFESSIONAL -> ProjectJpaEntity.ProjectTypeJpa.PROFESSIONAL;
            case OPEN_SOURCE -> ProjectJpaEntity.ProjectTypeJpa.OPEN_SOURCE;
            case EDUCATIONAL -> ProjectJpaEntity.ProjectTypeJpa.EDUCATIONAL;
            case CLIENT_WORK -> ProjectJpaEntity.ProjectTypeJpa.CLIENT_WORK;
        };
    }
}