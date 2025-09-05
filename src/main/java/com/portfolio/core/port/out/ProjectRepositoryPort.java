package com.portfolio.core.port.out;

import com.portfolio.core.domain.project.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectRepositoryPort {
    
    Project save(Project project);
    
    Optional<Project> findById(Long id);
    
    List<Project> findAll();
    
    List<Project> findAllPaginated(int page, int size);
    
    void deleteById(Long id);
    
    boolean existsById(Long id);
    
    long count();
    
    List<Project> findByStatus(com.portfolio.core.domain.project.ProjectStatus status);
    
    List<Project> findByType(com.portfolio.core.domain.project.ProjectType type);
    
    List<Project> findByTechnology(String technology);
}