package com.portfolio.adapter.in.rest;

import com.portfolio.adapter.in.rest.dto.ProjectRestDto;
import com.portfolio.adapter.in.rest.dto.MessageResponse;
import com.portfolio.adapter.in.rest.mapper.ProjectRestMapper;
import com.portfolio.dto.ProjectSummaryDto;
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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project API", description = "Clean hexagonal architecture project management")
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

    @PatchMapping("/{id}/description")
    @Operation(summary = "Update project description manually (with sync protection)")
    public ResponseEntity<ProjectRestDto> updateDescriptionManually(
            @PathVariable Long id, 
            @RequestBody Map<String, String> payload) {
        log.info("Manually updating description for project ID: {}", id);
        
        String description = payload.get("description");
        if (description == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Project updatedProject = updateProjectUseCase.updateDescriptionManually(id, description);
        return ResponseEntity.ok(restMapper.toRestDto(updatedProject));
    }

    @PatchMapping("/{id}/link")
    @Operation(summary = "Update project live demo link manually (with sync protection)")
    public ResponseEntity<ProjectRestDto> updateLinkManually(
            @PathVariable Long id, 
            @RequestBody Map<String, String> payload) {
        log.info("Manually updating link for project ID: {}", id);
        
        String link = payload.get("link");
        Project updatedProject = updateProjectUseCase.updateLinkManually(id, link);
        return ResponseEntity.ok(restMapper.toRestDto(updatedProject));
    }

    @PatchMapping("/{id}/skills")
    @Operation(summary = "Update project skills manually (with sync protection)")
    public ResponseEntity<ProjectRestDto> updateSkillsManually(
            @PathVariable Long id, 
            @RequestBody Map<String, Set<Long>> payload) {
        log.info("Manually updating skills for project ID: {}", id);
        
        Set<Long> skillIds = payload.get("skillIds");
        if (skillIds == null) {
            skillIds = new HashSet<>();
        }
        
        Project updatedProject = updateProjectUseCase.updateSkillsManually(id, skillIds);
        return ResponseEntity.ok(restMapper.toRestDto(updatedProject));
    }

    @PatchMapping("/{id}/experiences")
    @Operation(summary = "Update project experiences manually (with sync protection)")
    public ResponseEntity<ProjectRestDto> updateExperiencesManually(
            @PathVariable Long id, 
            @RequestBody Map<String, Set<Long>> payload) {
        log.info("Manually updating experiences for project ID: {}", id);
        
        Set<Long> experienceIds = payload.get("experienceIds");
        if (experienceIds == null) {
            experienceIds = new HashSet<>();
        }
        
        Project updatedProject = updateProjectUseCase.updateExperiencesManually(id, experienceIds);
        return ResponseEntity.ok(restMapper.toRestDto(updatedProject));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get compact project summaries for narration context")
    public ResponseEntity<List<ProjectSummaryDto>> getProjectsSummary() {
        log.info("Getting compact project summaries for narration");
        
        List<Project> projects = getProjectsUseCase.getAllProjects();
        List<ProjectSummaryDto> summaries = projects.stream()
            .map(this::toProjectSummary)
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(summaries);
    }
    
    private ProjectSummaryDto toProjectSummary(Project project) {
        List<String> tech = project.getMainTechnologies() != null 
            ? project.getMainTechnologies().stream()
                .limit(5) // Top 5 technologies
                .collect(Collectors.toList())
            : List.of();
            
        List<String> outcomes = List.of(
            "GitHub Stars: " + (project.getRepositoryStars() != null ? project.getRepositoryStars() : 0),
            "Repository: " + (project.getRepositoryFullName() != null ? project.getRepositoryFullName() : "N/A")
        );
        
        String ownerRepo = project.getTitle(); // Use title instead of name
        if (project.getRepositoryFullName() != null && project.getRepositoryFullName().contains("/")) {
            ownerRepo = project.getRepositoryFullName();
        }
        
        return new ProjectSummaryDto(
            String.valueOf(project.getId()),
            project.getTitle(),
            ownerRepo,
            tech,
            outcomes
        );
    }
}