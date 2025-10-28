package com.portfolio.adapter.out.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SourceRepositoryJpaRepository extends JpaRepository<SourceRepositoryJpaEntity, Long>, JpaSpecificationExecutor<SourceRepositoryJpaEntity> {
    
    Optional<SourceRepositoryJpaEntity> findByGithubId(Long githubId);
    
    Optional<SourceRepositoryJpaEntity> findByFullName(String fullName);
    
    Optional<SourceRepositoryJpaEntity> findByGithubRepoUrl(String githubRepoUrl);
    
    List<SourceRepositoryJpaEntity> findBySyncStatus(SourceRepositoryJpaEntity.SyncStatus syncStatus);
    
    @Query("SELECT s FROM SourceRepositoryJpaEntity s WHERE s.syncStatus = :status ORDER BY s.updatedAt DESC")
    List<SourceRepositoryJpaEntity> findBySyncStatusOrderByUpdatedAtDesc(@Param("status") SourceRepositoryJpaEntity.SyncStatus status);
    
    @Query("SELECT COUNT(s) FROM SourceRepositoryJpaEntity s WHERE s.syncStatus = :status")
    Long countBySyncStatus(@Param("status") SourceRepositoryJpaEntity.SyncStatus status);
    
    @Query("SELECT s FROM SourceRepositoryJpaEntity s ORDER BY s.updatedAt DESC")
    List<SourceRepositoryJpaEntity> findAllOrderByUpdatedAtDesc();
    
    // Pagination methods for admin controller
    Page<SourceRepositoryJpaEntity> findBySyncStatus(SourceRepositoryJpaEntity.SyncStatus syncStatus, Pageable pageable);
    
    Page<SourceRepositoryJpaEntity> findByLanguage(String language, Pageable pageable);
    
    Page<SourceRepositoryJpaEntity> findBySyncStatusAndLanguage(SourceRepositoryJpaEntity.SyncStatus syncStatus, 
                                                               String language, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM PortfolioProjectJpaEntity p WHERE p.sourceRepositoryId = :sourceId")
    long countLinkedPortfolioProjects(@Param("sourceId") Long sourceId);

    /**
     * Fetch all source repositories with topics eagerly loaded to avoid N+1 queries
     * PERF-006: This replaces findAll() in sync operations
     *
     * Before: 1 SELECT + N queries for topics (N = number of repos)
     * After: 1 SELECT with JOIN FETCH (total 1 query)
     *
     * @return List of all source repositories with topics eagerly fetched
     */
    @Query("SELECT DISTINCT s FROM SourceRepositoryJpaEntity s LEFT JOIN FETCH s.topics")
    List<SourceRepositoryJpaEntity> findAllWithTopics();
}