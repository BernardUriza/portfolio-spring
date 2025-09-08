package com.portfolio.adapter.out.persistence.jpa;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"skillIds", "experienceIds"})
@ToString(exclude = {"skillIds", "experienceIds"})
public class ProjectJpaEntity {
    
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
    @Builder.Default
    private ProjectStatusJpa status = ProjectStatusJpa.ACTIVE;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProjectTypeJpa type = ProjectTypeJpa.PERSONAL;
    
    @ElementCollection
    @CollectionTable(
        name = "project_technologies",
        joinColumns = @JoinColumn(name = "project_id")
    )
    @Column(name = "technology")
    @Builder.Default
    private List<String> mainTechnologies = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(
        name = "project_skill_ids",
        joinColumns = @JoinColumn(name = "project_id")
    )
    @Column(name = "skill_id")
    @Builder.Default
    private Set<Long> skillIds = new HashSet<>();
    
    @ElementCollection
    @CollectionTable(
        name = "project_experience_ids",
        joinColumns = @JoinColumn(name = "project_id")
    )
    @Column(name = "experience_id")
    @Builder.Default
    private Set<Long> experienceIds = new HashSet<>();
    
    @Column(name = "starred_project_id")
    private Long sourceStarredProjectId;
    
    // Repository linking information
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
    @Builder.Default
    private ProjectCompletionStatusJpa completionStatus = ProjectCompletionStatusJpa.BACKLOG;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private ProjectPriorityJpa priority;
    
    // Field protection flags
    @Column(name = "protect_description")
    @Builder.Default
    private Boolean protectDescription = false;
    
    @Column(name = "protect_live_demo_url")
    @Builder.Default
    private Boolean protectLiveDemoUrl = false;
    
    @Column(name = "protect_skills")
    @Builder.Default
    private Boolean protectSkills = false;
    
    @Column(name = "protect_experiences")
    @Builder.Default
    private Boolean protectExperiences = false;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Manual override flags to prevent sync overwriting
    @Column(name = "manual_description_override")
    @Builder.Default
    private Boolean manualDescriptionOverride = false;
    
    @Column(name = "manual_link_override")
    @Builder.Default
    private Boolean manualLinkOverride = false;
    
    @Column(name = "manual_skills_override")
    @Builder.Default
    private Boolean manualSkillsOverride = false;
    
    @Column(name = "manual_experiences_override")
    @Builder.Default
    private Boolean manualExperiencesOverride = false;
    
    public enum ProjectStatusJpa {
        ACTIVE, COMPLETED, ON_HOLD, ARCHIVED
    }
    
    public enum ProjectTypeJpa {
        PERSONAL, PROFESSIONAL, OPEN_SOURCE, EDUCATIONAL, CLIENT_WORK
    }
}