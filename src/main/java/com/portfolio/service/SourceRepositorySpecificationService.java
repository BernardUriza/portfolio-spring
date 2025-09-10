package com.portfolio.service;

import com.portfolio.adapter.out.persistence.jpa.SourceRepositoryJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.SourceRepositoryJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SourceRepositorySpecificationService {
    
    private final SourceRepositoryJpaRepository sourceRepositoryRepository;
    
    /**
     * Search source repositories with dynamic criteria
     */
    @Cacheable(value = "portfolio-projects", 
               key = "'source_' + #criteria.hashCode() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<SourceRepositoryJpaEntity> searchRepositories(SourceRepositorySearchCriteria criteria, Pageable pageable) {
        log.debug("Searching source repositories with criteria: {}", criteria);
        
        Specification<SourceRepositoryJpaEntity> spec = buildSpecification(criteria);
        return sourceRepositoryRepository.findAll(spec, pageable);
    }
    
    /**
     * Count repositories matching criteria
     */
    public long countRepositories(SourceRepositorySearchCriteria criteria) {
        Specification<SourceRepositoryJpaEntity> spec = buildSpecification(criteria);
        return sourceRepositoryRepository.count(spec);
    }
    
    /**
     * Find repositories suitable for AI analysis
     */
    @Cacheable(value = "portfolio-projects", key = "'ai_suitable'")
    public List<SourceRepositoryJpaEntity> findRepositoriesSuitableForAI() {
        log.debug("Finding repositories suitable for AI analysis");
        
        Specification<SourceRepositoryJpaEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Successfully synced repositories
            predicates.add(cb.equal(root.get("syncStatus"), 
                SourceRepositoryJpaEntity.SyncStatus.SYNCED));
            
            // Has description or README content
            predicates.add(cb.or(
                cb.and(cb.isNotNull(root.get("description")), 
                       cb.notEqual(root.get("description"), "")),
                cb.and(cb.isNotNull(root.get("readmeMarkdown")), 
                       cb.notEqual(root.get("readmeMarkdown"), ""))
            ));
            
            // Minimum star count for quality
            predicates.add(cb.greaterThanOrEqualTo(root.get("starsCount"), 0));
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        return sourceRepositoryRepository.findAll(spec);
    }
    
    /**
     * Build dynamic specification from criteria
     */
    private Specification<SourceRepositoryJpaEntity> buildSpecification(SourceRepositorySearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Name search
            if (criteria.getNameContains() != null && !criteria.getNameContains().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + criteria.getNameContains().toLowerCase() + "%"
                ));
            }
            
            // Full name search
            if (criteria.getFullNameContains() != null && !criteria.getFullNameContains().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("fullName")),
                    "%" + criteria.getFullNameContains().toLowerCase() + "%"
                ));
            }
            
            // Description search
            if (criteria.getDescriptionContains() != null && !criteria.getDescriptionContains().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),
                    "%" + criteria.getDescriptionContains().toLowerCase() + "%"
                ));
            }
            
            // Language filter
            if (criteria.getLanguages() != null && !criteria.getLanguages().isEmpty()) {
                predicates.add(root.get("language").in(criteria.getLanguages()));
            }
            
            // Sync status filter
            if (criteria.getSyncStatuses() != null && !criteria.getSyncStatuses().isEmpty()) {
                predicates.add(root.get("syncStatus").in(criteria.getSyncStatuses()));
            }
            
            // Star count range
            if (criteria.getMinStars() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("starsCount"), criteria.getMinStars()));
            }
            
            if (criteria.getMaxStars() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("starsCount"), criteria.getMaxStars()));
            }
            
            // Fork count range
            if (criteria.getMinForks() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("forksCount"), criteria.getMinForks()));
            }
            
            if (criteria.getMaxForks() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("forksCount"), criteria.getMaxForks()));
            }
            
            // Date range filters
            if (criteria.getUpdatedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("updatedAt"), criteria.getUpdatedAfter()));
            }
            
            if (criteria.getUpdatedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("updatedAt"), criteria.getUpdatedBefore()));
            }
            
            if (criteria.getLastSyncAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("lastSyncAt"), criteria.getLastSyncAfter()));
            }
            
            if (criteria.getLastSyncBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("lastSyncAt"), criteria.getLastSyncBefore()));
            }
            
            // Content filters
            if (criteria.getHasReadme() != null) {
                if (criteria.getHasReadme()) {
                    predicates.add(criteriaBuilder.and(
                        criteriaBuilder.isNotNull(root.get("readmeMarkdown")),
                        criteriaBuilder.notEqual(root.get("readmeMarkdown"), "")
                    ));
                } else {
                    predicates.add(criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("readmeMarkdown")),
                        criteriaBuilder.equal(root.get("readmeMarkdown"), "")
                    ));
                }
            }
            
            if (criteria.getHasTopics() != null) {
                if (criteria.getHasTopics()) {
                    predicates.add(criteriaBuilder.isNotEmpty(root.get("topics")));
                } else {
                    predicates.add(criteriaBuilder.isEmpty(root.get("topics")));
                }
            }
            
            // Note: isLinked filter would need a join or subquery with PortfolioProject
            // For now, we'll skip this complex filter as it requires cross-entity queries
            // This can be implemented later with a more sophisticated approach
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * Search criteria for source repositories
     */
    public static class SourceRepositorySearchCriteria {
        private String nameContains;
        private String fullNameContains;
        private String descriptionContains;
        private Set<String> languages;
        private Set<SourceRepositoryJpaEntity.SyncStatus> syncStatuses;
        private Integer minStars;
        private Integer maxStars;
        private Integer minForks;
        private Integer maxForks;
        private LocalDateTime updatedAfter;
        private LocalDateTime updatedBefore;
        private LocalDateTime lastSyncAfter;
        private LocalDateTime lastSyncBefore;
        private Boolean hasReadme;
        private Boolean hasTopics;
        private Boolean isLinked;
        
        // Getters and setters
        public String getNameContains() { return nameContains; }
        public void setNameContains(String nameContains) { this.nameContains = nameContains; }
        
        public String getFullNameContains() { return fullNameContains; }
        public void setFullNameContains(String fullNameContains) { this.fullNameContains = fullNameContains; }
        
        public String getDescriptionContains() { return descriptionContains; }
        public void setDescriptionContains(String descriptionContains) { this.descriptionContains = descriptionContains; }
        
        public Set<String> getLanguages() { return languages; }
        public void setLanguages(Set<String> languages) { this.languages = languages; }
        
        public Set<SourceRepositoryJpaEntity.SyncStatus> getSyncStatuses() { return syncStatuses; }
        public void setSyncStatuses(Set<SourceRepositoryJpaEntity.SyncStatus> syncStatuses) { this.syncStatuses = syncStatuses; }
        
        public Integer getMinStars() { return minStars; }
        public void setMinStars(Integer minStars) { this.minStars = minStars; }
        
        public Integer getMaxStars() { return maxStars; }
        public void setMaxStars(Integer maxStars) { this.maxStars = maxStars; }
        
        public Integer getMinForks() { return minForks; }
        public void setMinForks(Integer minForks) { this.minForks = minForks; }
        
        public Integer getMaxForks() { return maxForks; }
        public void setMaxForks(Integer maxForks) { this.maxForks = maxForks; }
        
        public LocalDateTime getUpdatedAfter() { return updatedAfter; }
        public void setUpdatedAfter(LocalDateTime updatedAfter) { this.updatedAfter = updatedAfter; }
        
        public LocalDateTime getUpdatedBefore() { return updatedBefore; }
        public void setUpdatedBefore(LocalDateTime updatedBefore) { this.updatedBefore = updatedBefore; }
        
        public LocalDateTime getLastSyncAfter() { return lastSyncAfter; }
        public void setLastSyncAfter(LocalDateTime lastSyncAfter) { this.lastSyncAfter = lastSyncAfter; }
        
        public LocalDateTime getLastSyncBefore() { return lastSyncBefore; }
        public void setLastSyncBefore(LocalDateTime lastSyncBefore) { this.lastSyncBefore = lastSyncBefore; }
        
        public Boolean getHasReadme() { return hasReadme; }
        public void setHasReadme(Boolean hasReadme) { this.hasReadme = hasReadme; }
        
        public Boolean getHasTopics() { return hasTopics; }
        public void setHasTopics(Boolean hasTopics) { this.hasTopics = hasTopics; }
        
        public Boolean getIsLinked() { return isLinked; }
        public void setIsLinked(Boolean isLinked) { this.isLinked = isLinked; }
        
        @Override
        public int hashCode() {
            return java.util.Objects.hash(nameContains, fullNameContains, descriptionContains, 
                languages, syncStatuses, minStars, maxStars, minForks, maxForks, 
                updatedAfter, updatedBefore, lastSyncAfter, lastSyncBefore, 
                hasReadme, hasTopics, isLinked);
        }
        
        @Override
        public String toString() {
            return "SourceRepositorySearchCriteria{" +
                "nameContains='" + nameContains + '\'' +
                ", fullNameContains='" + fullNameContains + '\'' +
                ", descriptionContains='" + descriptionContains + '\'' +
                ", languages=" + languages +
                ", syncStatuses=" + syncStatuses +
                ", minStars=" + minStars +
                ", maxStars=" + maxStars +
                ", minForks=" + minForks +
                ", maxForks=" + maxForks +
                ", updatedAfter=" + updatedAfter +
                ", updatedBefore=" + updatedBefore +
                ", lastSyncAfter=" + lastSyncAfter +
                ", lastSyncBefore=" + lastSyncBefore +
                ", hasReadme=" + hasReadme +
                ", hasTopics=" + hasTopics +
                ", isLinked=" + isLinked +
                '}';
        }
    }
}