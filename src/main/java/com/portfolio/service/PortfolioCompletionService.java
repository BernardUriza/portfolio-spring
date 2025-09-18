package com.portfolio.service;

import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.SourceRepositoryJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.SourceRepositoryJpaRepository;
import com.portfolio.dto.PortfolioCompletionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Creado por Bernard Orozco
 */
@Service
public class PortfolioCompletionService {

    private static final Logger log = LoggerFactory.getLogger(PortfolioCompletionService.class);

    private final SourceRepositoryJpaRepository sourceRepositoryRepository;

    public PortfolioCompletionService(SourceRepositoryJpaRepository sourceRepositoryRepository) {
        this.sourceRepositoryRepository = sourceRepositoryRepository;
    }
    
    @Cacheable(value = "portfolio-completion", key = "#portfolio.id + '_' + #portfolio.updatedAt?.toString()")
    public PortfolioCompletionDto calculateCompletion(PortfolioProjectJpaEntity portfolio) {
        log.debug("Calculating completion for portfolio project: id={}, updatedAt={}", 
                 portfolio.getId(), portfolio.getUpdatedAt());
        
        PortfolioCompletionDto.CompletionScoresDto scores = calculateDetailedScores(portfolio);
        double overallCompleteness = calculateOverallCompleteness(scores);
        
        return PortfolioCompletionDto.builder()
                .id(portfolio.getId())
                .title(portfolio.getTitle())
                .description(portfolio.getDescription())
                .link(portfolio.getLink())
                .githubRepo(portfolio.getGithubRepo())
                .status(portfolio.getStatus() != null ? portfolio.getStatus().name() : null)
                .type(portfolio.getType() != null ? portfolio.getType().name() : null)
                .completionStatus(portfolio.getCompletionStatus() != null ? portfolio.getCompletionStatus().name() : null)
                .priority(portfolio.getPriority() != null ? portfolio.getPriority().name() : null)
                .mainTechnologies(portfolio.getMainTechnologies())
                .sourceRepositoryId(portfolio.getSourceRepositoryId())
                .linkType(portfolio.getLinkType() != null ? portfolio.getLinkType().name() : null)
                .protectDescription(portfolio.getProtectDescription())
                .protectLiveDemoUrl(portfolio.getProtectLiveDemoUrl())
                .protectSkills(portfolio.getProtectSkills())
                .protectExperiences(portfolio.getProtectExperiences())
                .completionScores(scores)
                .overallCompleteness(overallCompleteness)
                .build();
    }
    
    /**
     * Evict cache entry for a specific portfolio project
     */
    @CacheEvict(value = "portfolio-completion", allEntries = false, 
                key = "#portfolioId + '_*'", condition = "#portfolioId != null")
    public void evictCompletionCache(Long portfolioId) {
        log.debug("Evicting completion cache for portfolio project: {}", portfolioId);
    }
    
    /**
     * Evict all completion cache entries
     */
    @CacheEvict(value = "portfolio-completion", allEntries = true)
    public void evictAllCompletionCache() {
        log.info("Evicting all portfolio completion cache entries");
    }
    
    private PortfolioCompletionDto.CompletionScoresDto calculateDetailedScores(PortfolioProjectJpaEntity portfolio) {
        return PortfolioCompletionDto.CompletionScoresDto.builder()
                .basicInfo(calculateBasicInfoScore(portfolio))
                .links(calculateLinksScore(portfolio))
                .metadata(calculateMetadataScore(portfolio))
                .enrichment(calculateEnrichmentScore(portfolio))
                .documentation(calculateDocumentationScore(portfolio))
                .build();
    }
    
    private Double calculateBasicInfoScore(PortfolioProjectJpaEntity portfolio) {
        double score = 0.0;
        
        if (isNotEmpty(portfolio.getTitle())) {
            score += 0.5; // Title is 50% of basic info
        }
        
        if (isNotEmpty(portfolio.getDescription())) {
            score += 0.5; // Description is 50% of basic info
        }
        
        return score;
    }
    
    private Double calculateLinksScore(PortfolioProjectJpaEntity portfolio) {
        double score = 0.0;
        
        if (isNotEmpty(portfolio.getGithubRepo())) {
            score += 0.6; // GitHub repo is 60% of links
        }
        
        if (isNotEmpty(portfolio.getLink())) {
            score += 0.4; // Live demo is 40% of links
        }
        
        return score;
    }
    
    private Double calculateMetadataScore(PortfolioProjectJpaEntity portfolio) {
        int filledFields = 0;
        int totalFields = 4;
        
        if (portfolio.getMainTechnologies() != null && !portfolio.getMainTechnologies().isEmpty()) {
            filledFields++;
        }
        
        if (portfolio.getStatus() != null) {
            filledFields++;
        }
        
        if (portfolio.getType() != null) {
            filledFields++;
        }
        
        if (portfolio.getCompletionStatus() != null) {
            filledFields++;
        }
        
        return (double) filledFields / totalFields;
    }
    
    private Double calculateEnrichmentScore(PortfolioProjectJpaEntity portfolio) {
        // This would be enhanced when Skills/Experiences are linked
        // For now, base it on source repository linking and AI analysis availability
        double score = 0.0;
        
        if (portfolio.getSourceRepositoryId() != null) {
            score += 0.5; // Linked to source repository
            
            // Check if source has rich content for AI analysis
            Optional<SourceRepositoryJpaEntity> sourceOpt = sourceRepositoryRepository.findById(portfolio.getSourceRepositoryId());
            if (sourceOpt.isPresent()) {
                SourceRepositoryJpaEntity source = sourceOpt.get();
                
                if (isNotEmpty(source.getReadmeMarkdown())) {
                    score += 0.3; // Has README for analysis
                }
                
                if (source.getTopics() != null && !source.getTopics().isEmpty()) {
                    score += 0.2; // Has topics for enrichment
                }
            }
        }
        
        return Math.min(score, 1.0);
    }
    
    private Double calculateDocumentationScore(PortfolioProjectJpaEntity portfolio) {
        if (portfolio.getSourceRepositoryId() == null) {
            return 0.0; // No source to analyze
        }
        
        Optional<SourceRepositoryJpaEntity> sourceOpt = sourceRepositoryRepository.findById(portfolio.getSourceRepositoryId());
        if (sourceOpt.isEmpty()) {
            return 0.0;
        }
        
        SourceRepositoryJpaEntity source = sourceOpt.get();
        double score = 0.0;
        
        if (isNotEmpty(source.getDescription())) {
            score += 0.3; // Repository has description
        }
        
        if (isNotEmpty(source.getReadmeMarkdown())) {
            score += 0.5; // Repository has README
            
            // Bonus for substantial README content
            if (source.getReadmeMarkdown().length() > 500) {
                score += 0.1;
            }
        }
        
        if (source.getTopics() != null && !source.getTopics().isEmpty()) {
            score += 0.1; // Repository has topics
        }
        
        return Math.min(score, 1.0);
    }
    
    private double calculateOverallCompleteness(PortfolioCompletionDto.CompletionScoresDto scores) {
        // Weighted average of all completion scores
        double weightedSum = 
            scores.getBasicInfo() * 0.3 +      // 30% - Basic info is crucial
            scores.getLinks() * 0.25 +         // 25% - Links are important
            scores.getMetadata() * 0.2 +       // 20% - Metadata adds context
            scores.getEnrichment() * 0.15 +    // 15% - AI enrichment
            scores.getDocumentation() * 0.1;   // 10% - Documentation quality
        
        return Math.round(weightedSum * 100.0) / 100.0; // Round to 2 decimal places
    }
    
    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
