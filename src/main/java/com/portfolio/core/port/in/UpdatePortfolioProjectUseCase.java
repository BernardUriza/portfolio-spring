package com.portfolio.core.port.in;

import com.portfolio.core.domain.project.PortfolioProject;
import com.portfolio.core.domain.project.LinkType;

import java.util.Set;

public interface UpdatePortfolioProjectUseCase {
    
    PortfolioProject updateProject(UpdatePortfolioProjectCommand command);
    
    void deleteProject(Long id);
    
    PortfolioProject updateDescriptionManually(Long id, String description);
    
    PortfolioProject updateLinkManually(Long id, String link);
    
    PortfolioProject updateSkillsManually(Long id, Set<Long> skillIds);
    
    PortfolioProject updateExperiencesManually(Long id, Set<Long> experienceIds);
    
    PortfolioProject linkToSourceRepository(Long projectId, Long sourceRepositoryId, LinkType linkType);
    
    PortfolioProject unlinkFromSourceRepository(Long projectId);
    
    record UpdatePortfolioProjectCommand(
        Long id,
        String title,
        String description,
        String link,
        String githubRepo,
        Set<Long> skillIds,
        Set<Long> experienceIds
    ) {}
}