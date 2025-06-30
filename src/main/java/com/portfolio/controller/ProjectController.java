package com.portfolio.controller;

import com.portfolio.dto.ProjectDTO;
import com.portfolio.dto.MessageResponse;
import com.portfolio.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project API", description = "Gestiona los proyectos del portafolio")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "Listar todos los proyectos")
    public ResponseEntity<Page<ProjectDTO>> getAllProjects(Pageable pageable) {
        return ResponseEntity.ok(projectService.getAllProjects(pageable));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo proyecto")
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody ProjectDTO dto) {
        return ResponseEntity.ok(projectService.createProject(dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un proyecto por ID")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un proyecto existente por ID")
    public ResponseEntity<ProjectDTO> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectDTO dto) {
        return ResponseEntity.ok(projectService.updateProject(id, dto));
    }

    @GetMapping("/{id}/ai-message")
    @Operation(summary = "Generar mensaje dinámico para un proyecto")
    public ResponseEntity<MessageResponse> getDynamicMessage(@PathVariable Long id) {
        String msg = projectService.generateSummaryMessage(id);
        return ResponseEntity.ok(new MessageResponse(msg));
    }
}
