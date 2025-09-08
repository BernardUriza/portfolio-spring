package com.portfolio.adapter.out.persistence.jpa;

import com.portfolio.core.domain.project.*;
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
                .repositoryId(jpaEntity.getRepositoryId())
                .repositoryFullName(jpaEntity.getRepositoryFullName())
                .repositoryUrl(jpaEntity.getRepositoryUrl())
                .repositoryStars(jpaEntity.getRepositoryStars())
                .defaultBranch(jpaEntity.getDefaultBranch())
                .completionStatus(toDomainCompletionStatus(jpaEntity.getCompletionStatus()))
                .priority(toDomainPriority(jpaEntity.getPriority()))
                .protection(FieldProtection.builder()
                        .description(jpaEntity.getProtectDescription() != null ? jpaEntity.getProtectDescription() : false)
                        .liveDemoUrl(jpaEntity.getProtectLiveDemoUrl() != null ? jpaEntity.getProtectLiveDemoUrl() : false)
                        .skills(jpaEntity.getProtectSkills() != null ? jpaEntity.getProtectSkills() : false)
                        .experiences(jpaEntity.getProtectExperiences() != null ? jpaEntity.getProtectExperiences() : false)
                        .build())
                .createdAt(jpaEntity.getCreatedAt())
                .updatedAt(jpaEntity.getUpdatedAt())
                .manualDescriptionOverride(jpaEntity.getManualDescriptionOverride() != null ? 
                        jpaEntity.getManualDescriptionOverride() : false)
                .manualLinkOverride(jpaEntity.getManualLinkOverride() != null ? 
                        jpaEntity.getManualLinkOverride() : false)
                .manualSkillsOverride(jpaEntity.getManualSkillsOverride() != null ? 
                        jpaEntity.getManualSkillsOverride() : false)
                .manualExperiencesOverride(jpaEntity.getManualExperiencesOverride() != null ? 
                        jpaEntity.getManualExperiencesOverride() : false)
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
                .repositoryId(domain.getRepositoryId())
                .repositoryFullName(domain.getRepositoryFullName())
                .repositoryUrl(domain.getRepositoryUrl())
                .repositoryStars(domain.getRepositoryStars())
                .defaultBranch(domain.getDefaultBranch())
                .completionStatus(toJpaCompletionStatus(domain.getCompletionStatus()))
                .priority(toJpaPriority(domain.getPriority()))
                .protectDescription(domain.getProtection().getDescription())
                .protectLiveDemoUrl(domain.getProtection().getLiveDemoUrl())
                .protectSkills(domain.getProtection().getSkills())
                .protectExperiences(domain.getProtection().getExperiences())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .manualDescriptionOverride(domain.getManualDescriptionOverride() != null ? 
                        domain.getManualDescriptionOverride() : false)
                .manualLinkOverride(domain.getManualLinkOverride() != null ? 
                        domain.getManualLinkOverride() : false)
                .manualSkillsOverride(domain.getManualSkillsOverride() != null ? 
                        domain.getManualSkillsOverride() : false)
                .manualExperiencesOverride(domain.getManualExperiencesOverride() != null ? 
                        domain.getManualExperiencesOverride() : false)
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
    
    public ProjectCompletionStatus toDomainCompletionStatus(ProjectJpaEntity.ProjectCompletionStatusJpa jpaStatus) {
        if (jpaStatus == null) return ProjectCompletionStatus.BACKLOG;
        
        return switch (jpaStatus) {
            case BACKLOG -> ProjectCompletionStatus.BACKLOG;
            case IN_PROGRESS -> ProjectCompletionStatus.IN_PROGRESS;
            case LIVE -> ProjectCompletionStatus.LIVE;
            case ARCHIVED -> ProjectCompletionStatus.ARCHIVED;
        };
    }
    
    public ProjectJpaEntity.ProjectCompletionStatusJpa toJpaCompletionStatus(ProjectCompletionStatus domainStatus) {
        if (domainStatus == null) return ProjectJpaEntity.ProjectCompletionStatusJpa.BACKLOG;
        
        return switch (domainStatus) {
            case BACKLOG -> ProjectJpaEntity.ProjectCompletionStatusJpa.BACKLOG;
            case IN_PROGRESS -> ProjectJpaEntity.ProjectCompletionStatusJpa.IN_PROGRESS;
            case LIVE -> ProjectJpaEntity.ProjectCompletionStatusJpa.LIVE;
            case ARCHIVED -> ProjectJpaEntity.ProjectCompletionStatusJpa.ARCHIVED;
        };
    }
    
    public ProjectPriority toDomainPriority(ProjectJpaEntity.ProjectPriorityJpa jpaPriority) {
        if (jpaPriority == null) return null;
        
        return switch (jpaPriority) {
            case LOW -> ProjectPriority.LOW;
            case MEDIUM -> ProjectPriority.MEDIUM;
            case HIGH -> ProjectPriority.HIGH;
        };
    }
    
    public ProjectJpaEntity.ProjectPriorityJpa toJpaPriority(ProjectPriority domainPriority) {
        if (domainPriority == null) return null;
        
        return switch (domainPriority) {
            case LOW -> ProjectJpaEntity.ProjectPriorityJpa.LOW;
            case MEDIUM -> ProjectJpaEntity.ProjectPriorityJpa.MEDIUM;
            case HIGH -> ProjectJpaEntity.ProjectPriorityJpa.HIGH;
        };
    }
}