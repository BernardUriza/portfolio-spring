/**
 * Creado por Bernard Orozco
 * DTO for portfolio completion data
 */
package com.portfolio.dto;

import java.util.Set;

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
    
    // Associations for admin editing
    private Set<Long> skillIds;
    private Set<Long> experienceIds;
    
    // Protection flags
    private Boolean protectDescription;
    private Boolean protectLiveDemoUrl;
    private Boolean protectSkills;
    private Boolean protectExperiences;
    
    // Completion metrics
    private CompletionScoresDto completionScores;
    private Double overallCompleteness;

    // Default constructor
    public PortfolioCompletionDto() {
    }

    // All args constructor
    public PortfolioCompletionDto(Long id, String title, String description, String link, String githubRepo,
                                  String status, String type, String completionStatus, String priority,
                                  java.util.List<String> mainTechnologies, Long sourceRepositoryId, String linkType,
                                  Set<Long> skillIds, Set<Long> experienceIds, Boolean protectDescription,
                                  Boolean protectLiveDemoUrl, Boolean protectSkills, Boolean protectExperiences,
                                  CompletionScoresDto completionScores, Double overallCompleteness) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.link = link;
        this.githubRepo = githubRepo;
        this.status = status;
        this.type = type;
        this.completionStatus = completionStatus;
        this.priority = priority;
        this.mainTechnologies = mainTechnologies;
        this.sourceRepositoryId = sourceRepositoryId;
        this.linkType = linkType;
        this.skillIds = skillIds;
        this.experienceIds = experienceIds;
        this.protectDescription = protectDescription;
        this.protectLiveDemoUrl = protectLiveDemoUrl;
        this.protectSkills = protectSkills;
        this.protectExperiences = protectExperiences;
        this.completionScores = completionScores;
        this.overallCompleteness = overallCompleteness;
    }

    // Builder pattern
    public static PortfolioCompletionDtoBuilder builder() {
        return new PortfolioCompletionDtoBuilder();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getGithubRepo() {
        return githubRepo;
    }

    public void setGithubRepo(String githubRepo) {
        this.githubRepo = githubRepo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(String completionStatus) {
        this.completionStatus = completionStatus;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public java.util.List<String> getMainTechnologies() {
        return mainTechnologies;
    }

    public void setMainTechnologies(java.util.List<String> mainTechnologies) {
        this.mainTechnologies = mainTechnologies;
    }

    public Long getSourceRepositoryId() {
        return sourceRepositoryId;
    }

    public void setSourceRepositoryId(Long sourceRepositoryId) {
        this.sourceRepositoryId = sourceRepositoryId;
    }

    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public Set<Long> getSkillIds() {
        return skillIds;
    }

    public void setSkillIds(Set<Long> skillIds) {
        this.skillIds = skillIds;
    }

    public Set<Long> getExperienceIds() {
        return experienceIds;
    }

    public void setExperienceIds(Set<Long> experienceIds) {
        this.experienceIds = experienceIds;
    }

    public Boolean getProtectDescription() {
        return protectDescription;
    }

    public void setProtectDescription(Boolean protectDescription) {
        this.protectDescription = protectDescription;
    }

    public Boolean getProtectLiveDemoUrl() {
        return protectLiveDemoUrl;
    }

    public void setProtectLiveDemoUrl(Boolean protectLiveDemoUrl) {
        this.protectLiveDemoUrl = protectLiveDemoUrl;
    }

    public Boolean getProtectSkills() {
        return protectSkills;
    }

    public void setProtectSkills(Boolean protectSkills) {
        this.protectSkills = protectSkills;
    }

    public Boolean getProtectExperiences() {
        return protectExperiences;
    }

    public void setProtectExperiences(Boolean protectExperiences) {
        this.protectExperiences = protectExperiences;
    }

    public CompletionScoresDto getCompletionScores() {
        return completionScores;
    }

    public void setCompletionScores(CompletionScoresDto completionScores) {
        this.completionScores = completionScores;
    }

    public Double getOverallCompleteness() {
        return overallCompleteness;
    }

    public void setOverallCompleteness(Double overallCompleteness) {
        this.overallCompleteness = overallCompleteness;
    }

    // Builder class
    public static class PortfolioCompletionDtoBuilder {
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
        private Set<Long> skillIds;
        private Set<Long> experienceIds;
        private Boolean protectDescription;
        private Boolean protectLiveDemoUrl;
        private Boolean protectSkills;
        private Boolean protectExperiences;
        private CompletionScoresDto completionScores;
        private Double overallCompleteness;

        public PortfolioCompletionDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public PortfolioCompletionDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public PortfolioCompletionDtoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public PortfolioCompletionDtoBuilder link(String link) {
            this.link = link;
            return this;
        }

        public PortfolioCompletionDtoBuilder githubRepo(String githubRepo) {
            this.githubRepo = githubRepo;
            return this;
        }

        public PortfolioCompletionDtoBuilder status(String status) {
            this.status = status;
            return this;
        }

        public PortfolioCompletionDtoBuilder type(String type) {
            this.type = type;
            return this;
        }

        public PortfolioCompletionDtoBuilder completionStatus(String completionStatus) {
            this.completionStatus = completionStatus;
            return this;
        }

        public PortfolioCompletionDtoBuilder priority(String priority) {
            this.priority = priority;
            return this;
        }

        public PortfolioCompletionDtoBuilder mainTechnologies(java.util.List<String> mainTechnologies) {
            this.mainTechnologies = mainTechnologies;
            return this;
        }

        public PortfolioCompletionDtoBuilder sourceRepositoryId(Long sourceRepositoryId) {
            this.sourceRepositoryId = sourceRepositoryId;
            return this;
        }

        public PortfolioCompletionDtoBuilder linkType(String linkType) {
            this.linkType = linkType;
            return this;
        }

        public PortfolioCompletionDtoBuilder skillIds(Set<Long> skillIds) {
            this.skillIds = skillIds;
            return this;
        }

        public PortfolioCompletionDtoBuilder experienceIds(Set<Long> experienceIds) {
            this.experienceIds = experienceIds;
            return this;
        }

        public PortfolioCompletionDtoBuilder protectDescription(Boolean protectDescription) {
            this.protectDescription = protectDescription;
            return this;
        }

        public PortfolioCompletionDtoBuilder protectLiveDemoUrl(Boolean protectLiveDemoUrl) {
            this.protectLiveDemoUrl = protectLiveDemoUrl;
            return this;
        }

        public PortfolioCompletionDtoBuilder protectSkills(Boolean protectSkills) {
            this.protectSkills = protectSkills;
            return this;
        }

        public PortfolioCompletionDtoBuilder protectExperiences(Boolean protectExperiences) {
            this.protectExperiences = protectExperiences;
            return this;
        }

        public PortfolioCompletionDtoBuilder completionScores(CompletionScoresDto completionScores) {
            this.completionScores = completionScores;
            return this;
        }

        public PortfolioCompletionDtoBuilder overallCompleteness(Double overallCompleteness) {
            this.overallCompleteness = overallCompleteness;
            return this;
        }

        public PortfolioCompletionDto build() {
            return new PortfolioCompletionDto(id, title, description, link, githubRepo, status, type,
                    completionStatus, priority, mainTechnologies, sourceRepositoryId, linkType, skillIds,
                    experienceIds, protectDescription, protectLiveDemoUrl, protectSkills, protectExperiences,
                    completionScores, overallCompleteness);
        }
    }

    public static class CompletionScoresDto {
        private Double basicInfo;        // title, description
        private Double links;           // githubRepo, live demo
        private Double metadata;        // technologies, status, type
        private Double enrichment;      // skills, experiences from AI
        private Double documentation;   // README analysis completion

        // Default constructor
        public CompletionScoresDto() {
        }

        // All args constructor
        public CompletionScoresDto(Double basicInfo, Double links, Double metadata, Double enrichment, Double documentation) {
            this.basicInfo = basicInfo;
            this.links = links;
            this.metadata = metadata;
            this.enrichment = enrichment;
            this.documentation = documentation;
        }

        // Builder pattern
        public static CompletionScoresDtoBuilder builder() {
            return new CompletionScoresDtoBuilder();
        }

        // Getters and Setters
        public Double getBasicInfo() {
            return basicInfo;
        }

        public void setBasicInfo(Double basicInfo) {
            this.basicInfo = basicInfo;
        }

        public Double getLinks() {
            return links;
        }

        public void setLinks(Double links) {
            this.links = links;
        }

        public Double getMetadata() {
            return metadata;
        }

        public void setMetadata(Double metadata) {
            this.metadata = metadata;
        }

        public Double getEnrichment() {
            return enrichment;
        }

        public void setEnrichment(Double enrichment) {
            this.enrichment = enrichment;
        }

        public Double getDocumentation() {
            return documentation;
        }

        public void setDocumentation(Double documentation) {
            this.documentation = documentation;
        }

        // Builder class
        public static class CompletionScoresDtoBuilder {
            private Double basicInfo;
            private Double links;
            private Double metadata;
            private Double enrichment;
            private Double documentation;

            public CompletionScoresDtoBuilder basicInfo(Double basicInfo) {
                this.basicInfo = basicInfo;
                return this;
            }

            public CompletionScoresDtoBuilder links(Double links) {
                this.links = links;
                return this;
            }

            public CompletionScoresDtoBuilder metadata(Double metadata) {
                this.metadata = metadata;
                return this;
            }

            public CompletionScoresDtoBuilder enrichment(Double enrichment) {
                this.enrichment = enrichment;
                return this;
            }

            public CompletionScoresDtoBuilder documentation(Double documentation) {
                this.documentation = documentation;
                return this;
            }

            public CompletionScoresDto build() {
                return new CompletionScoresDto(basicInfo, links, metadata, enrichment, documentation);
            }
        }
    }
}
