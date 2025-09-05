package com.portfolio.core.port.in;

import com.portfolio.core.domain.project.Project;

import java.time.LocalDate;
import java.util.List;

public interface CreateProjectUseCase {
    
    Project createProject(CreateProjectCommand command);
    
    record CreateProjectCommand(
            String title,
            String description,
            String link,
            String githubRepo,
            LocalDate createdDate,
            List<String> mainTechnologies
    ) {}
}