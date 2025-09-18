package com.portfolio.adapter.out.persistence.jpa;

import com.portfolio.core.domain.experience.ExperienceType;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "experiences",
       indexes = {
           @Index(name = "idx_exp_type", columnList = "type"),
           @Index(name = "idx_exp_current", columnList = "is_current_position"),
           @Index(name = "idx_exp_company", columnList = "company_name"),
           @Index(name = "idx_exp_start_date", columnList = "start_date"),
           @Index(name = "idx_exp_end_date", columnList = "end_date"),
           @Index(name = "idx_exp_type_start", columnList = "type, start_date"),
           @Index(name = "idx_exp_current_start", columnList = "is_current_position, start_date")
       })
@EntityListeners(AuditingEntityListener.class)
public class ExperienceJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "job_title", nullable = false, length = 200)
    private String jobTitle;
    
    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;
    
    @Column(name = "company_url")
    private String companyUrl;
    
    @Column(name = "location")
    private String location;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ExperienceType type;
    
    @Column(name = "description", length = 2000)
    private String description;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "is_current_position")
    private Boolean isCurrentPosition = true;
    
    @ElementCollection
    @CollectionTable(name = "experience_achievements", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(name = "achievement")
    private List<String> achievements = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "experience_technologies", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(name = "technology")
    private List<String> technologies = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "experience_skill_ids", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(name = "skill_id")
    private Set<Long> skillIds = new HashSet<>();
    
    @Column(name = "company_logo_url")
    private String companyLogoUrl;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;

    public ExperienceJpaEntity() {}

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
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public static final class Builder {
        private final ExperienceJpaEntity e = new ExperienceJpaEntity();
        public Builder id(Long id) { e.setId(id); return this; }
        public Builder jobTitle(String v) { e.setJobTitle(v); return this; }
        public Builder companyName(String v) { e.setCompanyName(v); return this; }
        public Builder companyUrl(String v) { e.setCompanyUrl(v); return this; }
        public Builder location(String v) { e.setLocation(v); return this; }
        public Builder type(ExperienceType v) { e.setType(v); return this; }
        public Builder description(String v) { e.setDescription(v); return this; }
        public Builder startDate(LocalDate v) { e.setStartDate(v); return this; }
        public Builder endDate(LocalDate v) { e.setEndDate(v); return this; }
        public Builder isCurrentPosition(Boolean v) { e.setIsCurrentPosition(v); return this; }
        public Builder achievements(List<String> v) { e.setAchievements(v); return this; }
        public Builder technologies(List<String> v) { e.setTechnologies(v); return this; }
        public Builder skillIds(Set<Long> v) { e.setSkillIds(v); return this; }
        public Builder companyLogoUrl(String v) { e.setCompanyLogoUrl(v); return this; }
        public Builder createdAt(LocalDateTime v) { e.setCreatedAt(v); return this; }
        public Builder updatedAt(LocalDateTime v) { e.setUpdatedAt(v); return this; }
        public Builder version(Long v) { e.setVersion(v); return this; }
        public ExperienceJpaEntity build() { return e; }
    }
}
