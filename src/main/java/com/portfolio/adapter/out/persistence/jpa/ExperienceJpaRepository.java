package com.portfolio.adapter.out.persistence.jpa;

import com.portfolio.core.domain.experience.ExperienceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperienceJpaRepository extends JpaRepository<ExperienceJpaEntity, Long> {
    
    List<ExperienceJpaEntity> findByType(ExperienceType type);
    
    List<ExperienceJpaEntity> findByIsCurrentPositionTrue();
    
    List<ExperienceJpaEntity> findByCompanyNameIgnoreCase(String companyName);
    
    @Query("SELECT e FROM ExperienceJpaEntity e ORDER BY e.startDate DESC")
    List<ExperienceJpaEntity> findAllOrderByStartDateDesc();
    
    @Query("SELECT e FROM ExperienceJpaEntity e WHERE e.isCurrentPosition = true ORDER BY e.startDate DESC")
    List<ExperienceJpaEntity> findCurrentExperiencesOrderedByStartDate();
    
    @Query("SELECT e FROM ExperienceJpaEntity e WHERE " +
           "(e.endDate IS NULL AND DATEDIFF(CURRENT_DATE, e.startDate) >= 365) OR " +
           "(e.endDate IS NOT NULL AND DATEDIFF(e.endDate, e.startDate) >= 365)")
    List<ExperienceJpaEntity> findLongTermExperiences();
    
    @Query("SELECT e FROM ExperienceJpaEntity e WHERE e.type = :type ORDER BY e.startDate DESC")
    List<ExperienceJpaEntity> findByTypeOrderByStartDate(@Param("type") ExperienceType type);
}