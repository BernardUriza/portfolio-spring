package com.portfolio.adapter.in.rest;

import com.portfolio.adapter.in.rest.dto.ProjectRestDto;
import com.portfolio.adapter.in.rest.dto.MessageResponse;
import com.portfolio.adapter.in.rest.mapper.ProjectRestMapper;
import com.portfolio.core.domain.project.Project;
import com.portfolio.core.port.in.CreateProjectUseCase;
import com.portfolio.core.port.in.GenerateProjectContentUseCase;
import com.portfolio.core.port.in.GetProjectsUseCase;
import com.portfolio.core.port.in.UpdateProjectUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/projects")
@RequiredArgsConstructor
@Tag(name = "Project API V2", description = "Clean architecture project management")
@Slf4j
public class ProjectRestController {

    private final CreateProjectUseCase createProjectUseCase;
    private final GetProjectsUseCase getProjectsUseCase;
    private final UpdateProjectUseCase updateProjectUseCase;
    private final GenerateProjectContentUseCase generateProjectContentUseCase;
    private final ProjectRestMapper restMapper;

    @GetMapping
    @Operation(summary = "Get all projects with pagination")
    public ResponseEntity<PageImpl<ProjectRestDto>> getAllProjects(Pageable pageable) {
        log.info("Getting projects with pagination: page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        List<Project> projects = getProjectsUseCase.getProjectsPaginated(
                pageable.getPageNumber(), 
                pageable.getPageSize()
        );
        
        List<ProjectRestDto> projectDtos = projects.stream()
                .map(restMapper::toRestDto)
                .collect(Collectors.toList());
        
        long totalElements = getProjectsUseCase.getProjectCount();
        PageImpl<ProjectRestDto> page = new PageImpl<>(projectDtos, pageable, totalElements);
        
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<ProjectRestDto> getProjectById(@PathVariable Long id) {
        log.info("Getting project by ID: {}", id);
        
        Project project = getProjectsUseCase.getProjectById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));
        
        ProjectRestDto dto = restMapper.toRestDto(project);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @Operation(summary = "Create new project")
    public ResponseEntity<ProjectRestDto> createProject(@Valid @RequestBody ProjectRestDto dto) {
        log.info("Creating new project: {}", dto.getTitle());
        
        CreateProjectUseCase.CreateProjectCommand command = restMapper.toCreateCommand(dto);
        Project createdProject = createProjectUseCase.createProject(command);
        
        ProjectRestDto responseDto = restMapper.toRestDto(createdProject);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update existing project")
    public ResponseEntity<ProjectRestDto> updateProject(
            @PathVariable Long id, 
            @Valid @RequestBody ProjectRestDto dto) {
        log.info("Updating project with ID: {}", id);
        
        UpdateProjectUseCase.UpdateProjectCommand command = restMapper.toUpdateCommand(id, dto);
        Project updatedProject = updateProjectUseCase.updateProject(command);
        
        ProjectRestDto responseDto = restMapper.toRestDto(updatedProject);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project by ID")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        log.info("Deleting project with ID: {}", id);
        
        updateProjectUseCase.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/ai-summary")
    @Operation(summary = "Generate AI summary for project")
    public ResponseEntity<MessageResponse> getProjectSummary(@PathVariable Long id) {
        log.info("Generating AI summary for project ID: {}", id);
        
        String summary = generateProjectContentUseCase.generateProjectSummary(id);
        return ResponseEntity.ok(new MessageResponse(summary));
    }

    @GetMapping("/{id}/ai-message")
    @Operation(summary = "Generate dynamic AI message for project")
    public ResponseEntity<MessageResponse> getDynamicMessage(@PathVariable Long id) {
        log.info("Generating dynamic message for project ID: {}", id);
        
        String message = generateProjectContentUseCase.generateDynamicMessage(id);
        return ResponseEntity.ok(new MessageResponse(message));
    }
}