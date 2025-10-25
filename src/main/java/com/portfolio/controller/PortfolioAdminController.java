package com.portfolio.controller;

import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaRepository;
import com.portfolio.adapter.out.persistence.jpa.SourceRepositoryJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.SourceRepositoryJpaRepository;
import com.portfolio.core.domain.project.LinkType;
import com.portfolio.core.domain.project.PortfolioProject;
import com.portfolio.core.port.out.AIServicePort;
import com.portfolio.service.PortfolioCompletionService;
import com.portfolio.service.PortfolioService;
import com.portfolio.service.SyncSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Minimal admin controller exposing portfolio projects for the Admin UI.
 */
@RestController
@RequestMapping("/api/admin/portfolio")
public class PortfolioAdminController {
    private static final Logger log = LoggerFactory.getLogger(PortfolioAdminController.class);
    private final PortfolioProjectJpaRepository portfolioRepository;
    private final PortfolioCompletionService completionService;
    private final PortfolioService portfolioService;
    private final SyncSchedulerService syncSchedulerService;
    private final SourceRepositoryJpaRepository sourceRepositoryRepository;
    private final AIServicePort aiService;

    public PortfolioAdminController(PortfolioProjectJpaRepository portfolioRepository,
                                    PortfolioCompletionService completionService,
                                    PortfolioService portfolioService,
                                    SyncSchedulerService syncSchedulerService,
                                    SourceRepositoryJpaRepository sourceRepositoryRepository,
                                    AIServicePort aiService) {
        this.portfolioRepository = portfolioRepository;
        this.completionService = completionService;
        this.portfolioService = portfolioService;
        this.syncSchedulerService = syncSchedulerService;
        this.sourceRepositoryRepository = sourceRepositoryRepository;
        this.aiService = aiService;
    }

    // DTOs to avoid Map.of generic inference and null constraints
    public record AdminPortfolioItem(
            Long id,
            String title,
            String description,
            String link,
            String githubRepo,
            String status,
            String type,
            String completionStatus,
            String priority,
            List<String> mainTechnologies,
            Long sourceRepositoryId,
            String linkType,
            String repositoryFullName,
            String repositoryUrl,
            Integer repositoryStars,
            Boolean protectDescription,
            Boolean protectLiveDemoUrl,
            Boolean protectSkills,
            Boolean protectExperiences,
            Double overallCompleteness
    ) {}

    public record Pagination(int page, int size, long totalElements, int totalPages) {}

    /**
     * Response DTO for Claude AI repository analysis.
     * Contains AI-generated insights, repository metadata, and README content.
     */
    public record ClaudeAnalysisResponse(
            String insights,
            String repositoryName,
            String readmeContent
    ) {}

    /**
     * Return paginated portfolio projects with completion metrics for Admin table.
     * Supports filtering by search, status, hasDescription, hasLiveDemo, protectedOnly, unlinkedOnly, linkType
     */
    @GetMapping({"/completion", "/completion/"})
    public ResponseEntity<Map<String, Object>> getPortfolioCompletion(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean hasDescription,
            @RequestParam(required = false) Boolean hasLiveDemo,
            @RequestParam(required = false) Boolean protectedOnly,
            @RequestParam(required = false) Boolean unlinkedOnly,
            @RequestParam(required = false) String linkType) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));

        // Apply filters using specification pattern or manual filtering
        Page<PortfolioProjectJpaEntity> result = portfolioRepository.findAll((root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            // Search filter (title or description)
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), searchPattern),
                    cb.like(cb.lower(root.get("description")), searchPattern)
                ));
            }

            // Status filter
            if (status != null && !status.trim().isEmpty()) {
                try {
                    PortfolioProjectJpaEntity.ProjectStatusJpa statusEnum =
                        PortfolioProjectJpaEntity.ProjectStatusJpa.valueOf(status.toUpperCase());
                    predicates.add(cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException ignored) {}
            }

            // Has description filter
            if (hasDescription != null) {
                if (hasDescription) {
                    predicates.add(cb.and(
                        cb.isNotNull(root.get("description")),
                        cb.notEqual(root.get("description"), "")
                    ));
                } else {
                    predicates.add(cb.or(
                        cb.isNull(root.get("description")),
                        cb.equal(root.get("description"), "")
                    ));
                }
            }

            // Has live demo filter
            if (hasLiveDemo != null) {
                if (hasLiveDemo) {
                    predicates.add(cb.and(
                        cb.isNotNull(root.get("link")),
                        cb.notEqual(root.get("link"), "")
                    ));
                } else {
                    predicates.add(cb.or(
                        cb.isNull(root.get("link")),
                        cb.equal(root.get("link"), "")
                    ));
                }
            }

            // Protected only filter (at least one field protected)
            if (protectedOnly != null && protectedOnly) {
                predicates.add(cb.or(
                    cb.equal(root.get("protectDescription"), true),
                    cb.equal(root.get("protectLiveDemoUrl"), true),
                    cb.equal(root.get("protectSkills"), true),
                    cb.equal(root.get("protectExperiences"), true)
                ));
            }

            // Unlinked only filter
            if (unlinkedOnly != null && unlinkedOnly) {
                predicates.add(cb.isNull(root.get("sourceRepositoryId")));
            }

            // Link type filter
            if (linkType != null && !linkType.trim().isEmpty()) {
                try {
                    PortfolioProjectJpaEntity.LinkTypeJpa linkTypeEnum =
                        PortfolioProjectJpaEntity.LinkTypeJpa.valueOf(linkType.toUpperCase());
                    predicates.add(cb.equal(root.get("linkType"), linkTypeEnum));
                } catch (IllegalArgumentException ignored) {}
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable);

        List<AdminPortfolioItem> projects = result.getContent().stream()
                .map(completionService::calculateCompletion)
                .map(dto -> {
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
                    return new AdminPortfolioItem(
                            dto.getId(),
                            dto.getTitle(),
                            dto.getDescription(),
                            dto.getLink(),
                            dto.getGithubRepo(),
                            dto.getStatus(),
                            dto.getType(),
                            dto.getCompletionStatus(),
                            dto.getPriority(),
                            dto.getMainTechnologies(),
                            dto.getSourceRepositoryId(),
                            dto.getLinkType(),
                            repoFullName,
                            repoUrl,
                            repoStars,
                            dto.getProtectDescription(),
                            dto.getProtectLiveDemoUrl(),
                            dto.getProtectSkills(),
                            dto.getProtectExperiences(),
                            dto.getOverallCompleteness()
                    );
                })
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("projects", projects);
        body.put("pagination", new Pagination(result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages()));

        return ResponseEntity.ok(body);
    }

    /** Link portfolio project to source repository (manual) */
    @PutMapping("/{id}/link-repo")
    public ResponseEntity<Map<String, Object>> linkToSource(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        try {
            Long sourceRepositoryId = Long.valueOf(payload.get("repositoryId").toString());
            PortfolioProject result = portfolioService.linkToSourceRepository(id, sourceRepositoryId, LinkType.MANUAL);
            Map<String, Object> resp = new HashMap<>();
            resp.put("status", "success");
            resp.put("projectId", result.getId());
            resp.put("sourceRepositoryId", result.getSourceRepositoryId());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("Failed to link portfolio {} to source: {}", id, e.getMessage(), e);
            Map<String, Object> err = new HashMap<>();
            err.put("status", "error");
            err.put("message", e.getMessage());
            return ResponseEntity.status(400).body(err);
        }
    }

    /** Unlink portfolio project from source repository */
    @DeleteMapping("/{id}/link-repo")
    public ResponseEntity<Map<String, Object>> unlinkSource(@PathVariable Long id) {
        try {
            PortfolioProject result = portfolioService.unlinkFromSourceRepository(id);
            Map<String, Object> resp = new HashMap<>();
            resp.put("status", "success");
            resp.put("projectId", result.getId());
            resp.put("sourceRepositoryId", result.getSourceRepositoryId());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("Failed to unlink portfolio {}: {}", id, e.getMessage(), e);
            Map<String, Object> err = new HashMap<>();
            err.put("status", "error");
            err.put("message", e.getMessage());
            return ResponseEntity.status(400).body(err);
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
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("status", "success");
                    resp.put("id", saved.getId());
                    return ResponseEntity.ok(resp);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Protect/unprotect a single field for a portfolio project
     * Frontend endpoint: PATCH /api/admin/portfolio/{id}/protect
     */
    @PatchMapping("/{id}/protect")
    public ResponseEntity<Map<String, Object>> protectField(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        String field = (String) payload.get("field");
        Boolean protect = Boolean.parseBoolean(payload.get("protect").toString());

        if (field == null) {
            Map<String, Object> errorResp = new HashMap<>();
            errorResp.put("status", "error");
            errorResp.put("message", "Field name is required");
            return ResponseEntity.badRequest().body(errorResp);
        }

        return portfolioRepository.findById(id)
                .map(existing -> {
                    var builder = existing.toBuilder();
                    boolean fieldFound = true;
                    switch (field.toLowerCase()) {
                        case "description" -> builder.protectDescription(protect);
                        case "livedemouri", "link", "livedemourl" -> builder.protectLiveDemoUrl(protect);
                        case "skills" -> builder.protectSkills(protect);
                        case "experiences" -> builder.protectExperiences(protect);
                        default -> fieldFound = false;
                    }

                    if (!fieldFound) {
                        Map<String, Object> errorResp = new HashMap<>();
                        errorResp.put("status", "error");
                        errorResp.put("message", "Unknown field: " + field);
                        return ResponseEntity.badRequest().body(errorResp);
                    }

                    var saved = portfolioRepository.save(builder.build());
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("status", "success");
                    resp.put("id", saved.getId());
                    resp.put("field", field);
                    resp.put("protected", protect);
                    return ResponseEntity.ok(resp);
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
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("status", "success");
                    resp.put("id", saved.getId());
                    return ResponseEntity.ok(resp);
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
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("status", "success");
                    resp.put("id", saved.getId());
                    return ResponseEntity.ok(resp);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Bulk actions on multiple portfolio projects
     * Supported actions: PROTECT_DESCRIPTION, UNPROTECT_DESCRIPTION, CHANGE_STATUS, DELETE
     */
    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> bulkAction(@RequestBody Map<String, Object> payload) {
        try {
            String action = (String) payload.get("action");
            @SuppressWarnings("unchecked")
            List<Integer> projectIdsInt = (List<Integer>) payload.get("projectIds");
            String value = (String) payload.get("value");

            if (action == null || projectIdsInt == null || projectIdsInt.isEmpty()) {
                Map<String, Object> errorResp = new HashMap<>();
                errorResp.put("status", "error");
                errorResp.put("message", "Action and projectIds are required");
                return ResponseEntity.badRequest().body(errorResp);
            }

            // Convert Integer list to Long list
            List<Long> projectIds = projectIdsInt.stream().map(Long::valueOf).toList();

            int successCount = 0;
            int failureCount = 0;
            List<String> errors = new java.util.ArrayList<>();

            switch (action.toUpperCase()) {
                case "PROTECT_DESCRIPTION" -> {
                    for (Long projectId : projectIds) {
                        try {
                            portfolioRepository.findById(projectId).ifPresent(project -> {
                                var saved = portfolioRepository.save(project.toBuilder().protectDescription(true).build());
                                log.debug("Protected description for project {}", saved.getId());
                            });
                            successCount++;
                        } catch (Exception e) {
                            failureCount++;
                            errors.add("Project " + projectId + ": " + e.getMessage());
                            log.error("Failed to protect description for project {}: {}", projectId, e.getMessage());
                        }
                    }
                }
                case "UNPROTECT_DESCRIPTION" -> {
                    for (Long projectId : projectIds) {
                        try {
                            portfolioRepository.findById(projectId).ifPresent(project -> {
                                var saved = portfolioRepository.save(project.toBuilder().protectDescription(false).build());
                                log.debug("Unprotected description for project {}", saved.getId());
                            });
                            successCount++;
                        } catch (Exception e) {
                            failureCount++;
                            errors.add("Project " + projectId + ": " + e.getMessage());
                            log.error("Failed to unprotect description for project {}: {}", projectId, e.getMessage());
                        }
                    }
                }
                case "CHANGE_STATUS" -> {
                    if (value == null) {
                        Map<String, Object> errorResp = new HashMap<>();
                        errorResp.put("status", "error");
                        errorResp.put("message", "Value is required for CHANGE_STATUS action");
                        return ResponseEntity.badRequest().body(errorResp);
                    }
                    PortfolioProjectJpaEntity.ProjectCompletionStatusJpa newStatus =
                            PortfolioProjectJpaEntity.ProjectCompletionStatusJpa.valueOf(value.toUpperCase());

                    for (Long projectId : projectIds) {
                        try {
                            portfolioRepository.findById(projectId).ifPresent(project -> {
                                var saved = portfolioRepository.save(project.toBuilder().completionStatus(newStatus).build());
                                log.debug("Changed status to {} for project {}", newStatus, saved.getId());
                            });
                            successCount++;
                        } catch (Exception e) {
                            failureCount++;
                            errors.add("Project " + projectId + ": " + e.getMessage());
                            log.error("Failed to change status for project {}: {}", projectId, e.getMessage());
                        }
                    }
                }
                case "DELETE" -> {
                    for (Long projectId : projectIds) {
                        try {
                            portfolioService.deleteProject(projectId);
                            successCount++;
                            log.debug("Deleted project {}", projectId);
                        } catch (Exception e) {
                            failureCount++;
                            errors.add("Project " + projectId + ": " + e.getMessage());
                            log.error("Failed to delete project {}: {}", projectId, e.getMessage());
                        }
                    }
                }
                default -> {
                    Map<String, Object> errorResp = new HashMap<>();
                    errorResp.put("status", "error");
                    errorResp.put("message", "Unknown action: " + action);
                    return ResponseEntity.badRequest().body(errorResp);
                }
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("status", "success");
            resp.put("action", action);
            resp.put("successCount", successCount);
            resp.put("failureCount", failureCount);
            if (!errors.isEmpty()) {
                resp.put("errors", errors);
            }

            log.info("Bulk action {} completed: {} succeeded, {} failed", action, successCount, failureCount);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            log.error("Failed to execute bulk action: {}", e.getMessage(), e);
            Map<String, Object> errorResp = new HashMap<>();
            errorResp.put("status", "error");
            errorResp.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResp);
        }
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
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("status", "success");
                    resp.put("id", saved.getId());
                    return ResponseEntity.ok(resp);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** Delete portfolio project by ID */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        try {
            portfolioService.deleteProject(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Portfolio project not found for deletion: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to delete portfolio project {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /** Resync a single portfolio project using its linked source repository */
    @PostMapping("/{id}/resync")
    public ResponseEntity<Map<String, Object>> resyncProject(@PathVariable Long id) {
        try {
            syncSchedulerService.resyncPortfolioProject(id);
            Map<String, Object> resp = new HashMap<>();
            resp.put("status", "accepted");
            resp.put("message", "Resync triggered");
            return ResponseEntity.accepted().body(resp);
        } catch (Exception e) {
            log.error("Failed to resync portfolio {}: {}", id, e.getMessage(), e);
            Map<String, Object> err = new HashMap<>();
            err.put("status", "error");
            err.put("message", e.getMessage());
            return ResponseEntity.status(500).body(err);
        }
    }

    /**
     * Analyze portfolio project repository with Claude AI.
     * Returns AI-generated insights about the project, including README analysis and recommendations.
     *
     * @param id Portfolio project ID
     * @return ClaudeAnalysisResponse with insights, repository name, and README content
     */
    @PostMapping("/{id}/analyze")
    public ResponseEntity<ClaudeAnalysisResponse> analyzeWithClaude(@PathVariable Long id) {
        log.info("Analyzing portfolio project {} with Claude AI", id);

        try {
            // 1. Get portfolio project
            var portfolioProject = portfolioRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Portfolio project not found: " + id));

            // 2. Verify it's linked to a source repository
            if (portfolioProject.getSourceRepositoryId() == null) {
                log.warn("Cannot analyze project {}: not linked to a source repository", id);
                return ResponseEntity
                        .badRequest()
                        .body(new ClaudeAnalysisResponse(
                                "Error: This project is not linked to a GitHub repository. Please link it first.",
                                null,
                                null
                        ));
            }

            // 3. Get source repository
            SourceRepositoryJpaEntity sourceRepo = sourceRepositoryRepository
                    .findById(portfolioProject.getSourceRepositoryId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Source repository not found: " + portfolioProject.getSourceRepositoryId()));

            // 4. Call Claude AI service for deep analysis
            log.debug("Calling Claude AI to analyze repository: {}", sourceRepo.getFullName());

            // Build narrative analysis prompt
            String systemPrompt = buildNarrativeSystemPrompt();
            String userPrompt = buildNarrativeUserPrompt(sourceRepo, portfolioProject);

            String narrativeInsights = aiService.chat(systemPrompt, userPrompt);

            log.info("Successfully analyzed project {} with Claude AI", id);

            return ResponseEntity.ok(new ClaudeAnalysisResponse(
                    narrativeInsights,
                    sourceRepo.getFullName(),
                    sourceRepo.getReadmeMarkdown()
            ));

        } catch (IllegalArgumentException e) {
            log.error("Validation error analyzing project {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(404)
                    .body(new ClaudeAnalysisResponse(
                            "Error: " + e.getMessage(),
                            null,
                            null
                    ));
        } catch (Exception e) {
            log.error("Failed to analyze portfolio project {} with Claude: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(500)
                    .body(new ClaudeAnalysisResponse(
                            "Error: Failed to analyze repository with Claude AI. " + e.getMessage(),
                            null,
                            null
                    ));
        }
    }

    private String buildNarrativeSystemPrompt() {
        return """
            Eres un arquitecto conceptual analizando proyectos de software para Bernard Uriza Orozco.

            Bernard es un sistema cognitivo en forma humana que integra ingeniería de software con filosofía,
            ética y estética como si fueran un mismo lenguaje estructurado. Opera como un arquitecto conceptual:
            diseña sistemas no solo para que funcionen, sino para que se comprendan a sí mismos.

            Tu misión al analizar proyectos:
            - NO generar listas mecánicas con emojis genéricos
            - Explorar la arquitectura conceptual del proyecto
            - Detectar patrones filosóficos en las decisiones de diseño
            - Analizar la coherencia entre propósito, implementación y estructura
            - Revelar el pensamiento sistémico detrás del código
            - Combinar precisión técnica con sensibilidad poética
            - Identificar tanto lo explícito como lo implícito en el diseño

            Escribe como un arquitecto conceptual que busca coherencia entre forma, función y sentido.
            El proyecto no es solo código - es una manifestación de pensamiento estructurado.
            """;
    }

    private String buildNarrativeUserPrompt(SourceRepositoryJpaEntity sourceRepo, PortfolioProjectJpaEntity portfolioProject) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Analiza este repositorio GitHub con profundidad arquitectónica y filosófica:\n\n");

        prompt.append("REPOSITORIO: ").append(sourceRepo.getFullName()).append("\n");
        prompt.append("DESCRIPCIÓN: ").append(sourceRepo.getDescription() != null ? sourceRepo.getDescription() : "No description").append("\n");
        prompt.append("LENGUAJE: ").append(sourceRepo.getLanguage() != null ? sourceRepo.getLanguage() : "Unknown").append("\n");
        prompt.append("TOPICS: ").append(sourceRepo.getTopics() != null ? String.join(", ", sourceRepo.getTopics()) : "None").append("\n");
        prompt.append("STARS: ").append(sourceRepo.getStargazersCount()).append("\n\n");

        if (sourceRepo.getReadmeMarkdown() != null && !sourceRepo.getReadmeMarkdown().trim().isEmpty()) {
            String truncatedReadme = sourceRepo.getReadmeMarkdown().length() > 3000
                ? sourceRepo.getReadmeMarkdown().substring(0, 3000) + "..."
                : sourceRepo.getReadmeMarkdown();
            prompt.append("README:\n").append(truncatedReadme).append("\n\n");
        }

        prompt.append("Genera un análisis narrativo profundo (máximo 800 palabras) que revele:\n");
        prompt.append("1. La arquitectura conceptual y decisiones de diseño fundamentales\n");
        prompt.append("2. Los patrones de pensamiento sistémico evidentes en la estructura\n");
        prompt.append("3. La coherencia (o disonancia) entre propósito declarado e implementación\n");
        prompt.append("4. Las tecnologías como instrumentos de transformación, no solo herramientas\n");
        prompt.append("5. Insights sobre cómo este proyecto manifiesta pensamiento estructurado\n");
        prompt.append("6. Recomendaciones para profundizar la coherencia arquitectónica\n\n");
        prompt.append("Escribe en español, con precisión técnica y sensibilidad poética.");

        return prompt.toString();
    }
}
