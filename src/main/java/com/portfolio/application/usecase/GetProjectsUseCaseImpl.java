package com.portfolio.application.usecase;

import com.portfolio.core.domain.project.Project;
import com.portfolio.core.domain.project.ProjectStatus;
import com.portfolio.core.domain.project.ProjectType;
import com.portfolio.core.port.in.GetProjectsUseCase;
import com.portfolio.core.port.out.ProjectRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetProjectsUseCaseImpl implements GetProjectsUseCase {
    
    private final ProjectRepositoryPort projectRepository;
    
    @Override
    @Transactional(readOnly = true)
    public List<Project> getAllProjects() {
        log.debug("Retrieving all projects");
        return projectRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Project> getProjectsPaginated(int page, int size) {
        log.debug("Retrieving paginated projects: page={}, size={}", page, size);
        return projectRepository.findAllPaginated(page, size);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Project> getProjectById(Long id) {
        log.debug("Retrieving project with ID: {}", id);
        return projectRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Project> getProjectsByStatus(ProjectStatus status) {
        log.debug("Retrieving projects with status: {}", status);
        return projectRepository.findByStatus(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Project> getProjectsByType(ProjectType type) {
        log.debug("Retrieving projects with type: {}", type);
        return projectRepository.findByType(type);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Project> getProjectsByTechnology(String technology) {
        log.debug("Retrieving projects with technology: {}", technology);
        return projectRepository.findByTechnology(technology);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getProjectCount() {
        log.debug("Getting total project count");
        return projectRepository.count();
    }
}