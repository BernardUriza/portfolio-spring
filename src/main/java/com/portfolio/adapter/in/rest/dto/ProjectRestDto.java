package com.portfolio.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ProjectRestDto {
    
    private Long id;
    
    @NotBlank(message = "Project title cannot be blank")
    @Size(max = 200, message = "Project title cannot exceed 200 characters")
    private String title;
    
    @NotBlank(message = "Project description cannot be blank")
    @Size(max = 1000, message = "Project description cannot exceed 1000 characters")
    private String description;
    
    private String link;
    
    private String githubRepo;
    
    @NotNull(message = "Created date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;
    
    private Integer estimatedDurationWeeks;
    
    private String status;
    
    private String type;
    
    private List<String> mainTechnologies;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public ProjectRestDto() {}

    public ProjectRestDto(Long id, String title, String description, String link, String githubRepo, LocalDate createdDate,
                          Integer estimatedDurationWeeks, String status, String type, List<String> mainTechnologies,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.link = link;
        this.githubRepo = githubRepo;
        this.createdDate = createdDate;
        this.estimatedDurationWeeks = estimatedDurationWeeks;
        this.status = status;
        this.type = type;
        this.mainTechnologies = mainTechnologies;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
