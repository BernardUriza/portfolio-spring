package com.portfolio.controller;

import com.portfolio.dto.ExperienceDTO;
import com.portfolio.service.ExperienceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/experience")
@RequiredArgsConstructor
@Tag(name = "Experience API", description = "Gestiona la experiencia profesional del portafolio")
public class ExperienceController {

    private final ExperienceService experienceService;

    @GetMapping
    @Operation(summary = "Listar todas las experiencias")
    public ResponseEntity<List<ExperienceDTO>> getAllExperiences() {
        return ResponseEntity.ok(experienceService.getAllExperiences());
    }

    @PostMapping
    @Operation(summary = "Crear una nueva experiencia")
    public ResponseEntity<ExperienceDTO> createExperience(@Valid @RequestBody ExperienceDTO dto) {
        return ResponseEntity.ok(experienceService.createExperience(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una experiencia por ID")
    public ResponseEntity<ExperienceDTO> updateExperience(@PathVariable Long id, @Valid @RequestBody ExperienceDTO dto) {
        return ResponseEntity.ok(experienceService.updateExperience(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una experiencia por ID")
    public ResponseEntity<Void> deleteExperience(@PathVariable Long id) {
        experienceService.deleteExperience(id);
        return ResponseEntity.noContent().build();
    }
}
