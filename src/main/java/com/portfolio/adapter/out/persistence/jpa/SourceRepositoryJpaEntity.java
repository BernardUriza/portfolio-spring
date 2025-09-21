/**
 * Creado por Bernard Orozco
 * JPA entity for source repository data persistence
 */
package com.portfolio.adapter.out.persistence.jpa;

import jakarta.persistence.*;
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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "source_repository_topics",
        joinColumns = @JoinColumn(name = "source_repository_id")
    )
    @Column(name = "topic")
    private List<String> topics = new ArrayList<>();

    @Column(name = "github_created_at")
    private String githubCreatedAt;

    @Column(name = "github_updated_at")
    private String githubUpdatedAt;

    @Column(name = "readme_markdown", columnDefinition = "TEXT")
    private String readmeMarkdown;

    @Enumerated(EnumType.STRING)
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

    // Default constructor
    public SourceRepositoryJpaEntity() {
    }

    // All args constructor
    public SourceRepositoryJpaEntity(Long id, Long githubId, String name, String fullName, String description,
                                     String githubRepoUrl, String homepage, String language, Boolean fork,
                                     Integer stargazersCount, List<String> topics, String githubCreatedAt,
                                     String githubUpdatedAt, String readmeMarkdown, SyncStatus syncStatus,
                                     LocalDateTime lastSyncAttempt, String syncErrorMessage, LocalDateTime createdAt,
                                     LocalDateTime updatedAt, Long version) {
        this.id = id;
        this.githubId = githubId;
        this.name = name;
        this.fullName = fullName;
        this.description = description;
        this.githubRepoUrl = githubRepoUrl;
        this.homepage = homepage;
        this.language = language;
        this.fork = fork;
        this.stargazersCount = stargazersCount;
        this.topics = topics != null ? topics : new ArrayList<>();
        this.githubCreatedAt = githubCreatedAt;
        this.githubUpdatedAt = githubUpdatedAt;
        this.readmeMarkdown = readmeMarkdown;
        this.syncStatus = syncStatus != null ? syncStatus : SyncStatus.UNSYNCED;
        this.lastSyncAttempt = lastSyncAttempt;
        this.syncErrorMessage = syncErrorMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    // Builder pattern
    public static SourceRepositoryJpaEntityBuilder builder() {
        return new SourceRepositoryJpaEntityBuilder();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGithubId() {
        return githubId;
    }

    public void setGithubId(Long githubId) {
        this.githubId = githubId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGithubRepoUrl() {
        return githubRepoUrl;
    }

    public void setGithubRepoUrl(String githubRepoUrl) {
        this.githubRepoUrl = githubRepoUrl;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Boolean getFork() {
        return fork;
    }

    public void setFork(Boolean fork) {
        this.fork = fork;
    }

    public Integer getStargazersCount() {
        return stargazersCount;
    }

    public void setStargazersCount(Integer stargazersCount) {
        this.stargazersCount = stargazersCount;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics != null ? topics : new ArrayList<>();
    }

    public String getGithubCreatedAt() {
        return githubCreatedAt;
    }

    public void setGithubCreatedAt(String githubCreatedAt) {
        this.githubCreatedAt = githubCreatedAt;
    }

    public String getGithubUpdatedAt() {
        return githubUpdatedAt;
    }

    public void setGithubUpdatedAt(String githubUpdatedAt) {
        this.githubUpdatedAt = githubUpdatedAt;
    }

    public String getReadmeMarkdown() {
        return readmeMarkdown;
    }

    public void setReadmeMarkdown(String readmeMarkdown) {
        this.readmeMarkdown = readmeMarkdown;
    }

    public SyncStatus getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(SyncStatus syncStatus) {
        this.syncStatus = syncStatus != null ? syncStatus : SyncStatus.UNSYNCED;
    }

    public LocalDateTime getLastSyncAttempt() {
        return lastSyncAttempt;
    }

    public void setLastSyncAttempt(LocalDateTime lastSyncAttempt) {
        this.lastSyncAttempt = lastSyncAttempt;
    }

    public String getSyncErrorMessage() {
        return syncErrorMessage;
    }

    public void setSyncErrorMessage(String syncErrorMessage) {
        this.syncErrorMessage = syncErrorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // Builder class
    public static class SourceRepositoryJpaEntityBuilder {
        private Long id;
        private Long githubId;
        private String name;
        private String fullName;
        private String description;
        private String githubRepoUrl;
        private String homepage;
        private String language;
        private Boolean fork;
        private Integer stargazersCount;
        private List<String> topics = new ArrayList<>();
        private String githubCreatedAt;
        private String githubUpdatedAt;
        private String readmeMarkdown;
        private SyncStatus syncStatus = SyncStatus.UNSYNCED;
        private LocalDateTime lastSyncAttempt;
        private String syncErrorMessage;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Long version;

        public SourceRepositoryJpaEntityBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public SourceRepositoryJpaEntityBuilder githubId(Long githubId) {
            this.githubId = githubId;
            return this;
        }

        public SourceRepositoryJpaEntityBuilder name(String name) {
            this.name = name;
            return this;
        }

        public SourceRepositoryJpaEntityBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public SourceRepositoryJpaEntityBuilder description(String description) {
            this.description = description;
            return this;
        }

        public SourceRepositoryJpaEntityBuilder githubRepoUrl(String githubRepoUrl) {
            this.githubRepoUrl = githubRepoUrl;
            return this;
        }

        public SourceRepositoryJpaEntityBuilder homepage(String homepage) {
            this.homepage = homepage;
            return this;
        }

        public SourceRepositoryJpaEntityBuilder language(String language) {
            this.language = language;
            return this;
        }

        public SourceRepositoryJpaEntityBuilder fork(Boolean fork) {
            this.fork = fork;
            return this;
        }

        public SourceRepositoryJpaEntityBuilder stargazersCount(Integer stargazersCount) {
            this.stargazersCount = stargazersCount;
            return this;
        }

        public SourceRepositoryJpaEntityBuilder topics(List<String> topics) {
            this.topics = topics != null ? topics : new ArrayList<>();
            return this;
        }

        public SourceRepositoryJpaEntityBuilder githubCreatedAt(String githubCreatedAt) {
            this.githubCreatedAt = githubCreatedAt;
            return this;
        }

        public SourceRepositoryJpaEntityBuilder githubUpdatedAt(String githubUpdatedAt) {
            this.githubUpdatedAt = githubUpdatedAt;
            return this;
        }

        public SourceRepositoryJpaEntityBuilder readmeMarkdown(String readmeMarkdown) {
            this.readmeMarkdown = readmeMarkdown;
            return this;
        }

        public SourceRepositoryJpaEntityBuilder syncStatus(SyncStatus syncStatus) {
            this.syncStatus = syncStatus != null ? syncStatus : SyncStatus.UNSYNCED;
            return this;
        }

        public SourceRepositoryJpaEntityBuilder lastSyncAttempt(LocalDateTime lastSyncAttempt) {
            this.lastSyncAttempt = lastSyncAttempt;
            return this;
        }

        public SourceRepositoryJpaEntityBuilder syncErrorMessage(String syncErrorMessage) {
            this.syncErrorMessage = syncErrorMessage;
            return this;
        }

        public SourceRepositoryJpaEntityBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public SourceRepositoryJpaEntityBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public SourceRepositoryJpaEntityBuilder version(Long version) {
            this.version = version;
            return this;
        }

        public SourceRepositoryJpaEntity build() {
            return new SourceRepositoryJpaEntity(id, githubId, name, fullName, description, githubRepoUrl,
                    homepage, language, fork, stargazersCount, topics, githubCreatedAt, githubUpdatedAt,
                    readmeMarkdown, syncStatus, lastSyncAttempt, syncErrorMessage, createdAt, updatedAt, version);
        }
    }

    public enum SyncStatus {
        UNSYNCED,      // Never processed
        SYNCED,        // Successfully processed
        FAILED,        // Processing failed
        PROCESSING     // Currently being processed
    }
}