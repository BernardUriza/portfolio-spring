package com.portfolio.core.port.in;

import com.portfolio.core.domain.project.Project;
import com.portfolio.core.domain.project.ProjectStatus;

import java.util.Set;

public interface UpdateProjectUseCase {
    
    Project updateProject(UpdateProjectCommand command);
    
    Project changeProjectStatus(Long id, ProjectStatus newStatus);
    
    void deleteProject(Long id);
    
    // Manual update methods with override protection
    Project updateDescriptionManually(Long id, String description);
    
    Project updateLinkManually(Long id, String link);
    
    Project updateSkillsManually(Long id, Set<Long> skillIds);
    
    Project updateExperiencesManually(Long id, Set<Long> experienceIds);
    
    record UpdateProjectCommand(
            Long id,
            String title,
            String description,
            String link,
            String githubRepo
    ) {}
}