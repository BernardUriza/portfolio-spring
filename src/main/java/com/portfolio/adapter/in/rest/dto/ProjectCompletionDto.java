package com.portfolio.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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

    public ProjectCompletionDto() {}

    public ProjectCompletionDto(Long projectId, Long repositoryId, String repositoryFullName, String repositoryUrl,
                                Integer repositoryStars, String defaultBranch, String title, String description,
                                String link, String githubRepo, LocalDate createdDate, Integer estimatedDurationWeeks,
                                String status, String type, List<String> mainTechnologies, Set<Long> skillIds,
                                Set<Long> experienceIds, String completionStatus, String priority,
                                FieldProtectionDto protection, CompletenessDto completeness,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.projectId = projectId;
        this.repositoryId = repositoryId;
        this.repositoryFullName = repositoryFullName;
        this.repositoryUrl = repositoryUrl;
        this.repositoryStars = repositoryStars;
        this.defaultBranch = defaultBranch;
        this.title = title;
        this.description = description;
        this.link = link;
        this.githubRepo = githubRepo;
        this.createdDate = createdDate;
        this.estimatedDurationWeeks = estimatedDurationWeeks;
        this.status = status;
        this.type = type;
        this.mainTechnologies = mainTechnologies;
        this.skillIds = skillIds;
        this.experienceIds = experienceIds;
        this.completionStatus = completionStatus;
        this.priority = priority;
        this.protection = protection;
        this.completeness = completeness;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getRepositoryId() { return repositoryId; }
    public void setRepositoryId(Long repositoryId) { this.repositoryId = repositoryId; }
    public String getRepositoryFullName() { return repositoryFullName; }
    public void setRepositoryFullName(String repositoryFullName) { this.repositoryFullName = repositoryFullName; }
    public String getRepositoryUrl() { return repositoryUrl; }
    public void setRepositoryUrl(String repositoryUrl) { this.repositoryUrl = repositoryUrl; }
    public Integer getRepositoryStars() { return repositoryStars; }
    public void setRepositoryStars(Integer repositoryStars) { this.repositoryStars = repositoryStars; }
    public String getDefaultBranch() { return defaultBranch; }
    public void setDefaultBranch(String defaultBranch) { this.defaultBranch = defaultBranch; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public String getGithubRepo() { return githubRepo; }
    public void setGithubRepo(String githubRepo) { this.githubRepo = githubRepo; }
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    public Integer getEstimatedDurationWeeks() { return estimatedDurationWeeks; }
    public void setEstimatedDurationWeeks(Integer estimatedDurationWeeks) { this.estimatedDurationWeeks = estimatedDurationWeeks; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public List<String> getMainTechnologies() { return mainTechnologies; }
    public void setMainTechnologies(List<String> mainTechnologies) { this.mainTechnologies = mainTechnologies; }
    public Set<Long> getSkillIds() { return skillIds; }
    public void setSkillIds(Set<Long> skillIds) { this.skillIds = skillIds; }
    public Set<Long> getExperienceIds() { return experienceIds; }
    public void setExperienceIds(Set<Long> experienceIds) { this.experienceIds = experienceIds; }
    public String getCompletionStatus() { return completionStatus; }
    public void setCompletionStatus(String completionStatus) { this.completionStatus = completionStatus; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public FieldProtectionDto getProtection() { return protection; }
    public void setProtection(FieldProtectionDto protection) { this.protection = protection; }
    public CompletenessDto getCompleteness() { return completeness; }
    public void setCompleteness(CompletenessDto completeness) { this.completeness = completeness; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public static class FieldProtectionDto {
        private Boolean description;
        private Boolean liveDemoUrl;
        private Boolean skills;
        private Boolean experiences;

        public FieldProtectionDto() {}
        public FieldProtectionDto(Boolean description, Boolean liveDemoUrl, Boolean skills, Boolean experiences) {
            this.description = description;
            this.liveDemoUrl = liveDemoUrl;
            this.skills = skills;
            this.experiences = experiences;
        }
        public Boolean getDescription() { return description; }
        public void setDescription(Boolean description) { this.description = description; }
        public Boolean getLiveDemoUrl() { return liveDemoUrl; }
        public void setLiveDemoUrl(Boolean liveDemoUrl) { this.liveDemoUrl = liveDemoUrl; }
        public Boolean getSkills() { return skills; }
        public void setSkills(Boolean skills) { this.skills = skills; }
        public Boolean getExperiences() { return experiences; }
        public void setExperiences(Boolean experiences) { this.experiences = experiences; }
    }
    
    public static class CompletenessDto {
        private Integer score;
        private List<String> missing;

        public CompletenessDto() {}
        public CompletenessDto(Integer score, List<String> missing) {
            this.score = score;
            this.missing = missing;
        }
        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
        public List<String> getMissing() { return missing; }
        public void setMissing(List<String> missing) { this.missing = missing; }
    }
}
