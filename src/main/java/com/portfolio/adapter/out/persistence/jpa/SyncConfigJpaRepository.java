package com.portfolio.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SyncConfigJpaRepository extends JpaRepository<SyncConfigJpaEntity, Long> {
    
    Optional<SyncConfigJpaEntity> findFirstByOrderByIdAsc();
}