package com.portfolio.dto;

import com.portfolio.core.domain.project.LinkType;
import com.portfolio.core.domain.project.ProjectCompletionStatus;
import com.portfolio.core.domain.project.ProjectPriority;
import com.portfolio.core.domain.project.ProjectStatus;
import com.portfolio.core.domain.project.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioProjectDto {
    
    private Long id;
    private String title;
    private String description;
    private String link;
    private String githubRepo;
    private LocalDate createdDate;
    private Integer estimatedDurationWeeks;
    private ProjectStatus status;
    private ProjectType type;
    private List<String> mainTechnologies;
    private Set<Long> skillIds;
    private Set<Long> experienceIds;
    
    // New explicit relationship to SourceRepository
    private Long sourceRepositoryId;
    private LinkType linkType;
    
    // Repository linking information (deprecated)
    private Long repositoryId;
    private String repositoryFullName;
    private String repositoryUrl;
    private Integer repositoryStars;
    private String defaultBranch;
    
    // Project completion management
    private ProjectCompletionStatus completionStatus;
    private ProjectPriority priority;
    
    // Field protection flags
    private Boolean protectDescription;
    private Boolean protectLiveDemoUrl;
    private Boolean protectSkills;
    private Boolean protectExperiences;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Manual override flags
    private Boolean manualDescriptionOverride;
    private Boolean manualLinkOverride;
    private Boolean manualSkillsOverride;
    private Boolean manualExperiencesOverride;
}