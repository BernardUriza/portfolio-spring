package com.portfolio.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@Jacksonized
public class ProjectCompletionDto {
    
    private Long projectId;
    private Long repositoryId;
    private String repositoryFullName;
    private String repositoryUrl;
    private Integer repositoryStars;
    private String defaultBranch;
    
    private String title;
    private String description;
    private String link;
    private String githubRepo;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;
    
    private Integer estimatedDurationWeeks;
    private String status;
    private String type;
    private List<String> mainTechnologies;
    private Set<Long> skillIds;
    private Set<Long> experienceIds;
    
    private String completionStatus;
    private String priority;
    private FieldProtectionDto protection;
    private CompletenessDto completeness;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @Jacksonized
    public static class FieldProtectionDto {
        private Boolean description;
        private Boolean liveDemoUrl;
        private Boolean skills;
        private Boolean experiences;
    }
    
    @Data
    @Builder
    @Jacksonized
    public static class CompletenessDto {
        private Integer score;
        private List<String> missing;
    }
}