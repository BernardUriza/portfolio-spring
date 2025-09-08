package com.portfolio.core.port.in;

import com.portfolio.core.domain.project.Project;
import com.portfolio.core.domain.project.ProjectCompletionStatus;
import com.portfolio.core.domain.project.ProjectPriority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjectCompletionUseCase {
    
    // Repository linking
    Project linkProjectToRepository(LinkRepositoryCommand command);
    Project unlinkProjectFromRepository(Long projectId);
    
    // Field protection management
    Project protectField(Long projectId, String fieldName, Boolean protect);
    
    // Completion status management
    Project changeCompletionStatus(Long projectId, ProjectCompletionStatus status);
    Project changePriority(Long projectId, ProjectPriority priority);
    
    // Bulk operations
    void bulkResync(List<Long> projectIds);
    void bulkProtectField(List<Long> projectIds, String fieldName, Boolean protect);
    void bulkChangeCompletionStatus(List<Long> projectIds, ProjectCompletionStatus status);
    
    // Querying with filters
    Page<Project> getProjectCompletion(ProjectCompletionQuery query, Pageable pageable);
    
    record LinkRepositoryCommand(
            Long projectId,
            Long repositoryId,
            String repositoryFullName
    ) {}
    
    record ProjectCompletionQuery(
            String search,
            ProjectCompletionStatus status,
            Boolean hasDescription,
            Boolean hasLiveDemo,
            Boolean protectedOnly,
            Boolean unlinkedOnly
    ) {}
}