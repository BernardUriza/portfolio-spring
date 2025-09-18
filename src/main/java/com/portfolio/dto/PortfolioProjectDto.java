/**
 * Creado por Bernard Orozco
 * DTO for portfolio project data transfer
 */
package com.portfolio.dto;

import com.portfolio.core.domain.project.LinkType;
import com.portfolio.core.domain.project.ProjectCompletionStatus;
import com.portfolio.core.domain.project.ProjectPriority;
import com.portfolio.core.domain.project.ProjectStatus;
import com.portfolio.core.domain.project.ProjectType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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

    // Default constructor
    public PortfolioProjectDto() {
    }

    // All args constructor
    public PortfolioProjectDto(Long id, String title, String description, String link, String githubRepo,
                              LocalDate createdDate, Integer estimatedDurationWeeks, ProjectStatus status,
                              ProjectType type, List<String> mainTechnologies, Set<Long> skillIds,
                              Set<Long> experienceIds, Long sourceRepositoryId, LinkType linkType,
                              Long repositoryId, String repositoryFullName, String repositoryUrl,
                              Integer repositoryStars, String defaultBranch, ProjectCompletionStatus completionStatus,
                              ProjectPriority priority, Boolean protectDescription, Boolean protectLiveDemoUrl,
                              Boolean protectSkills, Boolean protectExperiences, LocalDateTime createdAt,
                              LocalDateTime updatedAt, Boolean manualDescriptionOverride, Boolean manualLinkOverride,
                              Boolean manualSkillsOverride, Boolean manualExperiencesOverride) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.link = link;
        this.githubRepo = githubRepo;
        this.createdDate = createdDate;
        this.estimatedDurationWeeks = estimatedDurationWeeks;
        this.status = status;
        this.type = type;
        this.mainTechnologies = mainTechnologies;
        this.skillIds = skillIds;
        this.experienceIds = experienceIds;
        this.sourceRepositoryId = sourceRepositoryId;
        this.linkType = linkType;
        this.repositoryId = repositoryId;
        this.repositoryFullName = repositoryFullName;
        this.repositoryUrl = repositoryUrl;
        this.repositoryStars = repositoryStars;
        this.defaultBranch = defaultBranch;
        this.completionStatus = completionStatus;
        this.priority = priority;
        this.protectDescription = protectDescription;
        this.protectLiveDemoUrl = protectLiveDemoUrl;
        this.protectSkills = protectSkills;
        this.protectExperiences = protectExperiences;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.manualDescriptionOverride = manualDescriptionOverride;
        this.manualLinkOverride = manualLinkOverride;
        this.manualSkillsOverride = manualSkillsOverride;
        this.manualExperiencesOverride = manualExperiencesOverride;
    }

    // Builder pattern
    public static PortfolioProjectDtoBuilder builder() {
        return new PortfolioProjectDtoBuilder();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getGithubRepo() {
        return githubRepo;
    }

    public void setGithubRepo(String githubRepo) {
        this.githubRepo = githubRepo;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public Integer getEstimatedDurationWeeks() {
        return estimatedDurationWeeks;
    }

    public void setEstimatedDurationWeeks(Integer estimatedDurationWeeks) {
        this.estimatedDurationWeeks = estimatedDurationWeeks;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public ProjectType getType() {
        return type;
    }

    public void setType(ProjectType type) {
        this.type = type;
    }

    public List<String> getMainTechnologies() {
        return mainTechnologies;
    }

    public void setMainTechnologies(List<String> mainTechnologies) {
        this.mainTechnologies = mainTechnologies;
    }

    public Set<Long> getSkillIds() {
        return skillIds;
    }

    public void setSkillIds(Set<Long> skillIds) {
        this.skillIds = skillIds;
    }

    public Set<Long> getExperienceIds() {
        return experienceIds;
    }

    public void setExperienceIds(Set<Long> experienceIds) {
        this.experienceIds = experienceIds;
    }

    public Long getSourceRepositoryId() {
        return sourceRepositoryId;
    }

    public void setSourceRepositoryId(Long sourceRepositoryId) {
        this.sourceRepositoryId = sourceRepositoryId;
    }

    public LinkType getLinkType() {
        return linkType;
    }

    public void setLinkType(LinkType linkType) {
        this.linkType = linkType;
    }

    public Long getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(Long repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getRepositoryFullName() {
        return repositoryFullName;
    }

    public void setRepositoryFullName(String repositoryFullName) {
        this.repositoryFullName = repositoryFullName;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public Integer getRepositoryStars() {
        return repositoryStars;
    }

    public void setRepositoryStars(Integer repositoryStars) {
        this.repositoryStars = repositoryStars;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    public ProjectCompletionStatus getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(ProjectCompletionStatus completionStatus) {
        this.completionStatus = completionStatus;
    }

    public ProjectPriority getPriority() {
        return priority;
    }

    public void setPriority(ProjectPriority priority) {
        this.priority = priority;
    }

    public Boolean getProtectDescription() {
        return protectDescription;
    }

    public void setProtectDescription(Boolean protectDescription) {
        this.protectDescription = protectDescription;
    }

    public Boolean getProtectLiveDemoUrl() {
        return protectLiveDemoUrl;
    }

    public void setProtectLiveDemoUrl(Boolean protectLiveDemoUrl) {
        this.protectLiveDemoUrl = protectLiveDemoUrl;
    }

    public Boolean getProtectSkills() {
        return protectSkills;
    }

    public void setProtectSkills(Boolean protectSkills) {
        this.protectSkills = protectSkills;
    }

    public Boolean getProtectExperiences() {
        return protectExperiences;
    }

    public void setProtectExperiences(Boolean protectExperiences) {
        this.protectExperiences = protectExperiences;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getManualDescriptionOverride() {
        return manualDescriptionOverride;
    }

    public void setManualDescriptionOverride(Boolean manualDescriptionOverride) {
        this.manualDescriptionOverride = manualDescriptionOverride;
    }

    public Boolean getManualLinkOverride() {
        return manualLinkOverride;
    }

    public void setManualLinkOverride(Boolean manualLinkOverride) {
        this.manualLinkOverride = manualLinkOverride;
    }

    public Boolean getManualSkillsOverride() {
        return manualSkillsOverride;
    }

    public void setManualSkillsOverride(Boolean manualSkillsOverride) {
        this.manualSkillsOverride = manualSkillsOverride;
    }

    public Boolean getManualExperiencesOverride() {
        return manualExperiencesOverride;
    }

    public void setManualExperiencesOverride(Boolean manualExperiencesOverride) {
        this.manualExperiencesOverride = manualExperiencesOverride;
    }

    // Builder class
    public static class PortfolioProjectDtoBuilder {
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
        private Long sourceRepositoryId;
        private LinkType linkType;
        private Long repositoryId;
        private String repositoryFullName;
        private String repositoryUrl;
        private Integer repositoryStars;
        private String defaultBranch;
        private ProjectCompletionStatus completionStatus;
        private ProjectPriority priority;
        private Boolean protectDescription;
        private Boolean protectLiveDemoUrl;
        private Boolean protectSkills;
        private Boolean protectExperiences;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Boolean manualDescriptionOverride;
        private Boolean manualLinkOverride;
        private Boolean manualSkillsOverride;
        private Boolean manualExperiencesOverride;

        public PortfolioProjectDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public PortfolioProjectDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public PortfolioProjectDtoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public PortfolioProjectDtoBuilder link(String link) {
            this.link = link;
            return this;
        }

        public PortfolioProjectDtoBuilder githubRepo(String githubRepo) {
            this.githubRepo = githubRepo;
            return this;
        }

        public PortfolioProjectDtoBuilder createdDate(LocalDate createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public PortfolioProjectDtoBuilder estimatedDurationWeeks(Integer estimatedDurationWeeks) {
            this.estimatedDurationWeeks = estimatedDurationWeeks;
            return this;
        }

        public PortfolioProjectDtoBuilder status(ProjectStatus status) {
            this.status = status;
            return this;
        }

        public PortfolioProjectDtoBuilder type(ProjectType type) {
            this.type = type;
            return this;
        }

        public PortfolioProjectDtoBuilder mainTechnologies(List<String> mainTechnologies) {
            this.mainTechnologies = mainTechnologies;
            return this;
        }

        public PortfolioProjectDtoBuilder skillIds(Set<Long> skillIds) {
            this.skillIds = skillIds;
            return this;
        }

        public PortfolioProjectDtoBuilder experienceIds(Set<Long> experienceIds) {
            this.experienceIds = experienceIds;
            return this;
        }

        public PortfolioProjectDtoBuilder sourceRepositoryId(Long sourceRepositoryId) {
            this.sourceRepositoryId = sourceRepositoryId;
            return this;
        }

        public PortfolioProjectDtoBuilder linkType(LinkType linkType) {
            this.linkType = linkType;
            return this;
        }

        public PortfolioProjectDtoBuilder repositoryId(Long repositoryId) {
            this.repositoryId = repositoryId;
            return this;
        }

        public PortfolioProjectDtoBuilder repositoryFullName(String repositoryFullName) {
            this.repositoryFullName = repositoryFullName;
            return this;
        }

        public PortfolioProjectDtoBuilder repositoryUrl(String repositoryUrl) {
            this.repositoryUrl = repositoryUrl;
            return this;
        }

        public PortfolioProjectDtoBuilder repositoryStars(Integer repositoryStars) {
            this.repositoryStars = repositoryStars;
            return this;
        }

        public PortfolioProjectDtoBuilder defaultBranch(String defaultBranch) {
            this.defaultBranch = defaultBranch;
            return this;
        }

        public PortfolioProjectDtoBuilder completionStatus(ProjectCompletionStatus completionStatus) {
            this.completionStatus = completionStatus;
            return this;
        }

        public PortfolioProjectDtoBuilder priority(ProjectPriority priority) {
            this.priority = priority;
            return this;
        }

        public PortfolioProjectDtoBuilder protectDescription(Boolean protectDescription) {
            this.protectDescription = protectDescription;
            return this;
        }

        public PortfolioProjectDtoBuilder protectLiveDemoUrl(Boolean protectLiveDemoUrl) {
            this.protectLiveDemoUrl = protectLiveDemoUrl;
            return this;
        }

        public PortfolioProjectDtoBuilder protectSkills(Boolean protectSkills) {
            this.protectSkills = protectSkills;
            return this;
        }

        public PortfolioProjectDtoBuilder protectExperiences(Boolean protectExperiences) {
            this.protectExperiences = protectExperiences;
            return this;
        }

        public PortfolioProjectDtoBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PortfolioProjectDtoBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public PortfolioProjectDtoBuilder manualDescriptionOverride(Boolean manualDescriptionOverride) {
            this.manualDescriptionOverride = manualDescriptionOverride;
            return this;
        }

        public PortfolioProjectDtoBuilder manualLinkOverride(Boolean manualLinkOverride) {
            this.manualLinkOverride = manualLinkOverride;
            return this;
        }

        public PortfolioProjectDtoBuilder manualSkillsOverride(Boolean manualSkillsOverride) {
            this.manualSkillsOverride = manualSkillsOverride;
            return this;
        }

        public PortfolioProjectDtoBuilder manualExperiencesOverride(Boolean manualExperiencesOverride) {
            this.manualExperiencesOverride = manualExperiencesOverride;
            return this;
        }

        public PortfolioProjectDto build() {
            return new PortfolioProjectDto(id, title, description, link, githubRepo, createdDate,
                    estimatedDurationWeeks, status, type, mainTechnologies, skillIds, experienceIds,
                    sourceRepositoryId, linkType, repositoryId, repositoryFullName, repositoryUrl,
                    repositoryStars, defaultBranch, completionStatus, priority, protectDescription,
                    protectLiveDemoUrl, protectSkills, protectExperiences, createdAt, updatedAt,
                    manualDescriptionOverride, manualLinkOverride, manualSkillsOverride, manualExperiencesOverride);
        }
    }
}