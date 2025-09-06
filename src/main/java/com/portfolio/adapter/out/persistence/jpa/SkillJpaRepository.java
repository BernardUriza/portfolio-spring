package com.portfolio.adapter.out.persistence.jpa;

import com.portfolio.core.domain.skill.SkillCategory;
import com.portfolio.core.domain.skill.SkillLevel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillJpaRepository extends JpaRepository<SkillJpaEntity, Long> {
    
    List<SkillJpaEntity> findByCategory(SkillCategory category);
    
    List<SkillJpaEntity> findByLevel(SkillLevel level);
    
    List<SkillJpaEntity> findByIsFeaturedTrue();
    
    List<SkillJpaEntity> findByYearsOfExperienceGreaterThanEqual(Integer minYears);
    
    boolean existsByNameIgnoreCase(String name);
    
    @Query("SELECT s FROM SkillJpaEntity s ORDER BY s.createdAt DESC")
    List<SkillJpaEntity> findAllOrderByCreatedAtDesc();
    
    @Query("SELECT s FROM SkillJpaEntity s WHERE s.category = :category ORDER BY s.level DESC, s.yearsOfExperience DESC")
    List<SkillJpaEntity> findByCategoryOrderByLevelAndExperience(@Param("category") SkillCategory category);
    
    @Query("SELECT s FROM SkillJpaEntity s WHERE s.isFeatured = true ORDER BY s.level DESC, s.yearsOfExperience DESC")
    List<SkillJpaEntity> findFeaturedSkillsOrdered();
}