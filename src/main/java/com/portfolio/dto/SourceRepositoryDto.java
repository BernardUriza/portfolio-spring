/**
 * Creado por Bernard Orozco
 * DTO for source repository data transfer
 */
package com.portfolio.dto;

import com.portfolio.adapter.out.persistence.jpa.SourceRepositoryJpaEntity.SyncStatus;

import java.time.LocalDateTime;
import java.util.List;

public class SourceRepositoryDto {
    
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
    private List<String> topics;
    private String githubCreatedAt;
    private String githubUpdatedAt;
    private String readmeMarkdown;
    private SyncStatus syncStatus;
    private LocalDateTime lastSyncAttempt;
    private String syncErrorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public SourceRepositoryDto() {
    }

    // All args constructor
    public SourceRepositoryDto(Long id, Long githubId, String name, String fullName, String description,
                               String githubRepoUrl, String homepage, String language, Boolean fork,
                               Integer stargazersCount, List<String> topics, String githubCreatedAt,
                               String githubUpdatedAt, String readmeMarkdown, SyncStatus syncStatus,
                               LocalDateTime lastSyncAttempt, String syncErrorMessage, LocalDateTime createdAt,
                               LocalDateTime updatedAt) {
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
        this.topics = topics;
        this.githubCreatedAt = githubCreatedAt;
        this.githubUpdatedAt = githubUpdatedAt;
        this.readmeMarkdown = readmeMarkdown;
        this.syncStatus = syncStatus;
        this.lastSyncAttempt = lastSyncAttempt;
        this.syncErrorMessage = syncErrorMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Builder pattern
    public static SourceRepositoryDtoBuilder builder() {
        return new SourceRepositoryDtoBuilder();
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
        this.topics = topics;
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
        this.syncStatus = syncStatus;
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

    // Builder class
    public static class SourceRepositoryDtoBuilder {
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
        private List<String> topics;
        private String githubCreatedAt;
        private String githubUpdatedAt;
        private String readmeMarkdown;
        private SyncStatus syncStatus;
        private LocalDateTime lastSyncAttempt;
        private String syncErrorMessage;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public SourceRepositoryDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public SourceRepositoryDtoBuilder githubId(Long githubId) {
            this.githubId = githubId;
            return this;
        }

        public SourceRepositoryDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public SourceRepositoryDtoBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public SourceRepositoryDtoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public SourceRepositoryDtoBuilder githubRepoUrl(String githubRepoUrl) {
            this.githubRepoUrl = githubRepoUrl;
            return this;
        }

        public SourceRepositoryDtoBuilder homepage(String homepage) {
            this.homepage = homepage;
            return this;
        }

        public SourceRepositoryDtoBuilder language(String language) {
            this.language = language;
            return this;
        }

        public SourceRepositoryDtoBuilder fork(Boolean fork) {
            this.fork = fork;
            return this;
        }

        public SourceRepositoryDtoBuilder stargazersCount(Integer stargazersCount) {
            this.stargazersCount = stargazersCount;
            return this;
        }

        public SourceRepositoryDtoBuilder topics(List<String> topics) {
            this.topics = topics;
            return this;
        }

        public SourceRepositoryDtoBuilder githubCreatedAt(String githubCreatedAt) {
            this.githubCreatedAt = githubCreatedAt;
            return this;
        }

        public SourceRepositoryDtoBuilder githubUpdatedAt(String githubUpdatedAt) {
            this.githubUpdatedAt = githubUpdatedAt;
            return this;
        }

        public SourceRepositoryDtoBuilder readmeMarkdown(String readmeMarkdown) {
            this.readmeMarkdown = readmeMarkdown;
            return this;
        }

        public SourceRepositoryDtoBuilder syncStatus(SyncStatus syncStatus) {
            this.syncStatus = syncStatus;
            return this;
        }

        public SourceRepositoryDtoBuilder lastSyncAttempt(LocalDateTime lastSyncAttempt) {
            this.lastSyncAttempt = lastSyncAttempt;
            return this;
        }

        public SourceRepositoryDtoBuilder syncErrorMessage(String syncErrorMessage) {
            this.syncErrorMessage = syncErrorMessage;
            return this;
        }

        public SourceRepositoryDtoBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public SourceRepositoryDtoBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public SourceRepositoryDto build() {
            return new SourceRepositoryDto(id, githubId, name, fullName, description, githubRepoUrl,
                    homepage, language, fork, stargazersCount, topics, githubCreatedAt, githubUpdatedAt,
                    readmeMarkdown, syncStatus, lastSyncAttempt, syncErrorMessage, createdAt, updatedAt);
        }
    }
}