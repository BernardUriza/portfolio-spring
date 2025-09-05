package com.portfolio.adapter.out.persistence.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectJpaRepository extends JpaRepository<ProjectJpaEntity, Long> {
    
    List<ProjectJpaEntity> findByStatus(ProjectJpaEntity.ProjectStatusJpa status);
    
    List<ProjectJpaEntity> findByType(ProjectJpaEntity.ProjectTypeJpa type);
    
    @Query("SELECT p FROM ProjectJpaEntity p JOIN p.mainTechnologies t WHERE t = :technology")
    List<ProjectJpaEntity> findByTechnology(@Param("technology") String technology);
    
    List<ProjectJpaEntity> findAllBy(Pageable pageable);
}