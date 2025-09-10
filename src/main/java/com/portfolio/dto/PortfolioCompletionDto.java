package com.portfolio.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PortfolioCompletionDto {
    
    private Long id;
    private String title;
    private String description;
    private String link;
    private String githubRepo;
    private String status;
    private String type;
    private String completionStatus;
    private String priority;
    private java.util.List<String> mainTechnologies;
    private Long sourceRepositoryId;
    private String linkType;
    
    // Protection flags
    private Boolean protectDescription;
    private Boolean protectLiveDemoUrl;
    private Boolean protectSkills;
    private Boolean protectExperiences;
    
    // Completion metrics
    private CompletionScoresDto completionScores;
    private Double overallCompleteness;
    
    @Data
    @Builder
    public static class CompletionScoresDto {
        private Double basicInfo;        // title, description
        private Double links;           // githubRepo, live demo
        private Double metadata;        // technologies, status, type
        private Double enrichment;      // skills, experiences from AI
        private Double documentation;   // README analysis completion
    }
}