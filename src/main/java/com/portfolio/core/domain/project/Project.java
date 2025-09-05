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