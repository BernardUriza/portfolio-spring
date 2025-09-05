package com.portfolio.repository;

import com.portfolio.model.Project;
import com.portfolio.model.StarredProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    List<Project> findByStatusOrderByCreatedAtDesc(Project.ProjectStatus status);
    
    List<Project> findByTypeOrderByCreatedAtDesc(Project.ProjectType type);
    
    @Query("SELECT p FROM Project p WHERE p.status = 'ACTIVE' ORDER BY p.createdAt DESC")
    List<Project> findAllActiveOrderedByDate();
    
    Optional<Project> findByTitleIgnoreCase(String title);
    
    Optional<Project> findBySourceStarredProject(StarredProject starredProject);
    
    boolean existsByTitleIgnoreCase(String title);
    
    boolean existsBySourceStarredProject(StarredProject starredProject);
}
