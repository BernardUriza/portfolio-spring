package com.portfolio.application.usecase;

import com.portfolio.core.domain.project.Project;
import com.portfolio.core.port.in.CreateProjectUseCase;
import com.portfolio.core.port.out.ProjectRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreateProjectUseCaseImpl implements CreateProjectUseCase {
    
    private final ProjectRepositoryPort projectRepository;
    
    @Override
    @Transactional
    public Project createProject(CreateProjectCommand command) {
        log.info("Creating new project with title: {}", command.title());
        
        Project project = Project.create(
                command.title(),
                command.description(),
                command.link(),
                command.githubRepo(),
                command.createdDate(),
                command.mainTechnologies()
        );
        
        Project savedProject = projectRepository.save(project);
        
        log.info("Project created successfully with ID: {}", savedProject.getId());
        return savedProject;
    }
}