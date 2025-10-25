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
     */
    @GetMapping({"/completion", "/completion/"})
    public ResponseEntity<Map<String, Object>> getPortfolioCompletion(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<PortfolioProjectJpaEntity> result = portfolioRepository.findAll(pageable);

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

            // 4. Call Claude AI service for analysis
            log.debug("Calling Claude AI to analyze repository: {}", sourceRepo.getFullName());
            AIServicePort.ClaudeAnalysisResult analysis = aiService.analyzeRepository(
                    sourceRepo.getName(),
                    sourceRepo.getDescription(),
                    sourceRepo.getReadmeMarkdown(),
                    sourceRepo.getTopics(),
                    sourceRepo.getLanguage()
            );

            // 5. Build AI-generated insights message
            StringBuilder insights = new StringBuilder();
            insights.append("🤖 Claude AI Analysis\n\n");
            insights.append("📊 Project Overview:\n");
            insights.append("• Name: ").append(analysis.project.name).append("\n");
            insights.append("• Description: ").append(analysis.project.description).append("\n");

            if (analysis.project.estimatedDurationWeeks != null) {
                insights.append("• Estimated Duration: ").append(analysis.project.estimatedDurationWeeks).append(" weeks\n");
            }

            insights.append("\n🔧 Technologies:\n");
            if (analysis.project.technologies != null && !analysis.project.technologies.isEmpty()) {
                analysis.project.technologies.forEach(tech ->
                    insights.append("• ").append(tech).append("\n")
                );
            } else {
                insights.append("• No technologies detected\n");
            }

            insights.append("\n🎯 Skills Identified:\n");
            if (analysis.skills != null && !analysis.skills.isEmpty()) {
                analysis.skills.forEach(skill ->
                    insights.append("• ").append(skill).append("\n")
                );
            } else {
                insights.append("• No skills identified\n");
            }

            insights.append("\n💼 Relevant Experiences:\n");
            if (analysis.experiences != null && !analysis.experiences.isEmpty()) {
                analysis.experiences.forEach(exp ->
                    insights.append("• ").append(exp).append("\n")
                );
            } else {
                insights.append("• No experiences identified\n");
            }

            if (analysis.project.url != null) {
                insights.append("\n🔗 Project URL: ").append(analysis.project.url).append("\n");
            }

            insights.append("\n💡 Recommendations:\n");
            insights.append("• Review the identified skills and experiences for accuracy\n");
            insights.append("• Update project description if needed\n");
            insights.append("• Verify estimated duration aligns with project scope\n");

            log.info("Successfully analyzed project {} with Claude AI", id);

            return ResponseEntity.ok(new ClaudeAnalysisResponse(
                    insights.toString(),
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
}
