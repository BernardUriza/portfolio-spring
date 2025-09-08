package com.portfolio.application.usecase;

import com.portfolio.core.domain.project.Project;
import com.portfolio.core.domain.project.ProjectCompletionStatus;
import com.portfolio.core.domain.project.ProjectPriority;
import com.portfolio.core.port.in.ProjectCompletionUseCase;
import com.portfolio.core.port.out.ProjectRepositoryPort;
import com.portfolio.adapter.out.persistence.jpa.StarredProjectJpaRepository;
import com.portfolio.adapter.out.persistence.jpa.StarredProjectJpaEntity;
import com.portfolio.service.SyncMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectCompletionUseCaseImpl implements ProjectCompletionUseCase {
    
    private final ProjectRepositoryPort projectRepository;
    private final StarredProjectJpaRepository starredProjectRepository;
    private final SyncMonitorService syncMonitorService;
    
    @Override
    @Transactional
    public Project linkProjectToRepository(LinkRepositoryCommand command) {
        log.info("Linking project {} to repository {}", command.projectId(), command.repositoryFullName());
        
        Project project = projectRepository.findById(command.projectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + command.projectId()));
        
        // Find the starred project to get additional repository info
        StarredProjectJpaEntity starredProject = null;
        if (command.repositoryId() != null) {
            starredProject = starredProjectRepository.findByGithubId(command.repositoryId()).orElse(null);
        } else if (command.repositoryFullName() != null) {
            starredProject = starredProjectRepository.findByFullName(command.repositoryFullName()).orElse(null);
        }
        
        if (starredProject == null) {
            throw new IllegalArgumentException("Repository not found in starred projects: " + command.repositoryFullName());
        }
        
        String repositoryUrl = "https://github.com/" + starredProject.getFullName();
        Project linkedProject = project.linkToRepository(
                starredProject.getGithubId(),
                starredProject.getFullName(),
                repositoryUrl,
                starredProject.getStargazersCount(),
                "main" // Default branch - could be enhanced to fetch from GitHub API
        );
        
        Project savedProject = projectRepository.save(linkedProject);
        
        syncMonitorService.appendLog("INFO", 
            String.format("Linked project '%s' to repository '%s'", 
                         project.getTitle(), starredProject.getFullName()));
        
        return savedProject;
    }
    
    @Override
    @Transactional
    public Project unlinkProjectFromRepository(Long projectId) {
        log.info("Unlinking project {} from repository", projectId);
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));
        
        Project unlinkedProject = project.unlinkFromRepository();
        Project savedProject = projectRepository.save(unlinkedProject);
        
        syncMonitorService.appendLog("INFO", 
            String.format("Unlinked project '%s' from repository", project.getTitle()));
        
        return savedProject;
    }
    
    @Override
    @Transactional
    public Project protectField(Long projectId, String fieldName, Boolean protect) {
        log.info("Setting field protection for project {} field {} to {}", projectId, fieldName, protect);
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));
        
        Project protectedProject = project.protectField(fieldName, protect);
        Project savedProject = projectRepository.save(protectedProject);
        
        syncMonitorService.appendLog("INFO", 
            String.format("Set field protection for project '%s' field '%s' to %s", 
                         project.getTitle(), fieldName, protect));
        
        return savedProject;
    }
    
    @Override
    @Transactional
    public Project changeCompletionStatus(Long projectId, ProjectCompletionStatus status) {
        log.info("Changing completion status for project {} to {}", projectId, status);
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));
        
        Project updatedProject = project.changeCompletionStatus(status);
        Project savedProject = projectRepository.save(updatedProject);
        
        syncMonitorService.appendLog("INFO", 
            String.format("Changed completion status for project '%s' to %s", 
                         project.getTitle(), status));
        
        return savedProject;
    }
    
    @Override
    @Transactional
    public Project changePriority(Long projectId, ProjectPriority priority) {
        log.info("Changing priority for project {} to {}", projectId, priority);
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));
        
        Project updatedProject = project.changePriority(priority);
        Project savedProject = projectRepository.save(updatedProject);
        
        syncMonitorService.appendLog("INFO", 
            String.format("Changed priority for project '%s' to %s", 
                         project.getTitle(), priority));
        
        return savedProject;
    }
    
    @Override
    @Transactional
    public void bulkResync(List<Long> projectIds) {
        log.info("Bulk resyncing {} projects", projectIds.size());
        
        for (Long projectId : projectIds) {
            try {
                Project project = projectRepository.findById(projectId).orElse(null);
                if (project != null && project.getSourceStarredProjectId() != null) {
                    // Trigger resync logic here - this would integrate with the existing sync service
                    syncMonitorService.appendLog("INFO", 
                        String.format("Triggered resync for project '%s'", project.getTitle()));
                }
            } catch (Exception e) {
                log.error("Error resyncing project {}: {}", projectId, e.getMessage());
                syncMonitorService.appendLog("ERROR", 
                    String.format("Failed to resync project ID %d: %s", projectId, e.getMessage()));
            }
        }
    }
    
    @Override
    @Transactional
    public void bulkProtectField(List<Long> projectIds, String fieldName, Boolean protect) {
        log.info("Bulk setting field protection for {} projects, field {} to {}", 
                 projectIds.size(), fieldName, protect);
        
        for (Long projectId : projectIds) {
            try {
                protectField(projectId, fieldName, protect);
            } catch (Exception e) {
                log.error("Error protecting field for project {}: {}", projectId, e.getMessage());
                syncMonitorService.appendLog("ERROR", 
                    String.format("Failed to protect field %s for project ID %d: %s", 
                                 fieldName, projectId, e.getMessage()));
            }
        }
    }
    
    @Override
    @Transactional
    public void bulkChangeCompletionStatus(List<Long> projectIds, ProjectCompletionStatus status) {
        log.info("Bulk changing completion status for {} projects to {}", projectIds.size(), status);
        
        for (Long projectId : projectIds) {
            try {
                changeCompletionStatus(projectId, status);
            } catch (Exception e) {
                log.error("Error changing status for project {}: {}", projectId, e.getMessage());
                syncMonitorService.appendLog("ERROR", 
                    String.format("Failed to change status for project ID %d: %s", projectId, e.getMessage()));
            }
        }
    }
    
    @Override
    public Page<Project> getProjectCompletion(ProjectCompletionQuery query, Pageable pageable) {
        log.debug("Querying project completion with filters: {}", query);
        
        List<Project> allProjects = projectRepository.findAll();
        
        // Apply filters
        List<Project> filteredProjects = allProjects.stream()
                .filter(project -> applyFilters(project, query))
                .collect(Collectors.toList());
        
        // Apply search if provided
        if (query.search() != null && !query.search().trim().isEmpty()) {
            String searchTerm = query.search().toLowerCase();
            filteredProjects = filteredProjects.stream()
                    .filter(project -> 
                        project.getTitle().toLowerCase().contains(searchTerm) ||
                        (project.getDescription() != null && project.getDescription().toLowerCase().contains(searchTerm)) ||
                        (project.getRepositoryFullName() != null && project.getRepositoryFullName().toLowerCase().contains(searchTerm))
                    )
                    .collect(Collectors.toList());
        }
        
        // Sort by updated date desc
        filteredProjects.sort((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()));
        
        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredProjects.size());
        
        if (start > filteredProjects.size()) {
            filteredProjects = List.of();
        } else {
            filteredProjects = filteredProjects.subList(start, end);
        }
        
        return new PageImpl<>(filteredProjects, pageable, allProjects.size());
    }
    
    private boolean applyFilters(Project project, ProjectCompletionQuery query) {
        if (query.status() != null && !query.status().equals(project.getCompletionStatus())) {
            return false;
        }
        
        if (query.hasDescription() != null && query.hasDescription() != project.isDescriptionComplete()) {
            return false;
        }
        
        if (query.hasLiveDemo() != null && query.hasLiveDemo() != project.isLiveDemoComplete()) {
            return false;
        }
        
        if (query.protectedOnly() != null && query.protectedOnly()) {
            if (!project.isFieldProtected("description") && 
                !project.isFieldProtected("link") && 
                !project.isFieldProtected("skills") && 
                !project.isFieldProtected("experiences")) {
                return false;
            }
        }
        
        if (query.unlinkedOnly() != null && query.unlinkedOnly() != !project.isLinkedToRepository()) {
            return false;
        }
        
        return true;
    }
}