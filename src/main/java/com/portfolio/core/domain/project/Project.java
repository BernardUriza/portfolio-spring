package com.portfolio.core.domain.project;

import com.portfolio.core.domain.shared.DomainEntity;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class Project extends DomainEntity {
    
    private final Long id;
    private final String title;
    private final String description;
    private final String link;
    private final String githubRepo;
    private final LocalDate createdDate;
    private final Integer estimatedDurationWeeks;
    private final ProjectStatus status;
    private final ProjectType type;
    @Builder.Default
    private final List<String> mainTechnologies = new ArrayList<>();
    @Builder.Default
    private final Set<Long> skillIds = new HashSet<>();
    @Builder.Default
    private final Set<Long> experienceIds = new HashSet<>();
    private final Long sourceStarredProjectId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    
    // Repository linking information
    private final Long repositoryId;
    private final String repositoryFullName;
    private final String repositoryUrl;
    private final Integer repositoryStars;
    private final String defaultBranch;
    
    // Project completion management
    @Builder.Default
    private final ProjectCompletionStatus completionStatus = ProjectCompletionStatus.BACKLOG;
    private final ProjectPriority priority;
    @Builder.Default
    private final FieldProtection protection = FieldProtection.allUnprotected();
    
    // Manual override flags to protect fields from sync overwriting (deprecated - use protection)
    @Builder.Default
    private final Boolean manualDescriptionOverride = false;
    @Builder.Default
    private final Boolean manualLinkOverride = false;
    @Builder.Default
    private final Boolean manualSkillsOverride = false;
    @Builder.Default
    private final Boolean manualExperiencesOverride = false;
    
    public static Project create(String title, String description, String link, 
                                String githubRepo, LocalDate createdDate, 
                                List<String> mainTechnologies) {
        validateTitle(title);
        validateDescription(description);
        
        return Project.builder()
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
    
    public Project updateBasicInfo(String title, String description, String link, String githubRepo) {
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
    
    public Project addSkill(Long skillId) {
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
    
    public Project removeSkill(Long skillId) {
        Set<Long> newSkillIds = new HashSet<>(this.skillIds);
        newSkillIds.remove(skillId);
        
        return this.toBuilder()
                .skillIds(newSkillIds)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Project changeStatus(ProjectStatus newStatus) {
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
    public Project updateDescriptionManually(String description) {
        validateDescription(description);
        return this.toBuilder()
                .description(description)
                .manualDescriptionOverride(true)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Project updateLinkManually(String link) {
        return this.toBuilder()
                .link(link)
                .manualLinkOverride(true)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Project updateSkillsManually(Set<Long> skillIds) {
        return this.toBuilder()
                .skillIds(new HashSet<>(skillIds))
                .manualSkillsOverride(true)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Project updateExperiencesManually(Set<Long> experienceIds) {
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
        // This will need to be checked against StarredProject
        return true; // Placeholder - will be implemented in service layer
    }
    
    // Repository linking methods
    public Project linkToRepository(Long repositoryId, String repositoryFullName, String repositoryUrl, 
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
    
    public Project unlinkFromRepository() {
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
    
    // Completion status methods
    public Project changeCompletionStatus(ProjectCompletionStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Completion status cannot be null");
        }
        
        return this.toBuilder()
                .completionStatus(newStatus)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Project changePriority(ProjectPriority newPriority) {
        return this.toBuilder()
                .priority(newPriority)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    // Field protection methods
    public Project protectField(String fieldName, boolean protect) {
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