package com.portfolio.model;

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
@EqualsAndHashCode(exclude = {"skills", "experiences"})
@ToString(exclude = {"skills", "experiences"})
public class Project {
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
    private ProjectStatus status = ProjectStatus.ACTIVE;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProjectType type = ProjectType.PERSONAL;
    
    @ElementCollection
    @CollectionTable(
        name = "project_technologies",
        joinColumns = @JoinColumn(name = "project_id")
    )
    @Column(name = "technology")
    @Builder.Default
    private List<String> mainTechnologies = new ArrayList<>();
    
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "project_skills",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @Builder.Default
    private Set<Skill> skills = new HashSet<>();
    
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "project_experiences",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "experience_id")
    )
    @Builder.Default
    private Set<Experience> experiences = new HashSet<>();
    
    @ManyToOne
    @JoinColumn(name = "starred_project_id")
    private StarredProject sourceStarredProject;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum ProjectStatus {
        ACTIVE,
        COMPLETED,
        ON_HOLD,
        ARCHIVED
    }
    
    public enum ProjectType {
        PERSONAL,
        PROFESSIONAL,
        OPEN_SOURCE,
        EDUCATIONAL,
        CLIENT_WORK
    }
}
