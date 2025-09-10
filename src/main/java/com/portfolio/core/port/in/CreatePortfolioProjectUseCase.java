package com.portfolio.core.port.in;

import com.portfolio.core.domain.project.PortfolioProject;
import com.portfolio.core.domain.project.LinkType;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface CreatePortfolioProjectUseCase {
    
    PortfolioProject createProject(CreatePortfolioProjectCommand command);
    
    record CreatePortfolioProjectCommand(
        String title,
        String description,
        String link,
        String githubRepo,
        LocalDate createdDate,
        List<String> mainTechnologies,
        Set<Long> skillIds,
        Set<Long> experienceIds,
        Long sourceRepositoryId,
        LinkType linkType
    ) {}
}