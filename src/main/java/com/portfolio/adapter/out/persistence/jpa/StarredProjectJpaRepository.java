package com.portfolio.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StarredProjectJpaRepository extends JpaRepository<StarredProjectJpaEntity, Long> {
    
    Optional<StarredProjectJpaEntity> findByGithubId(Long githubId);
    
    Optional<StarredProjectJpaEntity> findByGithubRepoUrl(String githubRepoUrl);
    
    List<StarredProjectJpaEntity> findBySyncStatus(StarredProjectJpaEntity.SyncStatus syncStatus);
    
    @Query("SELECT s FROM StarredProjectJpaEntity s WHERE s.syncStatus = :status ORDER BY s.updatedAt DESC")
    List<StarredProjectJpaEntity> findBySyncStatusOrderByUpdatedAtDesc(@Param("status") StarredProjectJpaEntity.SyncStatus status);
    
    @Query("SELECT COUNT(s) FROM StarredProjectJpaEntity s WHERE s.syncStatus = :status")
    Long countBySyncStatus(@Param("status") StarredProjectJpaEntity.SyncStatus status);
    
    @Query("SELECT s FROM StarredProjectJpaEntity s ORDER BY s.updatedAt DESC")
    List<StarredProjectJpaEntity> findAllOrderByUpdatedAtDesc();
}