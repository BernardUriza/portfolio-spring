package com.portfolio.controller;

import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaRepository;
import com.portfolio.service.BootstrapSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public portfolio API for frontend consumption.
 * Includes bootstrap sync hook for empty portfolio scenarios.
 */
@RestController
@RequestMapping("/api/sync")
@Tag(name = "Public Portfolio API", description = "Public endpoints for portfolio data")
@CrossOrigin(origins = {"http://localhost:4200", "https://portfolio.bernarduriza.dev"})
public class PublicPortfolioController {
    private static final Logger log = LoggerFactory.getLogger(PublicPortfolioController.class);
    private final PortfolioProjectJpaRepository portfolioProjectRepository;
    private final BootstrapSyncService bootstrapSyncService;

    public PublicPortfolioController(PortfolioProjectJpaRepository portfolioProjectRepository,
                                     BootstrapSyncService bootstrapSyncService) {
        this.portfolioProjectRepository = portfolioProjectRepository;
        this.bootstrapSyncService = bootstrapSyncService;
    }

    @GetMapping("/projects")
    @Operation(
        summary = "Get all portfolio projects for public display",
        description = "Returns all active portfolio projects. Triggers bootstrap sync if portfolio is empty (non-blocking)."
    )
    public ResponseEntity<List<PortfolioProjectJpaEntity>> getProjects() {
        try {
            // Bootstrap sync hook - fire and forget, non-blocking
            BootstrapSyncService.BootstrapSyncResult bootstrapResult = bootstrapSyncService.maybeTrigger();
            
            // Add header to indicate if bootstrap sync was triggered
            ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();
            responseBuilder.header("X-Sync-Triggered", String.valueOf(bootstrapResult.triggered()));
            
            if (bootstrapResult.triggered()) {
                log.info("Bootstrap sync triggered for empty portfolio");
            }
            
            // Get current portfolio projects (even if empty)
            List<PortfolioProjectJpaEntity> projects = portfolioProjectRepository.findByStatus(
                PortfolioProjectJpaEntity.ProjectStatusJpa.ACTIVE
            );
            
            return responseBuilder.body(projects);
            
        } catch (Exception e) {
            log.error("Error getting portfolio projects", e);
            return ResponseEntity.internalServerError().body(List.of());
        }
    }
    
    @GetMapping("/projects/{id}")
    @Operation(
        summary = "Get specific portfolio project by ID",
        description = "Returns a specific portfolio project for detailed view"
    )
    public ResponseEntity<PortfolioProjectJpaEntity> getProject(@PathVariable Long id) {
        try {
            return portfolioProjectRepository.findById(id)
                    .filter(project -> project.getStatus() == PortfolioProjectJpaEntity.ProjectStatusJpa.ACTIVE)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting portfolio project {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
