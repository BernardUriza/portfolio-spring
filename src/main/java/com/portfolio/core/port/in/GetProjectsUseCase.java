package com.portfolio.core.port.in;

import com.portfolio.core.domain.project.Project;

import java.util.List;
import java.util.Optional;

public interface GetProjectsUseCase {
    
    List<Project> getAllProjects();
    
    List<Project> getProjectsPaginated(int page, int size);
    
    Optional<Project> getProjectById(Long id);
    
    List<Project> getProjectsByStatus(com.portfolio.core.domain.project.ProjectStatus status);
    
    List<Project> getProjectsByType(com.portfolio.core.domain.project.ProjectType type);
    
    List<Project> getProjectsByTechnology(String technology);
    
    long getProjectCount();
}