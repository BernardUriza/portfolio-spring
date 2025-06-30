package com.portfolio.controller;

import com.portfolio.dto.SkillDTO;
import com.portfolio.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
@Tag(name = "Skill API", description = "Gestiona las habilidades del portafolio")
public class SkillController {

    private final SkillService skillService;

    @GetMapping
    @Operation(summary = "Listar todas las habilidades")
    public ResponseEntity<Page<SkillDTO>> getAllSkills(Pageable pageable) {
        return ResponseEntity.ok(skillService.getAllSkills(pageable));
    }

    @PostMapping
    @Operation(summary = "Crear una nueva habilidad")
    public ResponseEntity<SkillDTO> createSkill(@Valid @RequestBody SkillDTO dto) {
        return ResponseEntity.ok(skillService.createSkill(dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una habilidad por ID")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una habilidad por ID")
    public ResponseEntity<SkillDTO> updateSkill(@PathVariable Long id, @Valid @RequestBody SkillDTO dto) {
        return ResponseEntity.ok(skillService.updateSkill(id, dto));
    }
}
