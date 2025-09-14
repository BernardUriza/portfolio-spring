package com.portfolio.controller;

import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaRepository;
import com.portfolio.adapter.out.persistence.jpa.SourceRepositoryJpaRepository;
import com.portfolio.core.domain.project.LinkType;
import com.portfolio.service.PortfolioCompletionService;
import com.portfolio.service.PortfolioService;
import com.portfolio.service.SyncSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Minimal admin controller exposing portfolio projects for the Admin UI.
 */
@RestController
@RequestMapping("/api/admin/portfolio")
@RequiredArgsConstructor
@Slf4j
public class PortfolioAdminController {

    private final PortfolioProjectJpaRepository portfolioRepository;
    private final PortfolioCompletionService completionService;
    private final PortfolioService portfolioService;
    private final SyncSchedulerService syncSchedulerService;
    private final SourceRepositoryJpaRepository sourceRepositoryRepository;

    /**
     * Return paginated portfolio projects with completion metrics for Admin table.
     */
    @GetMapping("/completion")
    public ResponseEntity<Map<String, Object>> getPortfolioCompletion(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<PortfolioProjectJpaEntity> result = portfolioRepository.findAll(pageable);

        List<Object> projects = result.getContent().stream()
                .map(completionService::calculateCompletion)
                .map(dto -> {
                    // Enrich with repository info when linked to a source repo
                    String repoFullName = null;
                    String repoUrl = null;
                    Integer repoStars = null;
                    if (dto.getSourceRepositoryId() != null) {
                        var srcOpt = sourceRepositoryRepository.findById(dto.getSourceRepositoryId());
                        if (srcOpt.isPresent()) {
                            var src = srcOpt.get();
                            repoFullName = src.getFullName();
                            repoUrl = src.getGithubRepoUrl();
                            repoStars = src.getStargazersCount();
                        }
                    }
                    return Map.of(
                        "id", dto.getId(),
                        "title", dto.getTitle(),
                        "description", dto.getDescription(),
                        "link", dto.getLink(),
                        "githubRepo", dto.getGithubRepo(),
                        "status", dto.getStatus(),
                        "type", dto.getType(),
                        "completionStatus", dto.getCompletionStatus(),
                        "priority", dto.getPriority(),
                        "mainTechnologies", dto.getMainTechnologies(),
                        "sourceRepositoryId", dto.getSourceRepositoryId(),
                        "linkType", dto.getLinkType(),
                        // Enriched repository fields for UI convenience
                        "repositoryFullName", repoFullName,
                        "repositoryUrl", repoUrl,
                        "repositoryStars", repoStars,
                        "protectDescription", dto.getProtectDescription(),
                        "protectLiveDemoUrl", dto.getProtectLiveDemoUrl(),
                        "protectSkills", dto.getProtectSkills(),
                        "protectExperiences", dto.getProtectExperiences(),
                        "overallCompleteness", dto.getOverallCompleteness()
                );
                })
                .toList();

        Map<String, Object> body = Map.of(
                "projects", projects,
                "pagination", Map.of(
                        "page", result.getNumber(),
                        "size", result.getSize(),
                        "totalElements", result.getTotalElements(),
                        "totalPages", result.getTotalPages()
                )
        );

        return ResponseEntity.ok(body);
    }

    /** Link portfolio project to source repository (manual) */
    @PutMapping("/{id}/link-repo")
    public ResponseEntity<Map<String, Object>> linkToSource(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        try {
            Long sourceRepositoryId = Long.valueOf(payload.get("repositoryId").toString());
            var result = portfolioService.linkToSourceRepository(id, sourceRepositoryId, LinkType.MANUAL);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "projectId", result.getId(),
                    "sourceRepositoryId", result.getSourceRepositoryId()
            ));
        } catch (Exception e) {
            log.error("Failed to link portfolio {} to source: {}", id, e.getMessage(), e);
            return ResponseEntity.status(400).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /** Unlink portfolio project from source repository */
    @DeleteMapping("/{id}/link-repo")
    public ResponseEntity<Map<String, Object>> unlinkSource(@PathVariable Long id) {
        try {
            var result = portfolioService.unlinkFromSourceRepository(id);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "projectId", result.getId(),
                    "sourceRepositoryId", result.getSourceRepositoryId()
            ));
        } catch (Exception e) {
            log.error("Failed to unlink portfolio {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(400).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /** Update protection flags */
    @PatchMapping("/{id}/protection")
    public ResponseEntity<Map<String, Object>> updateProtection(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        return portfolioRepository.findById(id)
                .map(existing -> {
                    var builder = existing.toBuilder();
                    if (payload.containsKey("protectDescription")) {
                        builder.protectDescription(Boolean.parseBoolean(payload.get("protectDescription").toString()));
                    }
                    if (payload.containsKey("protectLiveDemoUrl")) {
                        builder.protectLiveDemoUrl(Boolean.parseBoolean(payload.get("protectLiveDemoUrl").toString()));
                    }
                    if (payload.containsKey("protectSkills")) {
                        builder.protectSkills(Boolean.parseBoolean(payload.get("protectSkills").toString()));
                    }
                    if (payload.containsKey("protectExperiences")) {
                        builder.protectExperiences(Boolean.parseBoolean(payload.get("protectExperiences").toString()));
                    }
                    var saved = portfolioRepository.save(builder.build());
                    return ResponseEntity.ok(Map.of("status", "success", "id", saved.getId()));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** Change completion status */
    @PatchMapping("/{id}/completion-status")
    public ResponseEntity<Map<String, Object>> changeCompletionStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        if (status == null) return ResponseEntity.badRequest().build();
        return portfolioRepository.findById(id)
                .map(existing -> {
                    PortfolioProjectJpaEntity.ProjectCompletionStatusJpa newStatus =
                            PortfolioProjectJpaEntity.ProjectCompletionStatusJpa.valueOf(status.toUpperCase());
                    var saved = portfolioRepository.save(existing.toBuilder().completionStatus(newStatus).build());
                    return ResponseEntity.ok(Map.of("status", "success", "id", saved.getId()));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** Change priority */
    @PatchMapping("/{id}/priority")
    public ResponseEntity<Map<String, Object>> changePriority(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String priority = payload.get("priority");
        if (priority == null) return ResponseEntity.badRequest().build();
        return portfolioRepository.findById(id)
                .map(existing -> {
                    PortfolioProjectJpaEntity.ProjectPriorityJpa newPriority =
                            PortfolioProjectJpaEntity.ProjectPriorityJpa.valueOf(priority.toUpperCase());
                    var saved = portfolioRepository.save(existing.toBuilder().priority(newPriority).build());
                    return ResponseEntity.ok(Map.of("status", "success", "id", saved.getId()));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** Minimal update endpoint for description/link */
    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateFields(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        return portfolioRepository.findById(id)
                .map(existing -> {
                    var builder = existing.toBuilder();
                    if (payload.containsKey("description")) {
                        builder.description((String) payload.get("description"));
                    }
                    if (payload.containsKey("link")) {
                        builder.link((String) payload.get("link"));
                    }
                    var saved = portfolioRepository.save(builder.build());
                    return ResponseEntity.ok(Map.of("status", "success", "id", saved.getId()));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** Resync a single portfolio project using its linked source repository */
    @PostMapping("/{id}/resync")
    public ResponseEntity<Map<String, Object>> resyncProject(@PathVariable Long id) {
        try {
            syncSchedulerService.resyncPortfolioProject(id);
            return ResponseEntity.accepted().body(Map.of("status", "accepted", "message", "Resync triggered"));
        } catch (Exception e) {
            log.error("Failed to resync portfolio {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
}
