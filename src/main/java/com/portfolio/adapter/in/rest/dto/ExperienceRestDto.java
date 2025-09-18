package com.portfolio.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.portfolio.core.domain.experience.ExperienceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@JsonDeserialize(builder = ExperienceRestDto.Builder.class)
public class ExperienceRestDto {
    
    private Long id;
    
    @NotBlank(message = "Job title cannot be blank")
    @Size(max = 200, message = "Job title cannot exceed 200 characters")
    private String jobTitle;
    
    @NotBlank(message = "Company name cannot be blank")
    @Size(max = 200, message = "Company name cannot exceed 200 characters")
    private String companyName;
    
    private String companyUrl;
    
    private String location;
    
    @NotNull(message = "Experience type is required")
    private ExperienceType type;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    private Boolean isCurrentPosition;
    
    private List<String> achievements;
    
    private List<String> technologies;
    
    private Set<Long> skillIds;
    
    private String companyLogoUrl;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public ExperienceRestDto() {}

    public ExperienceRestDto(Long id, String jobTitle, String companyName, String companyUrl, String location,
                             ExperienceType type, String description, LocalDate startDate, LocalDate endDate,
                             Boolean isCurrentPosition, List<String> achievements, List<String> technologies,
                             Set<Long> skillIds, String companyLogoUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.jobTitle = jobTitle;
        this.companyName = companyName;
        this.companyUrl = companyUrl;
        this.location = location;
        this.type = type;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCurrentPosition = isCurrentPosition;
        this.achievements = achievements;
        this.technologies = technologies;
        this.skillIds = skillIds;
        this.companyLogoUrl = companyLogoUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getCompanyUrl() { return companyUrl; }
    public void setCompanyUrl(String companyUrl) { this.companyUrl = companyUrl; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public ExperienceType getType() { return type; }
    public void setType(ExperienceType type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Boolean getIsCurrentPosition() { return isCurrentPosition; }
    public void setIsCurrentPosition(Boolean isCurrentPosition) { this.isCurrentPosition = isCurrentPosition; }
    public List<String> getAchievements() { return achievements; }
    public void setAchievements(List<String> achievements) { this.achievements = achievements; }
    public List<String> getTechnologies() { return technologies; }
    public void setTechnologies(List<String> technologies) { this.technologies = technologies; }
    public Set<Long> getSkillIds() { return skillIds; }
    public void setSkillIds(Set<Long> skillIds) { this.skillIds = skillIds; }
    public String getCompanyLogoUrl() { return companyLogoUrl; }
    public void setCompanyLogoUrl(String companyLogoUrl) { this.companyLogoUrl = companyLogoUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private Long id;
        private String jobTitle;
        private String companyName;
        private String companyUrl;
        private String location;
        private ExperienceType type;
        private String description;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isCurrentPosition;
        private List<String> achievements;
        private List<String> technologies;
        private Set<Long> skillIds;
        private String companyLogoUrl;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder jobTitle(String jobTitle) { this.jobTitle = jobTitle; return this; }
        public Builder companyName(String companyName) { this.companyName = companyName; return this; }
        public Builder companyUrl(String companyUrl) { this.companyUrl = companyUrl; return this; }
        public Builder location(String location) { this.location = location; return this; }
        public Builder type(ExperienceType type) { this.type = type; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public Builder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public Builder isCurrentPosition(Boolean isCurrentPosition) { this.isCurrentPosition = isCurrentPosition; return this; }
        public Builder achievements(List<String> achievements) { this.achievements = achievements; return this; }
        public Builder technologies(List<String> technologies) { this.technologies = technologies; return this; }
        public Builder skillIds(Set<Long> skillIds) { this.skillIds = skillIds; return this; }
        public Builder companyLogoUrl(String companyLogoUrl) { this.companyLogoUrl = companyLogoUrl; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public ExperienceRestDto build() {
            return new ExperienceRestDto(id, jobTitle, companyName, companyUrl, location, type, description,
                    startDate, endDate, isCurrentPosition, achievements, technologies, skillIds,
                    companyLogoUrl, createdAt, updatedAt);
        }
    }
}
