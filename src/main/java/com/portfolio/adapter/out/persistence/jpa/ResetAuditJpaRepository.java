package com.portfolio.adapter.out.persistence.jpa;

import com.portfolio.core.domain.admin.ResetStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResetAuditJpaRepository extends JpaRepository<ResetAuditJpaEntity, Long> {
    
    Optional<ResetAuditJpaEntity> findByJobId(String jobId);
    
    @Query("SELECT r FROM ResetAuditJpaEntity r WHERE r.status = :status ORDER BY r.startedAt DESC")
    List<ResetAuditJpaEntity> findByStatusOrderByStartedAtDesc(ResetStatus status);
    
    @Query("SELECT r FROM ResetAuditJpaEntity r WHERE r.status IN ('STARTED', 'IN_PROGRESS') ORDER BY r.startedAt DESC")
    List<ResetAuditJpaEntity> findActiveJobs();
    
    @Query("SELECT r FROM ResetAuditJpaEntity r ORDER BY r.startedAt DESC")
    List<ResetAuditJpaEntity> findAllOrderByStartedAtDesc(Pageable pageable);
}