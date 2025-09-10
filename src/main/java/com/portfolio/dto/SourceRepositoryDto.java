package com.portfolio.dto;

import com.portfolio.adapter.out.persistence.jpa.SourceRepositoryJpaEntity.SyncStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}