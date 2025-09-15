package com.portfolio.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SyncConfigJpaRepository extends JpaRepository<SyncConfigJpaEntity, Long> {
    
    Optional<SyncConfigJpaEntity> findFirstByOrderByIdAsc();

    Optional<SyncConfigJpaEntity> findBySingletonKey(String singletonKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from SyncConfigJpaEntity c where c.singletonKey = 'X'")
    Optional<SyncConfigJpaEntity> lockSingleton();
}
