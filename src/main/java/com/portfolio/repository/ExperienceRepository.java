package com.portfolio.repository;

import com.portfolio.model.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    
    List<Experience> findByIsActiveTrue();
    
    List<Experience> findByTypeOrderByCreatedAtDesc(Experience.ExperienceType type);
    
    @Query("SELECT e FROM Experience e WHERE e.isActive = true ORDER BY e.createdAt DESC")
    List<Experience> findAllActiveOrderedByDate();
    
    Optional<Experience> findByTitleIgnoreCase(String title);
    
    boolean existsByTitleIgnoreCase(String title);
}
