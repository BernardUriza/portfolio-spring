package com.portfolio.controller;

import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaRepository;
import com.portfolio.aspect.RateLimit;
import com.portfolio.aspect.RequiresFeature;
import com.portfolio.core.domain.project.LinkType;
import com.portfolio.dto.LinkRepositoryRequestDto;
import com.portfolio.dto.PortfolioCompletionDto;
import com.portfolio.dto.ProtectionUpdateDto;
import com.portfolio.service.PortfolioService;
import com.portfolio.service.PortfolioCompletionService;
import com.portfolio.service.RateLimitingService;
import com.portfolio.service.SyncSchedulerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/admin/portfolio")
@RequiredArgsConstructor
@Slf4j
public class PortfolioAdminController {
    
    private final PortfolioProjectJpaRepository portfolioProjectRepository;
    private final PortfolioService portfolioService;
    private final PortfolioCompletionService portfolioCompletionService;
    private final SyncSchedulerService syncSchedulerService;
    
    /**
     * Get portfolio completion overview with enriched DTOs
     */
    @GetMapping("/completion")
    @RequiresFeature("portfolio_management")
    @RateLimit(type = RateLimitingService.RateLimitType.ADMIN_ENDPOINTS)
    public ResponseEntity<Map<String, Object>> getPortfolioCompletion(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            WebRequest request) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? 
            Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        
        Page<PortfolioProjectJpaEntity> portfolioPage = portfolioProjectRepository.findAll(pageRequest);
        
        // Create ETag from page content and last modified times
        String etag = generateETag(portfolioPage.getContent(), page, size, sortBy, sortDir);
        
        // Check if client has current version
        if (request.checkNotModified(etag)) {
            log.debug("Portfolio completion data not modified, returning 304 for ETag: {}", etag);
            return ResponseEntity.status(304).build();
        }
        
        List<PortfolioCompletionDto> completionDtos = portfolioPage.getContent()
                .stream()
                .map(portfolioCompletionService::calculateCompletion)
                .toList();
        
        // Calculate aggregate metrics
        double avgCompleteness = completionDtos.stream()
                .mapToDouble(PortfolioCompletionDto::getOverallCompleteness)
                .average()
                .orElse(0.0);
        
        long totalProjects = portfolioPage.getTotalElements();
        long linkedProjects = completionDtos.stream()
                .mapToLong(dto -> dto.getSourceRepositoryId() != null ? 1 : 0)
                .sum();
        
        long fullyCompleteProjects = completionDtos.stream()
                .mapToLong(dto -> dto.getOverallCompleteness() >= 0.9 ? 1 : 0)
                .sum();
        
        Map<String, Object> response = Map.of(
            "projects", completionDtos,
            "pagination", Map.of(
                "page", portfolioPage.getNumber(),
                "size", portfolioPage.getSize(),
                "totalElements", portfolioPage.getTotalElements(),
                "totalPages", portfolioPage.getTotalPages()
            ),
            "metrics", Map.of(
                "totalProjects", totalProjects,
                "linkedProjects", linkedProjects,
                "fullyCompleteProjects", fullyCompleteProjects,
                "averageCompleteness", Math.round(avgCompleteness * 100.0) / 100.0
            )
        );
        
        return ResponseEntity.ok()
            .eTag(etag)
            .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10))
                .cachePrivate()
                .mustRevalidate())
            .body(response);
    }
    
    /**
     * Get individual portfolio project with completion details
     */
    @GetMapping("/{id}/completion")
    public ResponseEntity<PortfolioCompletionDto> getProjectCompletion(
            @PathVariable Long id, 
            WebRequest request) {
        Optional<PortfolioProjectJpaEntity> portfolioOpt = portfolioProjectRepository.findById(id);
        if (portfolioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        PortfolioProjectJpaEntity portfolio = portfolioOpt.get();
        
        // Create ETag from project ID and last modified time
        String etag = generateProjectETag(portfolio);
        
        // Check if client has current version
        if (request.checkNotModified(etag)) {
            log.debug("Portfolio project completion not modified, returning 304 for project {}, ETag: {}", 
                     id, etag);
            return ResponseEntity.status(304).build();
        }
        
        PortfolioCompletionDto completion = portfolioCompletionService.calculateCompletion(portfolio);
        
        return ResponseEntity.ok()
            .eTag(etag)
            .cacheControl(CacheControl.maxAge(Duration.ofMinutes(15))
                .cachePrivate()
                .mustRevalidate())
            .body(completion);
    }
    
    /**
     * Link portfolio project to source repository
     */
    @PutMapping("/{id}/link-repo")
    @RequiresFeature("portfolio_management")
    @RateLimit(type = RateLimitingService.RateLimitType.ADMIN_ENDPOINTS)
    public ResponseEntity<Map<String, Object>> linkToRepository(
            @PathVariable Long id,
            @Valid @RequestBody LinkRepositoryRequestDto request) {
        
        try {
            LinkType linkType = LinkType.valueOf(request.getLinkType().toUpperCase());
            
            portfolioService.linkToSourceRepository(id, request.getSourceRepositoryId(), linkType);
            
            // Evict cache for this project since completion may have changed
            portfolioCompletionService.evictCompletionCache(id);
            
            log.info("Successfully linked portfolio project {} to source repository {} with type {}", 
                     id, request.getSourceRepositoryId(), linkType);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Portfolio project linked to source repository successfully",
                "portfolioProjectId", id,
                "sourceRepositoryId", request.getSourceRepositoryId(),
                "linkType", linkType.name()
            ));
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid link request for portfolio {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Failed to link portfolio project {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Failed to link portfolio project: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Unlink portfolio project from source repository
     */
    @DeleteMapping("/{id}/link-repo")
    public ResponseEntity<Map<String, Object>> unlinkFromRepository(@PathVariable Long id) {
        try {
            portfolioService.unlinkFromSourceRepository(id);
            
            // Evict cache for this project since completion may have changed
            portfolioCompletionService.evictCompletionCache(id);
            
            log.info("Successfully unlinked portfolio project {} from source repository", id);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Portfolio project unlinked from source repository successfully",
                "portfolioProjectId", id
            ));
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid unlink request for portfolio {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Failed to unlink portfolio project {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Failed to unlink portfolio project: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Trigger resync for individual portfolio project
     */
    @PostMapping("/{id}/resync")
    @RequiresFeature("ai_curation")
    @RateLimit(type = RateLimitingService.RateLimitType.AI_CURATION)
    public ResponseEntity<Map<String, Object>> resyncProject(@PathVariable Long id) {
        try {
            syncSchedulerService.resyncPortfolioProject(id);
            
            log.info("Successfully triggered resync for portfolio project {}", id);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Portfolio project resync completed successfully",
                "portfolioProjectId", id,
                "resyncedAt", LocalDateTime.now().toString()
            ));
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Invalid resync request for portfolio {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Failed to resync portfolio project {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Failed to resync portfolio project: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Update field protection settings
     */
    @PatchMapping("/{id}/protection")
    public ResponseEntity<Map<String, Object>> updateProtection(
            @PathVariable Long id,
            @RequestBody ProtectionUpdateDto protectionUpdate) {
        
        try {
            Optional<PortfolioProjectJpaEntity> portfolioOpt = portfolioProjectRepository.findById(id);
            if (portfolioOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            PortfolioProjectJpaEntity portfolio = portfolioOpt.get();
            PortfolioProjectJpaEntity.PortfolioProjectJpaEntityBuilder builder = portfolio.toBuilder();
            
            // Update only non-null protection flags
            if (protectionUpdate.getProtectDescription() != null) {
                builder.protectDescription(protectionUpdate.getProtectDescription());
            }
            if (protectionUpdate.getProtectLiveDemoUrl() != null) {
                builder.protectLiveDemoUrl(protectionUpdate.getProtectLiveDemoUrl());
            }
            if (protectionUpdate.getProtectSkills() != null) {
                builder.protectSkills(protectionUpdate.getProtectSkills());
            }
            if (protectionUpdate.getProtectExperiences() != null) {
                builder.protectExperiences(protectionUpdate.getProtectExperiences());
            }
            
            builder.updatedAt(LocalDateTime.now());
            
            PortfolioProjectJpaEntity updated = portfolioProjectRepository.save(builder.build());
            
            // Evict cache for this project since protection settings affect completion
            portfolioCompletionService.evictCompletionCache(id);
            
            log.info("Updated protection settings for portfolio project {}", id);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Protection settings updated successfully",
                "portfolioProjectId", id,
                "protections", Map.of(
                    "protectDescription", updated.getProtectDescription(),
                    "protectLiveDemoUrl", updated.getProtectLiveDemoUrl(),
                    "protectSkills", updated.getProtectSkills(),
                    "protectExperiences", updated.getProtectExperiences()
                )
            ));
            
        } catch (Exception e) {
            log.error("Failed to update protection settings for portfolio {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Failed to update protection settings: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get projects that need curation
     */
    @GetMapping("/needs-curation")
    public ResponseEntity<List<PortfolioCompletionDto>> getProjectsNeedingCuration() {
        List<PortfolioProjectJpaEntity> needsCuration = portfolioService.getProjectsNeedingCuration();
        
        List<PortfolioCompletionDto> completionDtos = needsCuration.stream()
                .map(portfolioCompletionService::calculateCompletion)
                .toList();
        
        return ResponseEntity.ok(completionDtos);
    }
    
    /**
     * Generate ETag for portfolio completion list
     */
    private String generateETag(List<PortfolioProjectJpaEntity> projects, int page, int size, String sortBy, String sortDir) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            
            // Include pagination and sorting parameters
            digest.update(String.format("page:%d,size:%d,sort:%s,%s", page, size, sortBy, sortDir).getBytes(StandardCharsets.UTF_8));
            
            // Include project data that affects completion calculation
            for (PortfolioProjectJpaEntity project : projects) {
                String projectData = String.format("id:%d,updated:%s,sourceRepo:%s", 
                    project.getId(), 
                    project.getUpdatedAt() != null ? project.getUpdatedAt().toString() : "null",
                    project.getSourceRepositoryId() != null ? project.getSourceRepositoryId().toString() : "null");
                digest.update(projectData.getBytes(StandardCharsets.UTF_8));
            }
            
            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            
            return "\"" + hexString.toString() + "\"";
        } catch (Exception e) {
            log.warn("Failed to generate ETag for completion list, using timestamp fallback", e);
            return "\"" + System.currentTimeMillis() + "\"";
        }
    }
    
    /**
     * Generate ETag for individual portfolio project completion
     */
    private String generateProjectETag(PortfolioProjectJpaEntity project) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            
            String projectData = String.format("id:%d,updated:%s,sourceRepo:%s,protection:%s,%s,%s,%s", 
                project.getId(),
                project.getUpdatedAt() != null ? project.getUpdatedAt().toString() : "null",
                project.getSourceRepositoryId() != null ? project.getSourceRepositoryId().toString() : "null",
                project.getProtectDescription(),
                project.getProtectLiveDemoUrl(),
                project.getProtectSkills(),
                project.getProtectExperiences());
            
            digest.update(projectData.getBytes(StandardCharsets.UTF_8));
            
            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            
            return "\"" + hexString.toString() + "\"";
        } catch (Exception e) {
            log.warn("Failed to generate ETag for project {}, using timestamp fallback", project.getId(), e);
            return "\"" + project.getId() + "_" + System.currentTimeMillis() + "\"";
        }
    }
}