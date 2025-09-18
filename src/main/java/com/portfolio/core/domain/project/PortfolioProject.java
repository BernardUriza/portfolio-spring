package com.portfolio.core.domain.project;

import com.portfolio.core.domain.shared.DomainEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Creado por Bernard Orozco
 */
public class PortfolioProject extends DomainEntity {

    private final Long id;
    private final String title;
    private final String description;
    private final String link;
    private final String githubRepo;
    private final LocalDate createdDate;
    private final Integer estimatedDurationWeeks;
    private final ProjectStatus status;
    private final ProjectType type;
    private final List<String> mainTechnologies;
    private final Set<Long> skillIds;
    private final Set<Long> experienceIds;

    // New explicit relationship to SourceRepository
    private final Long sourceRepositoryId;
    private final LinkType linkType;

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // Repository linking information (deprecated - will use sourceRepositoryId)
    private final Long repositoryId;
    private final String repositoryFullName;
    private final String repositoryUrl;
    private final Integer repositoryStars;
    private final String defaultBranch;

    // Project completion management
    private final ProjectCompletionStatus completionStatus;
    private final ProjectPriority priority;
    private final FieldProtection protection;

    // Manual override flags to protect fields from sync overwriting (deprecated - use protection)
    private final Boolean manualDescriptionOverride;
    private final Boolean manualLinkOverride;
    private final Boolean manualSkillsOverride;
    private final Boolean manualExperiencesOverride;

    private PortfolioProject(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.description = builder.description;
        this.link = builder.link;
        this.githubRepo = builder.githubRepo;
        this.createdDate = builder.createdDate;
        this.estimatedDurationWeeks = builder.estimatedDurationWeeks;
        this.status = builder.status;
        this.type = builder.type;
        this.mainTechnologies = builder.mainTechnologies != null ? builder.mainTechnologies : new ArrayList<>();
        this.skillIds = builder.skillIds != null ? builder.skillIds : new HashSet<>();
        this.experienceIds = builder.experienceIds != null ? builder.experienceIds : new HashSet<>();
        this.sourceRepositoryId = builder.sourceRepositoryId;
        this.linkType = builder.linkType;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.repositoryId = builder.repositoryId;
        this.repositoryFullName = builder.repositoryFullName;
        this.repositoryUrl = builder.repositoryUrl;
        this.repositoryStars = builder.repositoryStars;
        this.defaultBranch = builder.defaultBranch;
        this.completionStatus = builder.completionStatus != null ? builder.completionStatus : ProjectCompletionStatus.BACKLOG;
        this.priority = builder.priority;
        this.protection = builder.protection != null ? builder.protection : FieldProtection.allUnprotected();
        this.manualDescriptionOverride = Boolean.TRUE.equals(builder.manualDescriptionOverride);
        this.manualLinkOverride = Boolean.TRUE.equals(builder.manualLinkOverride);
        this.manualSkillsOverride = Boolean.TRUE.equals(builder.manualSkillsOverride);
        this.manualExperiencesOverride = Boolean.TRUE.equals(builder.manualExperiencesOverride);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(this.id)
                .title(this.title)
                .description(this.description)
                .link(this.link)
                .githubRepo(this.githubRepo)
                .createdDate(this.createdDate)
                .estimatedDurationWeeks(this.estimatedDurationWeeks)
                .status(this.status)
                .type(this.type)
                .mainTechnologies(this.mainTechnologies)
                .skillIds(this.skillIds)
                .experienceIds(this.experienceIds)
                .sourceRepositoryId(this.sourceRepositoryId)
                .linkType(this.linkType)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .repositoryId(this.repositoryId)
                .repositoryFullName(this.repositoryFullName)
                .repositoryUrl(this.repositoryUrl)
                .repositoryStars(this.repositoryStars)
                .defaultBranch(this.defaultBranch)
                .completionStatus(this.completionStatus)
                .priority(this.priority)
                .protection(this.protection)
                .manualDescriptionOverride(this.manualDescriptionOverride)
                .manualLinkOverride(this.manualLinkOverride)
                .manualSkillsOverride(this.manualSkillsOverride)
                .manualExperiencesOverride(this.manualExperiencesOverride);
    }

    public static class Builder {
        private Long id;
        private String title;
        private String description;
        private String link;
        private String githubRepo;
        private LocalDate createdDate;
        private Integer estimatedDurationWeeks;
        private ProjectStatus status;
        private ProjectType type;
        private List<String> mainTechnologies = new ArrayList<>();
        private Set<Long> skillIds = new HashSet<>();
        private Set<Long> experienceIds = new HashSet<>();
        private Long sourceRepositoryId;
        private LinkType linkType;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Long repositoryId;
        private String repositoryFullName;
        private String repositoryUrl;
        private Integer repositoryStars;
        private String defaultBranch;
        private ProjectCompletionStatus completionStatus = ProjectCompletionStatus.BACKLOG;
        private ProjectPriority priority;
        private FieldProtection protection = FieldProtection.allUnprotected();
        private Boolean manualDescriptionOverride = false;
        private Boolean manualLinkOverride = false;
        private Boolean manualSkillsOverride = false;
        private Boolean manualExperiencesOverride = false;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder link(String link) { this.link = link; return this; }
        public Builder githubRepo(String githubRepo) { this.githubRepo = githubRepo; return this; }
        public Builder createdDate(LocalDate createdDate) { this.createdDate = createdDate; return this; }
        public Builder estimatedDurationWeeks(Integer estimatedDurationWeeks) { this.estimatedDurationWeeks = estimatedDurationWeeks; return this; }
        public Builder status(ProjectStatus status) { this.status = status; return this; }
        public Builder type(ProjectType type) { this.type = type; return this; }
        public Builder mainTechnologies(List<String> mainTechnologies) { this.mainTechnologies = mainTechnologies; return this; }
        public Builder skillIds(Set<Long> skillIds) { this.skillIds = skillIds; return this; }
        public Builder experienceIds(Set<Long> experienceIds) { this.experienceIds = experienceIds; return this; }
        public Builder sourceRepositoryId(Long sourceRepositoryId) { this.sourceRepositoryId = sourceRepositoryId; return this; }
        public Builder linkType(LinkType linkType) { this.linkType = linkType; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder repositoryId(Long repositoryId) { this.repositoryId = repositoryId; return this; }
        public Builder repositoryFullName(String repositoryFullName) { this.repositoryFullName = repositoryFullName; return this; }
        public Builder repositoryUrl(String repositoryUrl) { this.repositoryUrl = repositoryUrl; return this; }
        public Builder repositoryStars(Integer repositoryStars) { this.repositoryStars = repositoryStars; return this; }
        public Builder defaultBranch(String defaultBranch) { this.defaultBranch = defaultBranch; return this; }
        public Builder completionStatus(ProjectCompletionStatus completionStatus) { this.completionStatus = completionStatus; return this; }
        public Builder priority(ProjectPriority priority) { this.priority = priority; return this; }
        public Builder protection(FieldProtection protection) { this.protection = protection; return this; }
        public Builder manualDescriptionOverride(Boolean manualDescriptionOverride) { this.manualDescriptionOverride = manualDescriptionOverride; return this; }
        public Builder manualLinkOverride(Boolean manualLinkOverride) { this.manualLinkOverride = manualLinkOverride; return this; }
        public Builder manualSkillsOverride(Boolean manualSkillsOverride) { this.manualSkillsOverride = manualSkillsOverride; return this; }
        public Builder manualExperiencesOverride(Boolean manualExperiencesOverride) { this.manualExperiencesOverride = manualExperiencesOverride; return this; }

        public PortfolioProject build() {
            return new PortfolioProject(this);
        }
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLink() { return link; }
    public String getGithubRepo() { return githubRepo; }
    public LocalDate getCreatedDate() { return createdDate; }
    public Integer getEstimatedDurationWeeks() { return estimatedDurationWeeks; }
    public ProjectStatus getStatus() { return status; }
    public ProjectType getType() { return type; }
    public List<String> getMainTechnologies() { return mainTechnologies; }
    public Set<Long> getSkillIds() { return skillIds; }
    public Set<Long> getExperienceIds() { return experienceIds; }
    public Long getSourceRepositoryId() { return sourceRepositoryId; }
    public LinkType getLinkType() { return linkType; }
    @Override
    public LocalDateTime getCreatedAt() { return createdAt; }
    @Override
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getRepositoryId() { return repositoryId; }
    public String getRepositoryFullName() { return repositoryFullName; }
    public String getRepositoryUrl() { return repositoryUrl; }
    public Integer getRepositoryStars() { return repositoryStars; }
    public String getDefaultBranch() { return defaultBranch; }
    public ProjectCompletionStatus getCompletionStatus() { return completionStatus; }
    public ProjectPriority getPriority() { return priority; }
    public FieldProtection getProtection() { return protection; }
    public Boolean getManualDescriptionOverride() { return manualDescriptionOverride; }
    public Boolean getManualLinkOverride() { return manualLinkOverride; }
    public Boolean getManualSkillsOverride() { return manualSkillsOverride; }
    public Boolean getManualExperiencesOverride() { return manualExperiencesOverride; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PortfolioProject that = (PortfolioProject) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(title, that.title) &&
               Objects.equals(description, that.description) &&
               Objects.equals(link, that.link) &&
               Objects.equals(githubRepo, that.githubRepo) &&
               Objects.equals(createdDate, that.createdDate) &&
               Objects.equals(estimatedDurationWeeks, that.estimatedDurationWeeks) &&
               status == that.status &&
               type == that.type &&
               Objects.equals(mainTechnologies, that.mainTechnologies) &&
               Objects.equals(skillIds, that.skillIds) &&
               Objects.equals(experienceIds, that.experienceIds) &&
               Objects.equals(sourceRepositoryId, that.sourceRepositoryId) &&
               linkType == that.linkType &&
               Objects.equals(createdAt, that.createdAt) &&
               Objects.equals(updatedAt, that.updatedAt) &&
               Objects.equals(repositoryId, that.repositoryId) &&
               Objects.equals(repositoryFullName, that.repositoryFullName) &&
               Objects.equals(repositoryUrl, that.repositoryUrl) &&
               Objects.equals(repositoryStars, that.repositoryStars) &&
               Objects.equals(defaultBranch, that.defaultBranch) &&
               completionStatus == that.completionStatus &&
               priority == that.priority &&
               Objects.equals(protection, that.protection) &&
               Objects.equals(manualDescriptionOverride, that.manualDescriptionOverride) &&
               Objects.equals(manualLinkOverride, that.manualLinkOverride) &&
               Objects.equals(manualSkillsOverride, that.manualSkillsOverride) &&
               Objects.equals(manualExperiencesOverride, that.manualExperiencesOverride);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, title, description, link, githubRepo, createdDate,
                          estimatedDurationWeeks, status, type, mainTechnologies, skillIds, experienceIds,
                          sourceRepositoryId, linkType, createdAt, updatedAt, repositoryId, repositoryFullName,
                          repositoryUrl, repositoryStars, defaultBranch, completionStatus, priority, protection,
                          manualDescriptionOverride, manualLinkOverride, manualSkillsOverride, manualExperiencesOverride);
    }

    public static PortfolioProject create(String title, String description, String link,
                                String githubRepo, LocalDate createdDate,
                                List<String> mainTechnologies) {
        validateTitle(title);
        validateDescription(description);

        return PortfolioProject.builder()
                .title(title)
                .description(description)
                .link(link)
                .githubRepo(githubRepo)
                .createdDate(createdDate)
                .mainTechnologies(mainTechnologies != null ? new ArrayList<>(mainTechnologies) : new ArrayList<>())
                .status(ProjectStatus.ACTIVE)
                .type(ProjectType.PERSONAL)
                .skillIds(new HashSet<>())
                .experienceIds(new HashSet<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public PortfolioProject updateBasicInfo(String title, String description, String link, String githubRepo) {
        validateTitle(title);
        validateDescription(description);

        return this.toBuilder()
                .title(title)
                .description(description)
                .link(link)
                .githubRepo(githubRepo)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public PortfolioProject addSkill(Long skillId) {
        if (skillId == null) {
            throw new IllegalArgumentException("Skill ID cannot be null");
        }

        Set<Long> newSkillIds = new HashSet<>(this.skillIds);
        newSkillIds.add(skillId);

        return this.toBuilder()
                .skillIds(newSkillIds)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public PortfolioProject removeSkill(Long skillId) {
        Set<Long> newSkillIds = new HashSet<>(this.skillIds);
        newSkillIds.remove(skillId);

        return this.toBuilder()
                .skillIds(newSkillIds)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public PortfolioProject changeStatus(ProjectStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Project status cannot be null");
        }

        return this.toBuilder()
                .status(newStatus)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public boolean isActive() {
        return ProjectStatus.ACTIVE.equals(this.status);
    }

    public boolean isCompleted() {
        return ProjectStatus.COMPLETED.equals(this.status);
    }

    public boolean hasGithubRepo() {
        return githubRepo != null && !githubRepo.trim().isEmpty();
    }

    public boolean hasExternalLink() {
        return link != null && !link.trim().isEmpty();
    }

    // Manual update methods with override protection
    public PortfolioProject updateDescriptionManually(String description) {
        validateDescription(description);
        return this.toBuilder()
                .description(description)
                .manualDescriptionOverride(true)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public PortfolioProject updateLinkManually(String link) {
        return this.toBuilder()
                .link(link)
                .manualLinkOverride(true)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public PortfolioProject updateSkillsManually(Set<Long> skillIds) {
        return this.toBuilder()
                .skillIds(new HashSet<>(skillIds))
                .manualSkillsOverride(true)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public PortfolioProject updateExperiencesManually(Set<Long> experienceIds) {
        return this.toBuilder()
                .experienceIds(new HashSet<>(experienceIds))
                .manualExperiencesOverride(true)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // Validation methods for completeness
    public boolean isDescriptionComplete() {
        return description != null && !description.trim().isEmpty();
    }

    public boolean isLiveDemoComplete() {
        return link != null && !link.trim().isEmpty();
    }

    public boolean hasSkills() {
        return skillIds != null && !skillIds.isEmpty();
    }

    public boolean hasExperiences() {
        return experienceIds != null && !experienceIds.isEmpty();
    }

    public boolean hasReadmeMarkdown() {
        // This will need to be checked against SourceRepository
        return true; // Placeholder - will be implemented in service layer
    }

    // Repository linking methods
    public PortfolioProject linkToRepository(Long repositoryId, String repositoryFullName, String repositoryUrl,
                                   Integer repositoryStars, String defaultBranch) {
        return this.toBuilder()
                .repositoryId(repositoryId)
                .repositoryFullName(repositoryFullName)
                .repositoryUrl(repositoryUrl)
                .repositoryStars(repositoryStars)
                .defaultBranch(defaultBranch)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public PortfolioProject unlinkFromRepository() {
        return this.toBuilder()
                .repositoryId(null)
                .repositoryFullName(null)
                .repositoryUrl(null)
                .repositoryStars(null)
                .defaultBranch(null)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public boolean isLinkedToRepository() {
        return repositoryId != null && repositoryFullName != null;
    }

    // New explicit source repository linking methods
    public PortfolioProject linkToSourceRepository(Long sourceRepositoryId, LinkType linkType) {
        if (sourceRepositoryId == null) {
            throw new IllegalArgumentException("Source repository ID cannot be null");
        }
        if (linkType == null) {
            throw new IllegalArgumentException("Link type cannot be null");
        }

        return this.toBuilder()
                .sourceRepositoryId(sourceRepositoryId)
                .linkType(linkType)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public PortfolioProject unlinkFromSourceRepository() {
        return this.toBuilder()
                .sourceRepositoryId(null)
                .linkType(null)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public boolean isLinkedToSourceRepository() {
        return sourceRepositoryId != null && linkType != null;
    }

    // Completion status methods
    public PortfolioProject changeCompletionStatus(ProjectCompletionStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Completion status cannot be null");
        }

        return this.toBuilder()
                .completionStatus(newStatus)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public PortfolioProject changePriority(ProjectPriority newPriority) {
        return this.toBuilder()
                .priority(newPriority)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // Field protection methods
    public PortfolioProject protectField(String fieldName, boolean protect) {
        FieldProtection newProtection = switch (fieldName.toLowerCase()) {
            case "description" -> protection.withDescription(protect);
            case "livedemouri", "link" -> protection.withLiveDemoUrl(protect);
            case "skills" -> protection.withSkills(protect);
            case "experiences" -> protection.withExperiences(protect);
            default -> throw new IllegalArgumentException("Unknown field: " + fieldName);
        };

        return this.toBuilder()
                .protection(newProtection)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public boolean isFieldProtected(String fieldName) {
        return switch (fieldName.toLowerCase()) {
            case "description" -> protection.getDescription();
            case "livedemouri", "link" -> protection.getLiveDemoUrl();
            case "skills" -> protection.getSkills();
            case "experiences" -> protection.getExperiences();
            default -> false;
        };
    }

    // Completeness calculation
    public ProjectCompleteness getCompleteness() {
        return ProjectCompleteness.calculate(this);
    }

    private static void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Project title cannot be null or empty");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("Project title cannot exceed 200 characters");
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Project description cannot be null or empty");
        }
        if (description.length() > 1000) {
            throw new IllegalArgumentException("Project description cannot exceed 1000 characters");
        }
    }
}