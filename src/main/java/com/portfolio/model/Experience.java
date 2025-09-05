package com.portfolio.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "experiences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"skills", "projects"})
@ToString(exclude = {"skills", "projects"})
public class Experience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    
    private String company;
    
    @Column(length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ExperienceType type = ExperienceType.PROFESSIONAL;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ExperienceLevel level = ExperienceLevel.INTERMEDIATE;
    
    @Builder.Default
    private Boolean isActive = true;
    
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "experience_skills",
        joinColumns = @JoinColumn(name = "experience_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @Builder.Default
    private Set<Skill> skills = new HashSet<>();
    
    @ManyToMany(mappedBy = "experiences", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Project> projects = new HashSet<>();
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum ExperienceType {
        PROFESSIONAL,
        PERSONAL_PROJECT,
        OPEN_SOURCE,
        EDUCATIONAL,
        FREELANCE,
        VOLUNTEER
    }
    
    public enum ExperienceLevel {
        ENTRY,
        INTERMEDIATE,
        SENIOR,
        EXPERT
    }
}
