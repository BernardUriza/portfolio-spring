package com.portfolio.repository;

import com.portfolio.model.StarredProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StarredProjectRepository extends JpaRepository<StarredProject, Long> {
    
    Optional<StarredProject> findByGithubId(Long githubId);
    
    List<StarredProject> findAllByOrderByStarredAtDesc();
    
    List<StarredProject> findAllByOrderByLastUpdatedDesc();
    
    @Query("SELECT sp FROM StarredProject sp WHERE sp.primaryLanguage = :language ORDER BY sp.starredAt DESC")
    List<StarredProject> findByPrimaryLanguageOrderByStarredAtDesc(String language);
    
    @Query("SELECT DISTINCT sp.primaryLanguage FROM StarredProject sp WHERE sp.primaryLanguage IS NOT NULL ORDER BY sp.primaryLanguage")
    List<String> findDistinctPrimaryLanguages();
    
    @Query("SELECT COUNT(sp) FROM StarredProject sp WHERE sp.starredAt >= :since")
    long countByStarredAtAfter(LocalDateTime since);
    
    boolean existsByGithubId(Long githubId);
}