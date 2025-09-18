package com.portfolio.adapter.out.persistence.jpa;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "portfolio_projects",
       indexes = {
           @Index(name = "idx_portfolio_status", columnList = "status"),
           @Index(name = "idx_portfolio_completion_status", columnList = "completion_status"),
           @Index(name = "idx_portfolio_source_repo", columnList = "source_repository_id"),
           @Index(name = "idx_portfolio_updated_at", columnList = "updated_at"),
           @Index(name = "idx_portfolio_created_date", columnList = "created_date"),
           @Index(name = "idx_portfolio_type", columnList = "type"),
           @Index(name = "idx_portfolio_status_updated", columnList = "status, updated_at")
       })
public class PortfolioProjectJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(length = 255)
    private String link;

    @Column(length = 255)
    private String githubRepo;

    @Column(nullable = false)
    private LocalDate createdDate;

    private Integer estimatedDurationWeeks;
    
    @Enumerated(EnumType.STRING)
    private ProjectStatusJpa status = ProjectStatusJpa.ACTIVE;
    
    @Enumerated(EnumType.STRING)
    private ProjectTypeJpa type = ProjectTypeJpa.PERSONAL;
    
    @ElementCollection
    @CollectionTable(
        name = "portfolio_project_technologies",
        joinColumns = @JoinColumn(name = "portfolio_project_id")
    )
    @Column(name = "technology")
    private List<String> mainTechnologies = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(
        name = "portfolio_project_skill_ids",
        joinColumns = @JoinColumn(name = "portfolio_project_id")
    )
    @Column(name = "skill_id")
    private Set<Long> skillIds = new HashSet<>();
    
    @ElementCollection
    @CollectionTable(
        name = "portfolio_project_experience_ids",
        joinColumns = @JoinColumn(name = "portfolio_project_id")
    )
    @Column(name = "experience_id")
    private Set<Long> experienceIds = new HashSet<>();
    
    // New explicit relationship to SourceRepository
    @Column(name = "source_repository_id")
    private Long sourceRepositoryId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "link_type")
    private LinkTypeJpa linkType;
    
    // Repository linking information (deprecated - will use sourceRepositoryId)
    @Column(name = "repository_id")
    private Long repositoryId;
    
    @Column(name = "repository_full_name", length = 300)
    private String repositoryFullName;
    
    @Column(name = "repository_url", length = 500)
    private String repositoryUrl;
    
    @Column(name = "repository_stars")
    private Integer repositoryStars;
    
    @Column(name = "default_branch", length = 100)
    private String defaultBranch;
    
    // Project completion management
    @Enumerated(EnumType.STRING)
    @Column(name = "completion_status")
    private ProjectCompletionStatusJpa completionStatus = ProjectCompletionStatusJpa.BACKLOG;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private ProjectPriorityJpa priority;
    
    // Field protection flags
    @Column(name = "protect_description")
    private Boolean protectDescription = false;
    
    @Column(name = "protect_live_demo_url")
    private Boolean protectLiveDemoUrl = false;
    
    @Column(name = "protect_skills")
    private Boolean protectSkills = false;
    
    @Column(name = "protect_experiences")
    private Boolean protectExperiences = false;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
    
    // Manual override flags to prevent sync overwriting
    @Column(name = "manual_description_override")
    private Boolean manualDescriptionOverride = false;
    
    @Column(name = "manual_link_override")
    private Boolean manualLinkOverride = false;
    
    @Column(name = "manual_skills_override")
    private Boolean manualSkillsOverride = false;
    
    @Column(name = "manual_experiences_override")
    private Boolean manualExperiencesOverride = false;
    
    public enum ProjectStatusJpa {
        ACTIVE, COMPLETED, ON_HOLD, ARCHIVED
    }
    
    public enum ProjectTypeJpa {
        PERSONAL, PROFESSIONAL, OPEN_SOURCE, EDUCATIONAL, CLIENT_WORK
    }
    
    public enum ProjectCompletionStatusJpa {
        BACKLOG, IN_PROGRESS, LIVE, ARCHIVED
    }
    
    public enum ProjectPriorityJpa {
        LOW, MEDIUM, HIGH
    }
    
    public enum LinkTypeJpa {
        AUTO, MANUAL
    }

    public PortfolioProjectJpaEntity() {}

    public static Builder builder() { return new Builder(); }
    public Builder toBuilder() { return new Builder(this); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public String getGithubRepo() { return githubRepo; }
    public void setGithubRepo(String githubRepo) { this.githubRepo = githubRepo; }
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    public Integer getEstimatedDurationWeeks() { return estimatedDurationWeeks; }
    public void setEstimatedDurationWeeks(Integer estimatedDurationWeeks) { this.estimatedDurationWeeks = estimatedDurationWeeks; }
    public ProjectStatusJpa getStatus() { return status; }
    public void setStatus(ProjectStatusJpa status) { this.status = status; }
    public ProjectTypeJpa getType() { return type; }
    public void setType(ProjectTypeJpa type) { this.type = type; }
    public List<String> getMainTechnologies() { return mainTechnologies; }
    public void setMainTechnologies(List<String> mainTechnologies) { this.mainTechnologies = mainTechnologies; }
    public Set<Long> getSkillIds() { return skillIds; }
    public void setSkillIds(Set<Long> skillIds) { this.skillIds = skillIds; }
    public Set<Long> getExperienceIds() { return experienceIds; }
    public void setExperienceIds(Set<Long> experienceIds) { this.experienceIds = experienceIds; }
    public Long getSourceRepositoryId() { return sourceRepositoryId; }
    public void setSourceRepositoryId(Long sourceRepositoryId) { this.sourceRepositoryId = sourceRepositoryId; }
    public LinkTypeJpa getLinkType() { return linkType; }
    public void setLinkType(LinkTypeJpa linkType) { this.linkType = linkType; }
    public Long getRepositoryId() { return repositoryId; }
    public void setRepositoryId(Long repositoryId) { this.repositoryId = repositoryId; }
    public String getRepositoryFullName() { return repositoryFullName; }
    public void setRepositoryFullName(String repositoryFullName) { this.repositoryFullName = repositoryFullName; }
    public String getRepositoryUrl() { return repositoryUrl; }
    public void setRepositoryUrl(String repositoryUrl) { this.repositoryUrl = repositoryUrl; }
    public Integer getRepositoryStars() { return repositoryStars; }
    public void setRepositoryStars(Integer repositoryStars) { this.repositoryStars = repositoryStars; }
    public String getDefaultBranch() { return defaultBranch; }
    public void setDefaultBranch(String defaultBranch) { this.defaultBranch = defaultBranch; }
    public ProjectCompletionStatusJpa getCompletionStatus() { return completionStatus; }
    public void setCompletionStatus(ProjectCompletionStatusJpa completionStatus) { this.completionStatus = completionStatus; }
    public ProjectPriorityJpa getPriority() { return priority; }
    public void setPriority(ProjectPriorityJpa priority) { this.priority = priority; }
    public Boolean getProtectDescription() { return protectDescription; }
    public void setProtectDescription(Boolean protectDescription) { this.protectDescription = protectDescription; }
    public Boolean getProtectLiveDemoUrl() { return protectLiveDemoUrl; }
    public void setProtectLiveDemoUrl(Boolean protectLiveDemoUrl) { this.protectLiveDemoUrl = protectLiveDemoUrl; }
    public Boolean getProtectSkills() { return protectSkills; }
    public void setProtectSkills(Boolean protectSkills) { this.protectSkills = protectSkills; }
    public Boolean getProtectExperiences() { return protectExperiences; }
    public void setProtectExperiences(Boolean protectExperiences) { this.protectExperiences = protectExperiences; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Boolean getManualDescriptionOverride() { return manualDescriptionOverride; }
    public void setManualDescriptionOverride(Boolean manualDescriptionOverride) { this.manualDescriptionOverride = manualDescriptionOverride; }
    public Boolean getManualLinkOverride() { return manualLinkOverride; }
    public void setManualLinkOverride(Boolean manualLinkOverride) { this.manualLinkOverride = manualLinkOverride; }
    public Boolean getManualSkillsOverride() { return manualSkillsOverride; }
    public void setManualSkillsOverride(Boolean manualSkillsOverride) { this.manualSkillsOverride = manualSkillsOverride; }
    public Boolean getManualExperiencesOverride() { return manualExperiencesOverride; }
    public void setManualExperiencesOverride(Boolean manualExperiencesOverride) { this.manualExperiencesOverride = manualExperiencesOverride; }

    public static final class Builder {
        private final PortfolioProjectJpaEntity e;
        public Builder() { e = new PortfolioProjectJpaEntity(); }
        public Builder(PortfolioProjectJpaEntity src) {
            e = new PortfolioProjectJpaEntity();
            e.id = src.id; e.title = src.title; e.description = src.description; e.link = src.link;
            e.githubRepo = src.githubRepo; e.createdDate = src.createdDate; e.estimatedDurationWeeks = src.estimatedDurationWeeks;
            e.status = src.status; e.type = src.type; e.mainTechnologies = src.mainTechnologies != null ? new ArrayList<>(src.mainTechnologies) : new ArrayList<>();
            e.skillIds = src.skillIds != null ? new HashSet<>(src.skillIds) : new HashSet<>();
            e.experienceIds = src.experienceIds != null ? new HashSet<>(src.experienceIds) : new HashSet<>();
            e.sourceRepositoryId = src.sourceRepositoryId; e.linkType = src.linkType; e.createdAt = src.createdAt; e.updatedAt = src.updatedAt;
            e.repositoryId = src.repositoryId; e.repositoryFullName = src.repositoryFullName; e.repositoryUrl = src.repositoryUrl; e.repositoryStars = src.repositoryStars; e.defaultBranch = src.defaultBranch;
            e.completionStatus = src.completionStatus; e.priority = src.priority; e.protectDescription = src.protectDescription; e.protectLiveDemoUrl = src.protectLiveDemoUrl;
            e.protectSkills = src.protectSkills; e.protectExperiences = src.protectExperiences; e.version = src.version;
            e.manualDescriptionOverride = src.manualDescriptionOverride; e.manualLinkOverride = src.manualLinkOverride;
            e.manualSkillsOverride = src.manualSkillsOverride; e.manualExperiencesOverride = src.manualExperiencesOverride;
        }
        public Builder id(Long v) { e.setId(v); return this; }
        public Builder title(String v) { e.setTitle(v); return this; }
        public Builder description(String v) { e.setDescription(v); return this; }
        public Builder link(String v) { e.setLink(v); return this; }
        public Builder githubRepo(String v) { e.setGithubRepo(v); return this; }
        public Builder createdDate(LocalDate v) { e.setCreatedDate(v); return this; }
        public Builder estimatedDurationWeeks(Integer v) { e.setEstimatedDurationWeeks(v); return this; }
        public Builder status(ProjectStatusJpa v) { e.setStatus(v); return this; }
        public Builder type(ProjectTypeJpa v) { e.setType(v); return this; }
        public Builder mainTechnologies(List<String> v) { e.setMainTechnologies(v); return this; }
        public Builder skillIds(Set<Long> v) { e.setSkillIds(v); return this; }
        public Builder experienceIds(Set<Long> v) { e.setExperienceIds(v); return this; }
        public Builder sourceRepositoryId(Long v) { e.setSourceRepositoryId(v); return this; }
        public Builder linkType(LinkTypeJpa v) { e.setLinkType(v); return this; }
        public Builder repositoryId(Long v) { e.setRepositoryId(v); return this; }
        public Builder repositoryFullName(String v) { e.setRepositoryFullName(v); return this; }
        public Builder repositoryUrl(String v) { e.setRepositoryUrl(v); return this; }
        public Builder repositoryStars(Integer v) { e.setRepositoryStars(v); return this; }
        public Builder defaultBranch(String v) { e.setDefaultBranch(v); return this; }
        public Builder completionStatus(ProjectCompletionStatusJpa v) { e.setCompletionStatus(v); return this; }
        public Builder priority(ProjectPriorityJpa v) { e.setPriority(v); return this; }
        public Builder protectDescription(Boolean v) { e.setProtectDescription(v); return this; }
        public Builder protectLiveDemoUrl(Boolean v) { e.setProtectLiveDemoUrl(v); return this; }
        public Builder protectSkills(Boolean v) { e.setProtectSkills(v); return this; }
        public Builder protectExperiences(Boolean v) { e.setProtectExperiences(v); return this; }
        public Builder createdAt(LocalDateTime v) { e.setCreatedAt(v); return this; }
        public Builder updatedAt(LocalDateTime v) { e.setUpdatedAt(v); return this; }
        public Builder version(Long v) { e.setVersion(v); return this; }
        public Builder manualDescriptionOverride(Boolean v) { e.setManualDescriptionOverride(v); return this; }
        public Builder manualLinkOverride(Boolean v) { e.setManualLinkOverride(v); return this; }
        public Builder manualSkillsOverride(Boolean v) { e.setManualSkillsOverride(v); return this; }
        public Builder manualExperiencesOverride(Boolean v) { e.setManualExperiencesOverride(v); return this; }
        public PortfolioProjectJpaEntity build() { return e; }
    }
}
