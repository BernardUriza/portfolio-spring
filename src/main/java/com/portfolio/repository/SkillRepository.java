package com.portfolio.repository;

import com.portfolio.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    
    Optional<Skill> findByNameIgnoreCase(String name);
    
    List<Skill> findByIsActiveTrue();
    
    List<Skill> findByCategoryOrderByNameAsc(Skill.SkillCategory category);
    
    @Query("SELECT s FROM Skill s WHERE s.isActive = true ORDER BY s.category, s.name")
    List<Skill> findAllActiveOrderedByCategoryAndName();
    
    boolean existsByNameIgnoreCase(String name);
}
