package com.portfolio.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubRepositoryDto {
    
    private Long id;
    
    private String name;
    
    private String description;
    
    @JsonProperty("html_url")
    private String htmlUrl;
    
    @JsonProperty("homepage")
    private String homepage;
    
    @JsonProperty("language")
    private String language;
    
    @JsonProperty("topics")
    private List<String> topics;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    @JsonProperty("pushed_at")
    private LocalDateTime pushedAt;
    
    @JsonProperty("stargazers_count")
    private Integer stargazersCount;
    
    @JsonProperty("forks_count")
    private Integer forksCount;
    
    @JsonProperty("archived")
    private Boolean archived;
    
    @JsonProperty("disabled")
    private Boolean disabled;
    
    @JsonProperty("private")
    private Boolean privateRepo;
    
    @JsonProperty("fork")
    private Boolean fork;
}