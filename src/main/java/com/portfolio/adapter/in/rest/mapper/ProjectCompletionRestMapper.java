package com.portfolio.adapter.in.rest.mapper;

import com.portfolio.adapter.in.rest.dto.ProjectCompletionDto;
import com.portfolio.core.domain.project.Project;
import com.portfolio.core.domain.project.ProjectCompleteness;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ProjectCompletionRestMapper {
    
    public ProjectCompletionDto toDto(Project project) {
        if (project == null) return null;
        
        ProjectCompleteness completeness = project.getCompleteness();
        
        return ProjectCompletionDto.builder()
                .projectId(project.getId())
                .repositoryId(project.getRepositoryId())
                .repositoryFullName(project.getRepositoryFullName())
                .repositoryUrl(project.getRepositoryUrl())
                .repositoryStars(project.getRepositoryStars())
                .defaultBranch(project.getDefaultBranch())
                .title(project.getTitle())
                .description(project.getDescription())
                .link(project.getLink())
                .githubRepo(project.getGithubRepo())
                .createdDate(project.getCreatedDate())
                .estimatedDurationWeeks(project.getEstimatedDurationWeeks())
                .status(project.getStatus() != null ? project.getStatus().name() : null)
                .type(project.getType() != null ? project.getType().name() : null)
                .mainTechnologies(project.getMainTechnologies() != null ? 
                        new ArrayList<>(project.getMainTechnologies()) : new ArrayList<>())
                .skillIds(project.getSkillIds())
                .experienceIds(project.getExperienceIds())
                .completionStatus(project.getCompletionStatus() != null ? 
                        project.getCompletionStatus().name() : null)
                .priority(project.getPriority() != null ? project.getPriority().name() : null)
                .protection(mapProtection(project))
                .completeness(mapCompleteness(completeness))
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
    
    private ProjectCompletionDto.FieldProtectionDto mapProtection(Project project) {
        if (project.getProtection() == null) {
            return ProjectCompletionDto.FieldProtectionDto.builder()
                    .description(false)
                    .liveDemoUrl(false)
                    .skills(false)
                    .experiences(false)
                    .build();
        }
        
        return ProjectCompletionDto.FieldProtectionDto.builder()
                .description(project.getProtection().getDescription())
                .liveDemoUrl(project.getProtection().getLiveDemoUrl())
                .skills(project.getProtection().getSkills())
                .experiences(project.getProtection().getExperiences())
                .build();
    }
    
    private ProjectCompletionDto.CompletenessDto mapCompleteness(ProjectCompleteness completeness) {
        if (completeness == null) {
            return ProjectCompletionDto.CompletenessDto.builder()
                    .score(0)
                    .missing(new ArrayList<>())
                    .build();
        }
        
        return ProjectCompletionDto.CompletenessDto.builder()
                .score(completeness.getScore())
                .missing(completeness.getMissing() != null ? 
                        new ArrayList<>(completeness.getMissing()) : new ArrayList<>())
                .build();
    }
}