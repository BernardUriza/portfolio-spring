package com.portfolio.adapter.in.rest;

import com.portfolio.dto.SourceRepositoryDto;
import com.portfolio.service.SourceRepositoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/source-repos")
@Tag(name = "Source Repository API", description = "Raw GitHub repositories management")
public class SourceRepositoryRestController {
    private static final Logger log = LoggerFactory.getLogger(SourceRepositoryRestController.class);
    private final SourceRepositoryService sourceRepositoryService;

    public SourceRepositoryRestController(SourceRepositoryService sourceRepositoryService) {
        this.sourceRepositoryService = sourceRepositoryService;
    }

    @GetMapping
    @Operation(summary = "Get all source repositories")
    public ResponseEntity<List<SourceRepositoryDto>> getAllSourceRepositories() {
        log.info("Getting all source repositories");
        
        List<SourceRepositoryDto> repositories = sourceRepositoryService.getAllSourceRepositories();
        return ResponseEntity.ok(repositories);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get source repository by ID")
    public ResponseEntity<SourceRepositoryDto> getSourceRepositoryById(@PathVariable Long id) {
        log.info("Getting source repository by ID: {}", id);
        
        return sourceRepositoryService.getSourceRepository(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get repositories by sync status")
    public ResponseEntity<List<SourceRepositoryDto>> getRepositoriesByStatus(@PathVariable String status) {
        log.info("Getting repositories by status: {}", status);
        
        try {
            var syncStatus = com.portfolio.adapter.out.persistence.jpa.SourceRepositoryJpaEntity.SyncStatus.valueOf(status.toUpperCase());
            List<SourceRepositoryDto> repositories = sourceRepositoryService.getSourceRepositoriesByStatus(syncStatus);
            return ResponseEntity.ok(repositories);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/homepage")
    @Operation(summary = "Update repository homepage URL")
    public ResponseEntity<SourceRepositoryDto> updateHomepage(
            @PathVariable Long id, 
            @RequestBody Map<String, String> payload) {
        log.info("Updating homepage for repository ID: {}", id);
        
        String homepage = payload.get("homepage");
        return sourceRepositoryService.updateRepositoryHomepage(id, homepage)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete source repository by ID")
    public ResponseEntity<Void> deleteSourceRepository(@PathVariable Long id) {
        log.info("Deleting source repository with ID: {}", id);
        
        boolean deleted = sourceRepositoryService.deleteSourceRepository(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
