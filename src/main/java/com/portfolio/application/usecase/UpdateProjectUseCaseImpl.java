package com.portfolio.application.usecase;

import com.portfolio.core.domain.project.Project;
import com.portfolio.core.domain.project.ProjectStatus;
import com.portfolio.core.port.in.UpdateProjectUseCase;
import com.portfolio.core.port.out.ProjectRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateProjectUseCaseImpl implements UpdateProjectUseCase {
    
    private final ProjectRepositoryPort projectRepository;
    
    @Override
    @Transactional
    public Project updateProject(UpdateProjectCommand command) {
        log.info("Updating project with ID: {}", command.id());
        
        Project existingProject = projectRepository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + command.id()));
        
        Project updatedProject = existingProject.updateBasicInfo(
                command.title(),
                command.description(),
                command.link(),
                command.githubRepo()
        );
        
        Project savedProject = projectRepository.save(updatedProject);
        log.info("Project updated successfully with ID: {}", savedProject.getId());
        
        return savedProject;
    }
    
    @Override
    @Transactional
    public Project changeProjectStatus(Long id, ProjectStatus newStatus) {
        log.info("Changing status of project {} to {}", id, newStatus);
        
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));
        
        if (!existingProject.getStatus().canTransitionTo(newStatus)) {
            throw new IllegalArgumentException(
                    String.format("Invalid status transition from %s to %s", 
                            existingProject.getStatus(), newStatus));
        }
        
        Project updatedProject = existingProject.changeStatus(newStatus);
        Project savedProject = projectRepository.save(updatedProject);
        
        log.info("Project status changed successfully for ID: {}", savedProject.getId());
        return savedProject;
    }
    
    @Override
    @Transactional
    public void deleteProject(Long id) {
        log.info("Deleting project with ID: {}", id);
        
        if (!projectRepository.existsById(id)) {
            throw new IllegalArgumentException("Project not found with ID: " + id);
        }
        
        projectRepository.deleteById(id);
        log.info("Project deleted successfully with ID: {}", id);
    }
}