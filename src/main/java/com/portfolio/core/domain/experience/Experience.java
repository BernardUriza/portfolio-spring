package com.portfolio.core.domain.experience;

import com.portfolio.core.domain.shared.DomainEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Experience extends DomainEntity {
    private final Long id;
    private final String jobTitle;
    private final String companyName;
    private final String companyUrl;
    private final String location;
    private final ExperienceType type;
    private final String description;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Boolean isCurrentPosition;
    private final List<String> achievements;
    private final List<String> technologies;
    private final Set<Long> skillIds;
    private final String companyLogoUrl;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Experience(Builder b) {
        this.id = b.id;
        this.jobTitle = b.jobTitle;
        this.companyName = b.companyName;
        this.companyUrl = b.companyUrl;
        this.location = b.location;
        this.type = b.type;
        this.description = b.description;
        this.startDate = b.startDate;
        this.endDate = b.endDate;
        this.isCurrentPosition = b.isCurrentPosition;
        this.achievements = b.achievements != null ? b.achievements : new ArrayList<>();
        this.technologies = b.technologies != null ? b.technologies : new ArrayList<>();
        this.skillIds = b.skillIds != null ? b.skillIds : new HashSet<>();
        this.companyLogoUrl = b.companyLogoUrl;
        this.createdAt = b.createdAt;
        this.updatedAt = b.updatedAt;
    }

    public static Builder builder() { return new Builder(); }
    public Builder toBuilder() { return new Builder(this); }

    public Long getId() { return id; }
    public String getJobTitle() { return jobTitle; }
    public String getCompanyName() { return companyName; }
    public String getCompanyUrl() { return companyUrl; }
    public String getLocation() { return location; }
    public ExperienceType getType() { return type; }
    public String getDescription() { return description; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Boolean getIsCurrentPosition() { return isCurrentPosition; }
    public List<String> getAchievements() { return achievements; }
    public List<String> getTechnologies() { return technologies; }
    public Set<Long> getSkillIds() { return skillIds; }
    public String getCompanyLogoUrl() { return companyLogoUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    public static Experience create(String jobTitle, String companyName, ExperienceType type, 
                                  String description, LocalDate startDate) {
        validateJobTitle(jobTitle);
        validateCompanyName(companyName);
        validateType(type);
        validateStartDate(startDate);
        
        return Experience.builder()
                .jobTitle(jobTitle)
                .companyName(companyName)
                .type(type)
                .description(description)
                .startDate(startDate)
                .isCurrentPosition(true)
                .achievements(new ArrayList<>())
                .technologies(new ArrayList<>())
                .skillIds(new HashSet<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Experience updateBasicInfo(String jobTitle, String companyName, ExperienceType type, String description) {
        validateJobTitle(jobTitle);
        validateCompanyName(companyName);
        validateType(type);
        
        return this.toBuilder()
                .jobTitle(jobTitle)
                .companyName(companyName)
                .type(type)
                .description(description)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Experience updateDates(LocalDate startDate, LocalDate endDate) {
        validateStartDate(startDate);
        validateEndDate(startDate, endDate);
        
        return this.toBuilder()
                .startDate(startDate)
                .endDate(endDate)
                .isCurrentPosition(endDate == null)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Experience endPosition(LocalDate endDate) {
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null when ending a position");
        }
        validateEndDate(this.startDate, endDate);
        
        return this.toBuilder()
                .endDate(endDate)
                .isCurrentPosition(false)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Experience updateCompanyDetails(String companyUrl, String location, String companyLogoUrl) {
        return this.toBuilder()
                .companyUrl(companyUrl)
                .location(location)
                .companyLogoUrl(companyLogoUrl)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Experience addAchievement(String achievement) {
        if (achievement == null || achievement.trim().isEmpty()) {
            throw new IllegalArgumentException("Achievement cannot be null or empty");
        }
        
        List<String> newAchievements = new ArrayList<>(this.achievements);
        newAchievements.add(achievement.trim());
        
        return this.toBuilder()
                .achievements(newAchievements)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Experience removeAchievement(String achievement) {
        List<String> newAchievements = new ArrayList<>(this.achievements);
        newAchievements.remove(achievement);
        
        return this.toBuilder()
                .achievements(newAchievements)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Experience addTechnology(String technology) {
        if (technology == null || technology.trim().isEmpty()) {
            throw new IllegalArgumentException("Technology cannot be null or empty");
        }
        
        List<String> newTechnologies = new ArrayList<>(this.technologies);
        if (!newTechnologies.contains(technology.trim())) {
            newTechnologies.add(technology.trim());
        }
        
        return this.toBuilder()
                .technologies(newTechnologies)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Experience addSkill(Long skillId) {
        if (skillId == null) {
            throw new IllegalArgumentException("Skill ID cannot be null");
        }
        
        Set<Long> newSkillIds = new HashSet<>(this.skillIds);
        newSkillIds.add(skillId);
        
        return this.toBuilder()
                .skillIds(newSkillIds)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Period getDuration() {
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        return Period.between(startDate, end);
    }
    
    public int getDurationInMonths() {
        Period period = getDuration();
        return period.getYears() * 12 + period.getMonths();
    }
    
    public boolean isLongTerm() {
        return getDurationInMonths() >= 12;
    }
    
    public boolean isCurrent() {
        return Boolean.TRUE.equals(isCurrentPosition);
    }
    
    public boolean hasCompanyUrl() {
        return companyUrl != null && !companyUrl.trim().isEmpty();
    }
    
    public boolean hasCompanyLogo() {
        return companyLogoUrl != null && !companyLogoUrl.trim().isEmpty();
    }
    
    private static void validateJobTitle(String jobTitle) {
        if (jobTitle == null || jobTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("Job title cannot be null or empty");
        }
        if (jobTitle.length() > 200) {
            throw new IllegalArgumentException("Job title cannot exceed 200 characters");
        }
    }
    
    private static void validateCompanyName(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be null or empty");
        }
        if (companyName.length() > 200) {
            throw new IllegalArgumentException("Company name cannot exceed 200 characters");
        }
    }
    
    private static void validateType(ExperienceType type) {
        if (type == null) {
            throw new IllegalArgumentException("Experience type cannot be null");
        }
    }
    
    private static void validateStartDate(LocalDate startDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (startDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the future");
        }
    }
    
    private static void validateEndDate(LocalDate startDate, LocalDate endDate) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        if (endDate != null && endDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("End date cannot be in the future");
        }
    }

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

        public Builder() {}
        public Builder(Experience e) {
            this.id = e.id;
            this.jobTitle = e.jobTitle;
            this.companyName = e.companyName;
            this.companyUrl = e.companyUrl;
            this.location = e.location;
            this.type = e.type;
            this.description = e.description;
            this.startDate = e.startDate;
            this.endDate = e.endDate;
            this.isCurrentPosition = e.isCurrentPosition;
            this.achievements = e.achievements;
            this.technologies = e.technologies;
            this.skillIds = e.skillIds;
            this.companyLogoUrl = e.companyLogoUrl;
            this.createdAt = e.createdAt;
            this.updatedAt = e.updatedAt;
        }
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
        public Experience build() { return new Experience(this); }
    }
}
