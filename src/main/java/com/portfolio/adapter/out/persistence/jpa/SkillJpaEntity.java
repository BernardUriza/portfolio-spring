package com.portfolio.adapter.out.persistence.jpa;

import com.portfolio.core.domain.skill.SkillCategory;
import com.portfolio.core.domain.skill.SkillLevel;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "skills",
       uniqueConstraints = @UniqueConstraint(columnNames = "name"),
       indexes = {
           @Index(name = "idx_skill_category", columnList = "category"),
           @Index(name = "idx_skill_level", columnList = "level"),
           @Index(name = "idx_skill_featured", columnList = "is_featured"),
           @Index(name = "idx_skill_experience", columnList = "years_of_experience"),
           @Index(name = "idx_skill_cat_level", columnList = "category, level"),
           @Index(name = "idx_skill_created_at", columnList = "created_at")
       })
@EntityListeners(AuditingEntityListener.class)
public class SkillJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private SkillCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private SkillLevel level;
    
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;
    
    @Column(name = "is_featured")
    private Boolean isFeatured = false;
    
    @Column(name = "icon_url")
    private String iconUrl;
    
    @Column(name = "documentation_url")
    private String documentationUrl;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;

    public SkillJpaEntity() {}

    public static Builder builder() { return new Builder(); }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public SkillCategory getCategory() { return category; }
    public void setCategory(SkillCategory category) { this.category = category; }
    public SkillLevel getLevel() { return level; }
    public void setLevel(SkillLevel level) { this.level = level; }
    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }
    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public String getDocumentationUrl() { return documentationUrl; }
    public void setDocumentationUrl(String documentationUrl) { this.documentationUrl = documentationUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public static final class Builder {
        private final SkillJpaEntity e = new SkillJpaEntity();
        public Builder id(Long id) { e.setId(id); return this; }
        public Builder name(String v) { e.setName(v); return this; }
        public Builder description(String v) { e.setDescription(v); return this; }
        public Builder category(SkillCategory v) { e.setCategory(v); return this; }
        public Builder level(SkillLevel v) { e.setLevel(v); return this; }
        public Builder yearsOfExperience(Integer v) { e.setYearsOfExperience(v); return this; }
        public Builder isFeatured(Boolean v) { e.setIsFeatured(v); return this; }
        public Builder iconUrl(String v) { e.setIconUrl(v); return this; }
        public Builder documentationUrl(String v) { e.setDocumentationUrl(v); return this; }
        public Builder createdAt(LocalDateTime v) { e.setCreatedAt(v); return this; }
        public Builder updatedAt(LocalDateTime v) { e.setUpdatedAt(v); return this; }
        public Builder version(Long v) { e.setVersion(v); return this; }
        public SkillJpaEntity build() { return e; }
    }
}
