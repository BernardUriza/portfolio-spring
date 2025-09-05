package com.portfolio.core.port.in;

import com.portfolio.core.domain.project.Project;
import com.portfolio.core.domain.project.ProjectStatus;

public interface UpdateProjectUseCase {
    
    Project updateProject(UpdateProjectCommand command);
    
    Project changeProjectStatus(Long id, ProjectStatus newStatus);
    
    void deleteProject(Long id);
    
    record UpdateProjectCommand(
            Long id,
            String title,
            String description,
            String link,
            String githubRepo
    ) {}
}