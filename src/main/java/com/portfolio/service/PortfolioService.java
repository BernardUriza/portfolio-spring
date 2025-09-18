package com.portfolio.service;

import com.portfolio.adapter.out.persistence.jpa.SourceRepositoryJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.SourceRepositoryJpaRepository;
import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaRepository;
import com.portfolio.core.domain.project.PortfolioProject;
import com.portfolio.core.domain.project.LinkType;
import com.portfolio.core.domain.project.ProjectStatus;
import com.portfolio.core.domain.project.ProjectType;
import com.portfolio.core.port.out.AIServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PortfolioService {
    private static final Logger log = LoggerFactory.getLogger(PortfolioService.class);

    public final PortfolioProjectJpaRepository portfolioProjectRepository;
    private final SourceRepositoryJpaRepository sourceRepositoryRepository;
    private final AIServicePort aiService;
    private final SyncMonitorService syncMonitorService;
    private final OptimisticLockingService optimisticLockingService;
    private final AuditTrailService auditTrailService;

    public PortfolioService(PortfolioProjectJpaRepository portfolioProjectRepository,
                            SourceRepositoryJpaRepository sourceRepositoryRepository,
                            AIServicePort aiService,
                            SyncMonitorService syncMonitorService,
                            OptimisticLockingService optimisticLockingService,
                            AuditTrailService auditTrailService) {
        this.portfolioProjectRepository = portfolioProjectRepository;
        this.sourceRepositoryRepository = sourceRepositoryRepository;
        this.aiService = aiService;
        this.syncMonitorService = syncMonitorService;
        this.optimisticLockingService = optimisticLockingService;
        this.auditTrailService = auditTrailService;
    }
    
    /**
     * Curate portfolio project from source repository using Claude AI
     */
    @Transactional
    public PortfolioProject curateFromSource(Long sourceRepositoryId) {
        log.debug("Starting curation for source repository ID: {}", sourceRepositoryId);
        
        // Get source repository
        Optional<SourceRepositoryJpaEntity> sourceOpt = sourceRepositoryRepository.findById(sourceRepositoryId);
        if (sourceOpt.isEmpty()) {
            throw new IllegalArgumentException("Source repository not found: " + sourceRepositoryId);
        }
        
        SourceRepositoryJpaEntity source = sourceOpt.get();
        
        // Check if portfolio project already exists for this source
        Optional<PortfolioProjectJpaEntity> existingOpt = portfolioProjectRepository.findBySourceRepositoryId(sourceRepositoryId);
        
        try {
            // Call Claude API for analysis
            AIServicePort.ClaudeAnalysisResult analysis = aiService.analyzeRepository(
                source.getName(),
                source.getDescription(),
                source.getReadmeMarkdown(),
                source.getTopics(),
                source.getLanguage()
            );
            
            PortfolioProjectJpaEntity portfolioEntity;
            
            if (existingOpt.isPresent()) {
                // Update existing portfolio project respecting protections
                portfolioEntity = updateExistingPortfolioProject(existingOpt.get(), source, analysis);
                syncMonitorService.appendLog("INFO", 
                    String.format("Updated portfolio project '%s' from source repository", analysis.project.name));
            } else {
                // Create new portfolio project
                portfolioEntity = createNewPortfolioProject(source, analysis);
                syncMonitorService.appendLog("INFO", 
                    String.format("Created new portfolio project '%s' from source repository", analysis.project.name));
            }
            
            // Mark source as synced
            source.setSyncStatus(SourceRepositoryJpaEntity.SyncStatus.SYNCED);
            source.setLastSyncAttempt(LocalDateTime.now());
            source.setSyncErrorMessage(null);
            sourceRepositoryRepository.save(source);
            
            return convertToDomain(portfolioEntity);
            
        } catch (Exception e) {
            log.error("Error curating from source repository {}: {}", sourceRepositoryId, e.getMessage(), e);
            
            // Mark source as failed
            source.setSyncStatus(SourceRepositoryJpaEntity.SyncStatus.FAILED);
            source.setLastSyncAttempt(LocalDateTime.now());
            source.setSyncErrorMessage(e.getMessage());
            sourceRepositoryRepository.save(source);
            
            syncMonitorService.appendLog("ERROR", 
                String.format("Failed to curate from source '%s': %s", source.getName(), e.getMessage()));
            
            throw new RuntimeException("Curation failed for source: " + sourceRepositoryId, e);
        }
    }
    
    private PortfolioProjectJpaEntity createNewPortfolioProject(SourceRepositoryJpaEntity source, 
                                                               AIServicePort.ClaudeAnalysisResult analysis) {
        PortfolioProjectJpaEntity entity = PortfolioProjectJpaEntity.builder()
                .title(analysis.project.name)
                .description(analysis.project.description)
                .link(analysis.project.url)
                .githubRepo(source.getGithubRepoUrl())
                .createdDate(LocalDate.now())
                .estimatedDurationWeeks(analysis.project.estimatedDurationWeeks)
                .status(PortfolioProjectJpaEntity.ProjectStatusJpa.ACTIVE)
                .type(source.getFork() ? PortfolioProjectJpaEntity.ProjectTypeJpa.OPEN_SOURCE : 
                      PortfolioProjectJpaEntity.ProjectTypeJpa.PERSONAL)
                .mainTechnologies(analysis.project.technologies)
                .sourceRepositoryId(source.getId())
                .linkType(PortfolioProjectJpaEntity.LinkTypeJpa.AUTO)
                .completionStatus(PortfolioProjectJpaEntity.ProjectCompletionStatusJpa.BACKLOG)
                .protectDescription(false)
                .protectLiveDemoUrl(false)
                .protectSkills(false)
                .protectExperiences(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        return portfolioProjectRepository.save(entity);
    }
    
    private PortfolioProjectJpaEntity updateExistingPortfolioProject(PortfolioProjectJpaEntity existing,
                                                                    SourceRepositoryJpaEntity source,
                                                                    AIServicePort.ClaudeAnalysisResult analysis) {
        // Update only non-protected fields
        PortfolioProjectJpaEntity.Builder builder = new PortfolioProjectJpaEntity.Builder(existing);
        
        // Always update title (no protection for title)
        builder.title(analysis.project.name);
        
        // Update description only if not protected
        if (!existing.getProtectDescription()) {
            builder.description(analysis.project.description);
            syncMonitorService.appendLog("DEBUG", "Updated description for portfolio project: " + analysis.project.name);
        } else {
            syncMonitorService.appendLog("DEBUG", "Skipped description update (protected): " + analysis.project.name);
        }
        
        // Update live demo URL only if not protected
        if (!existing.getProtectLiveDemoUrl()) {
            builder.link(analysis.project.url);
            syncMonitorService.appendLog("DEBUG", "Updated live demo URL for portfolio project: " + analysis.project.name);
        } else {
            syncMonitorService.appendLog("DEBUG", "Skipped live demo URL update (protected): " + analysis.project.name);
        }
        
        // Update technologies
        builder.mainTechnologies(analysis.project.technologies);
        
        // Update github repo URL
        builder.githubRepo(source.getGithubRepoUrl());
        
        // Update timestamp
        builder.updatedAt(LocalDateTime.now());
        
        PortfolioProjectJpaEntity updated = builder.build();
        return portfolioProjectRepository.save(updated);
    }
    
    /**
     * Link existing portfolio project to source repository
     */
    @Transactional
    public PortfolioProject linkToSourceRepository(Long portfolioProjectId, Long sourceRepositoryId, LinkType linkType) {
        return optimisticLockingService.executeWithRetry(() -> {
            Optional<PortfolioProjectJpaEntity> portfolioOpt = portfolioProjectRepository.findById(portfolioProjectId);
            if (portfolioOpt.isEmpty()) {
                throw new IllegalArgumentException("Portfolio project not found: " + portfolioProjectId);
            }
            
            Optional<SourceRepositoryJpaEntity> sourceOpt = sourceRepositoryRepository.findById(sourceRepositoryId);
            if (sourceOpt.isEmpty()) {
                throw new IllegalArgumentException("Source repository not found: " + sourceRepositoryId);
            }
            
            PortfolioProjectJpaEntity portfolio = portfolioOpt.get();
            PortfolioProjectJpaEntity beforeUpdate = portfolio.toBuilder().build(); // Deep copy for audit
            
            PortfolioProjectJpaEntity updated = portfolio.toBuilder()
                    .sourceRepositoryId(sourceRepositoryId)
                    .linkType(convertLinkType(linkType))
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            PortfolioProjectJpaEntity saved = portfolioProjectRepository.save(updated);
            
            // Audit the linking operation
            auditTrailService.auditUpdate("PortfolioProject", portfolioProjectId, 
                beforeUpdate, saved, "system", "linkToSourceRepository");
            
            syncMonitorService.appendLog("INFO", 
                String.format("Linked portfolio project '%s' to source repository ID %d (%s)", 
                             portfolio.getTitle(), sourceRepositoryId, linkType));
            
            return convertToDomain(saved);
        }, "linkToSourceRepository", "PortfolioProject", portfolioProjectId, "system");
    }
    
    /**
     * Unlink portfolio project from source repository
     */
    @Transactional
    public PortfolioProject unlinkFromSourceRepository(Long portfolioProjectId) {
        Optional<PortfolioProjectJpaEntity> portfolioOpt = portfolioProjectRepository.findById(portfolioProjectId);
        if (portfolioOpt.isEmpty()) {
            throw new IllegalArgumentException("Portfolio project not found: " + portfolioProjectId);
        }
        
        PortfolioProjectJpaEntity portfolio = portfolioOpt.get();
        PortfolioProjectJpaEntity updated = portfolio.toBuilder()
                .sourceRepositoryId(null)
                .linkType(null)
                .updatedAt(LocalDateTime.now())
                .build();
        
        portfolioProjectRepository.save(updated);
        
        syncMonitorService.appendLog("INFO", 
            String.format("Unlinked portfolio project '%s' from source repository", portfolio.getTitle()));
        
        return convertToDomain(updated);
    }
    
    /**
     * Get portfolio projects that need curation (linked AUTO but not recently curated)
     */
    public List<PortfolioProjectJpaEntity> getProjectsNeedingCuration() {
        return portfolioProjectRepository.findLinkedProjects().stream()
                .filter(p -> p.getLinkType() == PortfolioProjectJpaEntity.LinkTypeJpa.AUTO)
                .filter(p -> p.getUpdatedAt().isBefore(LocalDateTime.now().minusHours(24))) // Not updated in 24h
                .toList();
    }
    
    private PortfolioProject convertToDomain(PortfolioProjectJpaEntity entity) {
        return PortfolioProject.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .link(entity.getLink())
                .githubRepo(entity.getGithubRepo())
                .createdDate(entity.getCreatedDate())
                .estimatedDurationWeeks(entity.getEstimatedDurationWeeks())
                .status(convertStatus(entity.getStatus()))
                .type(convertType(entity.getType()))
                .mainTechnologies(entity.getMainTechnologies())
                .sourceRepositoryId(entity.getSourceRepositoryId())
                .linkType(convertLinkTypeToDomain(entity.getLinkType()))
                .completionStatus(convertCompletionStatus(entity.getCompletionStatus()))
                .priority(convertPriority(entity.getPriority()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    private ProjectStatus convertStatus(PortfolioProjectJpaEntity.ProjectStatusJpa status) {
        if (status == null) return ProjectStatus.ACTIVE;
        return switch (status) {
            case ACTIVE -> ProjectStatus.ACTIVE;
            case COMPLETED -> ProjectStatus.COMPLETED;
            case ON_HOLD -> ProjectStatus.ON_HOLD;
            case ARCHIVED -> ProjectStatus.ARCHIVED;
        };
    }
    
    private ProjectType convertType(PortfolioProjectJpaEntity.ProjectTypeJpa type) {
        if (type == null) return ProjectType.PERSONAL;
        return switch (type) {
            case PERSONAL -> ProjectType.PERSONAL;
            case PROFESSIONAL -> ProjectType.PROFESSIONAL;
            case OPEN_SOURCE -> ProjectType.OPEN_SOURCE;
            case EDUCATIONAL -> ProjectType.EDUCATIONAL;
            case CLIENT_WORK -> ProjectType.CLIENT_WORK;
        };
    }
    
    private LinkType convertLinkTypeToDomain(PortfolioProjectJpaEntity.LinkTypeJpa linkType) {
        if (linkType == null) return null;
        return switch (linkType) {
            case AUTO -> LinkType.AUTO;
            case MANUAL -> LinkType.MANUAL;
        };
    }
    
    private PortfolioProjectJpaEntity.LinkTypeJpa convertLinkType(LinkType linkType) {
        if (linkType == null) return null;
        return switch (linkType) {
            case LinkType.AUTO -> PortfolioProjectJpaEntity.LinkTypeJpa.AUTO;
            case LinkType.MANUAL -> PortfolioProjectJpaEntity.LinkTypeJpa.MANUAL;
        };
    }
    
    private com.portfolio.core.domain.project.ProjectCompletionStatus convertCompletionStatus(
            PortfolioProjectJpaEntity.ProjectCompletionStatusJpa status) {
        if (status == null) return com.portfolio.core.domain.project.ProjectCompletionStatus.BACKLOG;
        return switch (status) {
            case BACKLOG -> com.portfolio.core.domain.project.ProjectCompletionStatus.BACKLOG;
            case IN_PROGRESS -> com.portfolio.core.domain.project.ProjectCompletionStatus.IN_PROGRESS;
            case LIVE -> com.portfolio.core.domain.project.ProjectCompletionStatus.LIVE;
            case ARCHIVED -> com.portfolio.core.domain.project.ProjectCompletionStatus.ARCHIVED;
        };
    }
    
    private com.portfolio.core.domain.project.ProjectPriority convertPriority(
            PortfolioProjectJpaEntity.ProjectPriorityJpa priority) {
        if (priority == null) return null;
        return switch (priority) {
            case LOW -> com.portfolio.core.domain.project.ProjectPriority.LOW;
            case MEDIUM -> com.portfolio.core.domain.project.ProjectPriority.MEDIUM;
            case HIGH -> com.portfolio.core.domain.project.ProjectPriority.HIGH;
        };
    }
}
