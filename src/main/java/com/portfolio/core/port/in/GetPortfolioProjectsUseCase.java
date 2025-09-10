package com.portfolio.core.port.in;

import com.portfolio.core.domain.project.PortfolioProject;

import java.util.List;
import java.util.Optional;

public interface GetPortfolioProjectsUseCase {
    
    List<PortfolioProject> getAllProjects();
    
    List<PortfolioProject> getProjectsPaginated(int page, int size);
    
    Optional<PortfolioProject> getProjectById(Long id);
    
    long getProjectCount();
    
    List<PortfolioProject> getLinkedProjects();
    
    List<PortfolioProject> getUnlinkedProjects();
}