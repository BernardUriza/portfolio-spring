package com.portfolio.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "skills")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"projects", "experiences"})
@ToString(exclude = {"projects", "experiences"})
public class Skill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SkillCategory category = SkillCategory.TECHNICAL;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SkillLevel level = SkillLevel.INTERMEDIATE;
    
    @Builder.Default
    private Boolean isActive = true;
    
    @ManyToMany(mappedBy = "skills", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Project> projects = new HashSet<>();
    
    @ManyToMany(mappedBy = "skills", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Experience> experiences = new HashSet<>();
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum SkillCategory {
        TECHNICAL,
        FRAMEWORK,
        LANGUAGE,
        TOOL,
        SOFT_SKILL,
        DATABASE,
        PLATFORM,
        METHODOLOGY
    }
    
    public enum SkillLevel {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED,
        EXPERT
    }
}
