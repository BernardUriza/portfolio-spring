package com.portfolio.adapter.out.persistence.jpa;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "source_repositories", 
       uniqueConstraints = @UniqueConstraint(columnNames = "github_repo_url"),
       indexes = {
           @Index(name = "idx_source_github_id", columnList = "github_id"),
           @Index(name = "idx_source_full_name", columnList = "full_name"),
           @Index(name = "idx_source_sync_status", columnList = "sync_status"),
           @Index(name = "idx_source_language", columnList = "language"),
           @Index(name = "idx_source_updated_at", columnList = "updated_at"),
           @Index(name = "idx_source_sync_updated", columnList = "sync_status, updated_at"),
           @Index(name = "idx_source_lang_sync", columnList = "language, sync_status"),
           @Index(name = "idx_source_stars", columnList = "stargazers_count")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceRepositoryJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_id", nullable = false, unique = true)
    private Long githubId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "full_name", nullable = false, length = 300)
    private String fullName;

    @Column(length = 1000)
    private String description;

    @Column(name = "github_repo_url", nullable = false, length = 500)
    private String githubRepoUrl;

    @Column(length = 500)
    private String homepage;

    @Column(length = 50)
    private String language;

    @Column(name = "is_fork")
    private Boolean fork;

    @Column(name = "stargazers_count")
    private Integer stargazersCount;

    @ElementCollection
    @CollectionTable(
        name = "source_repository_topics",
        joinColumns = @JoinColumn(name = "source_repository_id")
    )
    @Column(name = "topic")
    @Builder.Default
    private List<String> topics = new ArrayList<>();

    @Column(name = "github_created_at")
    private String githubCreatedAt;

    @Column(name = "github_updated_at")
    private String githubUpdatedAt;

    @Column(name = "readme_markdown", columnDefinition = "TEXT")
    private String readmeMarkdown;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SyncStatus syncStatus = SyncStatus.UNSYNCED;

    @Column(name = "last_sync_attempt")
    private LocalDateTime lastSyncAttempt;

    @Column(name = "sync_error_message", length = 1000)
    private String syncErrorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;

    public enum SyncStatus {
        UNSYNCED,      // Never processed
        SYNCED,        // Successfully processed
        FAILED,        // Processing failed
        PROCESSING     // Currently being processed
    }
}