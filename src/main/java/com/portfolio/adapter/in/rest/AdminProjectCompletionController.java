package com.portfolio.adapter.in.rest;

import com.portfolio.adapter.in.rest.dto.ProjectCompletionDto;
import com.portfolio.adapter.in.rest.mapper.ProjectCompletionRestMapper;
import com.portfolio.core.domain.project.Project;
import com.portfolio.core.domain.project.ProjectCompletionStatus;
import com.portfolio.core.domain.project.ProjectPriority;
import com.portfolio.core.port.in.ProjectCompletionUseCase;
import com.portfolio.core.port.in.UpdateProjectUseCase;
import com.portfolio.service.StarredProjectService;
import com.portfolio.dto.StarredProjectDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/projects")
@RequiredArgsConstructor
@Slf4j
public class AdminProjectCompletionController {
    
    private final ProjectCompletionUseCase projectCompletionUseCase;
    private final UpdateProjectUseCase updateProjectUseCase;
    private final ProjectCompletionRestMapper mapper;
    private final StarredProjectService starredProjectService;
    
    @GetMapping("/completion")
    public ResponseEntity<Page<ProjectCompletionDto>> getProjectCompletion(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ProjectCompletionStatus status,
            @RequestParam(required = false) Boolean hasDescription,
            @RequestParam(required = false) Boolean hasLiveDemo,
            @RequestParam(required = false) Boolean protectedOnly,
            @RequestParam(required = false) Boolean unlinkedOnly) {
        
        log.info("Getting project completion - page: {}, size: {}, status: {}", page, size, status);
        
        Pageable pageable = PageRequest.of(page, size);
        ProjectCompletionUseCase.ProjectCompletionQuery query = 
                new ProjectCompletionUseCase.ProjectCompletionQuery(
                        search, status, hasDescription, hasLiveDemo, protectedOnly, unlinkedOnly);
        
        Page<Project> projects = projectCompletionUseCase.getProjectCompletion(query, pageable);
        Page<ProjectCompletionDto> projectDtos = projects.map(mapper::toDto);
        
        return ResponseEntity.ok(projectDtos);
    }
    
    @PutMapping("/{id}/link-repo")
    public ResponseEntity<ProjectCompletionDto> linkProjectToRepository(
            @PathVariable Long id,
            @RequestBody LinkRepositoryRequest request) {
        
        log.info("Linking project {} to repository {}", id, request.repositoryFullName());
        
        ProjectCompletionUseCase.LinkRepositoryCommand command = 
                new ProjectCompletionUseCase.LinkRepositoryCommand(
                        id, request.repositoryId(), request.repositoryFullName());
        
        Project linkedProject = projectCompletionUseCase.linkProjectToRepository(command);
        ProjectCompletionDto dto = mapper.toDto(linkedProject);
        
        return ResponseEntity.ok(dto);
    }
    
    @DeleteMapping("/{id}/link-repo")
    public ResponseEntity<ProjectCompletionDto> unlinkProjectFromRepository(@PathVariable Long id) {
        log.info("Unlinking project {} from repository", id);
        
        Project unlinkedProject = projectCompletionUseCase.unlinkProjectFromRepository(id);
        ProjectCompletionDto dto = mapper.toDto(unlinkedProject);
        
        return ResponseEntity.ok(dto);
    }
    
    @PatchMapping("/{id}/protect")
    public ResponseEntity<ProjectCompletionDto> protectField(
            @PathVariable Long id,
            @RequestBody ProtectFieldRequest request) {
        
        log.info("Setting field protection for project {} field {} to {}", 
                 id, request.field(), request.protect());
        
        Project project = projectCompletionUseCase.protectField(id, request.field(), request.protect());
        ProjectCompletionDto dto = mapper.toDto(project);
        
        return ResponseEntity.ok(dto);
    }
    
    @PatchMapping("/{id}/completion-status")
    public ResponseEntity<ProjectCompletionDto> changeCompletionStatus(
            @PathVariable Long id,
            @RequestBody ChangeCompletionStatusRequest request) {
        
        log.info("Changing completion status for project {} to {}", id, request.status());
        
        Project project = projectCompletionUseCase.changeCompletionStatus(id, request.status());
        ProjectCompletionDto dto = mapper.toDto(project);
        
        return ResponseEntity.ok(dto);
    }
    
    @PatchMapping("/{id}/priority")
    public ResponseEntity<ProjectCompletionDto> changePriority(
            @PathVariable Long id,
            @RequestBody ChangePriorityRequest request) {
        
        log.info("Changing priority for project {} to {}", id, request.priority());
        
        Project project = projectCompletionUseCase.changePriority(id, request.priority());
        ProjectCompletionDto dto = mapper.toDto(project);
        
        return ResponseEntity.ok(dto);
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<ProjectCompletionDto> updateProject(
            @PathVariable Long id,
            @RequestBody UpdateProjectRequest request,
            @RequestParam(defaultValue = "false") boolean force) {
        
        log.info("Updating project {} - force: {}", id, force);
        
        Project project;
        if (force) {
            // Direct update bypassing protections
            UpdateProjectUseCase.UpdateProjectCommand command = 
                    new UpdateProjectUseCase.UpdateProjectCommand(
                            id, request.title(), request.description(), 
                            request.link(), request.githubRepo());
            project = updateProjectUseCase.updateProject(command);
        } else {
            // Use manual update methods that respect protections
            project = updateProjectUseCase.updateProject(
                    new UpdateProjectUseCase.UpdateProjectCommand(
                            id, request.title(), request.description(), 
                            request.link(), request.githubRepo()));
        }
        
        ProjectCompletionDto dto = mapper.toDto(project);
        return ResponseEntity.ok(dto);
    }
    
    @PostMapping("/{id}/resync")
    public ResponseEntity<Void> resyncProject(@PathVariable Long id) {
        log.info("Resyncing project {}", id);
        
        projectCompletionUseCase.bulkResync(List.of(id));
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/bulk")
    public ResponseEntity<Void> bulkAction(@RequestBody BulkActionRequest request) {
        log.info("Executing bulk action {} for {} projects", request.action(), request.ids().size());
        
        switch (request.action()) {
            case "resync" -> projectCompletionUseCase.bulkResync(request.ids());
            case "protect" -> {
                String field = (String) request.payload().get("field");
                Boolean protect = (Boolean) request.payload().get("protect");
                projectCompletionUseCase.bulkProtectField(request.ids(), field, protect);
            }
            case "unprotect" -> {
                String field = (String) request.payload().get("field");
                projectCompletionUseCase.bulkProtectField(request.ids(), field, false);
            }
            case "status" -> {
                String statusStr = (String) request.payload().get("status");
                ProjectCompletionStatus status = ProjectCompletionStatus.valueOf(statusStr);
                projectCompletionUseCase.bulkChangeCompletionStatus(request.ids(), status);
            }
            default -> throw new IllegalArgumentException("Unknown bulk action: " + request.action());
        }
        
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/starred-repos")
    public ResponseEntity<List<StarredProjectDto>> getStarredRepositories() {
        log.info("Getting starred repositories for linking");
        
        List<StarredProjectDto> starredProjects = starredProjectService.getAllStarredProjects();
        
        return ResponseEntity.ok(starredProjects);
    }
    
    // Request DTOs
    public record LinkRepositoryRequest(
            Long repositoryId,
            String repositoryFullName
    ) {}
    
    public record ProtectFieldRequest(
            String field,
            Boolean protect
    ) {}
    
    public record ChangeCompletionStatusRequest(
            ProjectCompletionStatus status
    ) {}
    
    public record ChangePriorityRequest(
            ProjectPriority priority
    ) {}
    
    public record UpdateProjectRequest(
            String title,
            String description,
            String link,
            String githubRepo
    ) {}
    
    public record BulkActionRequest(
            String action,
            List<Long> ids,
            Map<String, Object> payload
    ) {}
}