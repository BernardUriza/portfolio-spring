package com.portfolio.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SourceRepositoryJpaRepository extends JpaRepository<SourceRepositoryJpaEntity, Long> {
    
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
}