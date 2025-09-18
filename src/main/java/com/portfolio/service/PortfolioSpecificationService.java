package com.portfolio.service;

import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Creado por Bernard Orozco
 */
@Service
public class PortfolioSpecificationService {

    private static final Logger log = LoggerFactory.getLogger(PortfolioSpecificationService.class);

    private final PortfolioProjectJpaRepository portfolioProjectRepository;

    public PortfolioSpecificationService(PortfolioProjectJpaRepository portfolioProjectRepository) {
        this.portfolioProjectRepository = portfolioProjectRepository;
    }
    
    /**
     * Search portfolio projects with dynamic criteria
     */
    @Cacheable(value = "portfolio-projects", 
               key = "#criteria.hashCode() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<PortfolioProjectJpaEntity> searchProjects(PortfolioSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching portfolio projects with criteria: {}", criteria);
        
        Specification<PortfolioProjectJpaEntity> spec = buildSpecification(criteria);
        return portfolioProjectRepository.findAll(spec, pageable);
    }
    
    /**
     * Count projects matching criteria
     */
    public long countProjects(PortfolioSearchCriteria criteria) {
        Specification<PortfolioProjectJpaEntity> spec = buildSpecification(criteria);
        return portfolioProjectRepository.count(spec);
    }
    
    /**
     * Find projects needing curation based on completion scores
     */
    @Cacheable(value = "portfolio-projects", key = "'needs_curation_' + #minCompletionScore")
    public List<PortfolioProjectJpaEntity> findProjectsNeedingCuration(double minCompletionScore) {
        log.debug("Finding projects needing curation with min completion score: {}", minCompletionScore);
        
        Specification<PortfolioProjectJpaEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Projects that are active
            predicates.add(cb.equal(root.get("status"), 
                PortfolioProjectJpaEntity.ProjectStatusJpa.ACTIVE));
            
            // Projects with source repository (for AI analysis)
            predicates.add(cb.isNotNull(root.get("sourceRepositoryId")));
            
            // Projects that don't have protection flags set (can be updated)
            predicates.add(cb.or(
                cb.isNull(root.get("protectDescription")),
                cb.isFalse(root.get("protectDescription"))
            ));
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        return portfolioProjectRepository.findAll(spec);
    }
    
    /**
     * Build dynamic specification from criteria
     */
    private Specification<PortfolioProjectJpaEntity> buildSpecification(PortfolioSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Title search
            if (criteria.getTitleContains() != null && !criteria.getTitleContains().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")),
                    "%" + criteria.getTitleContains().toLowerCase() + "%"
                ));
            }
            
            // Description search
            if (criteria.getDescriptionContains() != null && !criteria.getDescriptionContains().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),
                    "%" + criteria.getDescriptionContains().toLowerCase() + "%"
                ));
            }
            
            // Status filter
            if (criteria.getStatuses() != null && !criteria.getStatuses().isEmpty()) {
                predicates.add(root.get("status").in(criteria.getStatuses()));
            }
            
            // Completion status filter
            if (criteria.getCompletionStatuses() != null && !criteria.getCompletionStatuses().isEmpty()) {
                predicates.add(root.get("completionStatus").in(criteria.getCompletionStatuses()));
            }
            
            // Type filter
            if (criteria.getTypes() != null && !criteria.getTypes().isEmpty()) {
                predicates.add(root.get("type").in(criteria.getTypes()));
            }
            
            // Priority filter
            if (criteria.getPriorities() != null && !criteria.getPriorities().isEmpty()) {
                predicates.add(root.get("priority").in(criteria.getPriorities()));
            }
            
            // Technology filter
            if (criteria.getTechnologies() != null && !criteria.getTechnologies().isEmpty()) {
                for (String tech : criteria.getTechnologies()) {
                    predicates.add(criteriaBuilder.isMember(tech, root.get("mainTechnologies")));
                }
            }
            
            // Source repository filter
            if (criteria.getHasSourceRepository() != null) {
                if (criteria.getHasSourceRepository()) {
                    predicates.add(criteriaBuilder.isNotNull(root.get("sourceRepositoryId")));
                } else {
                    predicates.add(criteriaBuilder.isNull(root.get("sourceRepositoryId")));
                }
            }
            
            // Date range filters
            if (criteria.getCreatedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createdDate"), criteria.getCreatedAfter()));
            }
            
            if (criteria.getCreatedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("createdDate"), criteria.getCreatedBefore()));
            }
            
            if (criteria.getUpdatedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("updatedAt"), criteria.getUpdatedAfter()));
            }
            
            if (criteria.getUpdatedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("updatedAt"), criteria.getUpdatedBefore()));
            }
            
            // Protection status filters
            if (criteria.getProtectionFilters() != null) {
                ProtectionFilters protection = criteria.getProtectionFilters();
                
                if (protection.getDescriptionProtected() != null) {
                    predicates.add(criteriaBuilder.equal(
                        root.get("protectDescription"), protection.getDescriptionProtected()));
                }
                
                if (protection.getLiveDemoProtected() != null) {
                    predicates.add(criteriaBuilder.equal(
                        root.get("protectLiveDemoUrl"), protection.getLiveDemoProtected()));
                }
                
                if (protection.getSkillsProtected() != null) {
                    predicates.add(criteriaBuilder.equal(
                        root.get("protectSkills"), protection.getSkillsProtected()));
                }
                
                if (protection.getExperiencesProtected() != null) {
                    predicates.add(criteriaBuilder.equal(
                        root.get("protectExperiences"), protection.getExperiencesProtected()));
                }
            }
            
            // Minimum estimated duration filter
            if (criteria.getMinEstimatedDurationWeeks() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("estimatedDurationWeeks"), criteria.getMinEstimatedDurationWeeks()));
            }
            
            // Maximum estimated duration filter
            if (criteria.getMaxEstimatedDurationWeeks() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("estimatedDurationWeeks"), criteria.getMaxEstimatedDurationWeeks()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * Search criteria for portfolio projects
     */
    public static class PortfolioSearchCriteria {
        private String titleContains;
        private String descriptionContains;
        private Set<PortfolioProjectJpaEntity.ProjectStatusJpa> statuses;
        private Set<PortfolioProjectJpaEntity.ProjectCompletionStatusJpa> completionStatuses;
        private Set<PortfolioProjectJpaEntity.ProjectTypeJpa> types;
        private Set<PortfolioProjectJpaEntity.ProjectPriorityJpa> priorities;
        private Set<String> technologies;
        private Boolean hasSourceRepository;
        private LocalDate createdAfter;
        private LocalDate createdBefore;
        private LocalDateTime updatedAfter;
        private LocalDateTime updatedBefore;
        private ProtectionFilters protectionFilters;
        private Integer minEstimatedDurationWeeks;
        private Integer maxEstimatedDurationWeeks;
        
        // Getters and setters
        public String getTitleContains() { return titleContains; }
        public void setTitleContains(String titleContains) { this.titleContains = titleContains; }
        
        public String getDescriptionContains() { return descriptionContains; }
        public void setDescriptionContains(String descriptionContains) { this.descriptionContains = descriptionContains; }
        
        public Set<PortfolioProjectJpaEntity.ProjectStatusJpa> getStatuses() { return statuses; }
        public void setStatuses(Set<PortfolioProjectJpaEntity.ProjectStatusJpa> statuses) { this.statuses = statuses; }
        
        public Set<PortfolioProjectJpaEntity.ProjectCompletionStatusJpa> getCompletionStatuses() { return completionStatuses; }
        public void setCompletionStatuses(Set<PortfolioProjectJpaEntity.ProjectCompletionStatusJpa> completionStatuses) { this.completionStatuses = completionStatuses; }
        
        public Set<PortfolioProjectJpaEntity.ProjectTypeJpa> getTypes() { return types; }
        public void setTypes(Set<PortfolioProjectJpaEntity.ProjectTypeJpa> types) { this.types = types; }
        
        public Set<PortfolioProjectJpaEntity.ProjectPriorityJpa> getPriorities() { return priorities; }
        public void setPriorities(Set<PortfolioProjectJpaEntity.ProjectPriorityJpa> priorities) { this.priorities = priorities; }
        
        public Set<String> getTechnologies() { return technologies; }
        public void setTechnologies(Set<String> technologies) { this.technologies = technologies; }
        
        public Boolean getHasSourceRepository() { return hasSourceRepository; }
        public void setHasSourceRepository(Boolean hasSourceRepository) { this.hasSourceRepository = hasSourceRepository; }
        
        public LocalDate getCreatedAfter() { return createdAfter; }
        public void setCreatedAfter(LocalDate createdAfter) { this.createdAfter = createdAfter; }
        
        public LocalDate getCreatedBefore() { return createdBefore; }
        public void setCreatedBefore(LocalDate createdBefore) { this.createdBefore = createdBefore; }
        
        public LocalDateTime getUpdatedAfter() { return updatedAfter; }
        public void setUpdatedAfter(LocalDateTime updatedAfter) { this.updatedAfter = updatedAfter; }
        
        public LocalDateTime getUpdatedBefore() { return updatedBefore; }
        public void setUpdatedBefore(LocalDateTime updatedBefore) { this.updatedBefore = updatedBefore; }
        
        public ProtectionFilters getProtectionFilters() { return protectionFilters; }
        public void setProtectionFilters(ProtectionFilters protectionFilters) { this.protectionFilters = protectionFilters; }
        
        public Integer getMinEstimatedDurationWeeks() { return minEstimatedDurationWeeks; }
        public void setMinEstimatedDurationWeeks(Integer minEstimatedDurationWeeks) { this.minEstimatedDurationWeeks = minEstimatedDurationWeeks; }
        
        public Integer getMaxEstimatedDurationWeeks() { return maxEstimatedDurationWeeks; }
        public void setMaxEstimatedDurationWeeks(Integer maxEstimatedDurationWeeks) { this.maxEstimatedDurationWeeks = maxEstimatedDurationWeeks; }
        
        @Override
        public int hashCode() {
            return java.util.Objects.hash(titleContains, descriptionContains, statuses, 
                completionStatuses, types, priorities, technologies, hasSourceRepository, 
                createdAfter, createdBefore, updatedAfter, updatedBefore, protectionFilters,
                minEstimatedDurationWeeks, maxEstimatedDurationWeeks);
        }
        
        @Override
        public String toString() {
            return "PortfolioSearchCriteria{" +
                "titleContains='" + titleContains + '\'' +
                ", descriptionContains='" + descriptionContains + '\'' +
                ", statuses=" + statuses +
                ", completionStatuses=" + completionStatuses +
                ", types=" + types +
                ", priorities=" + priorities +
                ", technologies=" + technologies +
                ", hasSourceRepository=" + hasSourceRepository +
                ", createdAfter=" + createdAfter +
                ", createdBefore=" + createdBefore +
                ", updatedAfter=" + updatedAfter +
                ", updatedBefore=" + updatedBefore +
                ", protectionFilters=" + protectionFilters +
                ", minEstimatedDurationWeeks=" + minEstimatedDurationWeeks +
                ", maxEstimatedDurationWeeks=" + maxEstimatedDurationWeeks +
                '}';
        }
    }
    
    /**
     * Protection-related search filters
     */
    public static class ProtectionFilters {
        private Boolean descriptionProtected;
        private Boolean liveDemoProtected;
        private Boolean skillsProtected;
        private Boolean experiencesProtected;
        
        public Boolean getDescriptionProtected() { return descriptionProtected; }
        public void setDescriptionProtected(Boolean descriptionProtected) { this.descriptionProtected = descriptionProtected; }
        
        public Boolean getLiveDemoProtected() { return liveDemoProtected; }
        public void setLiveDemoProtected(Boolean liveDemoProtected) { this.liveDemoProtected = liveDemoProtected; }
        
        public Boolean getSkillsProtected() { return skillsProtected; }
        public void setSkillsProtected(Boolean skillsProtected) { this.skillsProtected = skillsProtected; }
        
        public Boolean getExperiencesProtected() { return experiencesProtected; }
        public void setExperiencesProtected(Boolean experiencesProtected) { this.experiencesProtected = experiencesProtected; }
        
        @Override
        public int hashCode() {
            return java.util.Objects.hash(descriptionProtected, liveDemoProtected, skillsProtected, experiencesProtected);
        }
        
        @Override
        public String toString() {
            return "ProtectionFilters{" +
                "descriptionProtected=" + descriptionProtected +
                ", liveDemoProtected=" + liveDemoProtected +
                ", skillsProtected=" + skillsProtected +
                ", experiencesProtected=" + experiencesProtected +
                '}';
        }
    }
}