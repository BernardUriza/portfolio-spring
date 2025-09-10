package com.portfolio.adapter.out.persistence.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioProjectJpaRepository extends JpaRepository<PortfolioProjectJpaEntity, Long> {
    
    List<PortfolioProjectJpaEntity> findByStatus(PortfolioProjectJpaEntity.ProjectStatusJpa status);
    
    List<PortfolioProjectJpaEntity> findByCompletionStatus(PortfolioProjectJpaEntity.ProjectCompletionStatusJpa completionStatus);
    
    @Query("SELECT p FROM PortfolioProjectJpaEntity p WHERE p.sourceRepositoryId IS NOT NULL")
    List<PortfolioProjectJpaEntity> findLinkedProjects();
    
    @Query("SELECT p FROM PortfolioProjectJpaEntity p WHERE p.sourceRepositoryId IS NULL")
    List<PortfolioProjectJpaEntity> findUnlinkedProjects();
    
    Optional<PortfolioProjectJpaEntity> findBySourceRepositoryId(Long sourceRepositoryId);
    
    @Query("SELECT p FROM PortfolioProjectJpaEntity p ORDER BY p.updatedAt DESC")
    List<PortfolioProjectJpaEntity> findAllOrderByUpdatedAtDesc();
    
    @Query("SELECT p FROM PortfolioProjectJpaEntity p ORDER BY p.updatedAt DESC")
    List<PortfolioProjectJpaEntity> findAllOrderByUpdatedAtDesc(Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM PortfolioProjectJpaEntity p WHERE p.completionStatus = :status")
    Long countByCompletionStatus(@Param("status") PortfolioProjectJpaEntity.ProjectCompletionStatusJpa status);
}