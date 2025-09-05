package com.portfolio.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "starred_projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StarredProject {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private Long githubId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private String repositoryUrl;
    
    private String homepageUrl;
    
    private String primaryLanguage;
    
    @ElementCollection
    @CollectionTable(name = "starred_project_topics", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "topic")
    private List<String> topics;
    
    private LocalDateTime starredAt;
    
    private LocalDateTime lastUpdated;
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}