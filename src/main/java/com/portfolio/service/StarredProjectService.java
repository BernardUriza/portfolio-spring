package com.portfolio.service;

import com.portfolio.dto.GitHubRepositoryDto;
import com.portfolio.model.StarredProject;
import com.portfolio.repository.StarredProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StarredProjectService {
    
    private final StarredProjectRepository repository;
    private final GitHubApiService gitHubApiService;
    private final SemanticTransformationService semanticTransformationService;
    
    @Scheduled(fixedRate = 300000) // 5 minutes = 300000 milliseconds
    @Transactional
    public void syncStarredRepositories() {
        log.info("Starting scheduled sync of starred repositories");
        
        try {
            List<GitHubRepositoryDto> gitHubRepos = gitHubApiService.getStarredRepositories();
            
            if (gitHubRepos.isEmpty()) {
                log.warn("No starred repositories found or API returned empty response");
                return;
            }
            
            int updated = 0;
            int created = 0;
            int semanticallyProcessed = 0;
            
            for (GitHubRepositoryDto gitHubRepo : gitHubRepos) {
                try {
                    StarredProject existingProject = repository.findByGithubId(gitHubRepo.getId())
                            .orElse(null);
                    
                    StarredProject project = mapToEntity(gitHubRepo, existingProject);
                    boolean isNewProject = false;
                    boolean hasProjectChanges = false;
                    
                    if (existingProject == null) {
                        StarredProject savedProject = repository.save(project);
                        created++;
                        isNewProject = true;
                        log.debug("Created new starred project: {}", savedProject.getName());
                        
                        // Add to processing queue with rate limiting
                        try {
                            // Add 2-second delay to prevent API rate limiting
                            Thread.sleep(2000);
                            semanticTransformationService.processStarredProject(savedProject);
                            semanticallyProcessed++;
                            log.debug("Successfully processed semantic transformation for new project: {}", savedProject.getName());
                        } catch (InterruptedException e) {
                            log.warn("Thread interrupted during rate limiting delay");
                            Thread.currentThread().interrupt();
                        } catch (Exception semanticError) {
                            log.error("Failed semantic transformation for new project '{}': {}", 
                                    savedProject.getName(), semanticError.getMessage());
                        }
                        
                    } else if (hasChanges(existingProject, project)) {
                        project.setId(existingProject.getId());
                        project.setCreatedAt(existingProject.getCreatedAt());
                        StarredProject savedProject = repository.save(project);
                        updated++;
                        hasProjectChanges = true;
                        log.debug("Updated starred project: {}", savedProject.getName());
                        
                        // Re-process updated project with Claude if significant changes
                        if (hasSignificantChanges(existingProject, project)) {
                            try {
                                // Add 2-second delay to prevent API rate limiting
                                Thread.sleep(2000);
                                semanticTransformationService.processStarredProject(savedProject);
                                semanticallyProcessed++;
                                log.debug("Reprocessed semantic transformation for updated project: {}", savedProject.getName());
                            } catch (InterruptedException e) {
                                log.warn("Thread interrupted during rate limiting delay");
                                Thread.currentThread().interrupt();
                            } catch (Exception semanticError) {
                                log.error("Failed semantic reprocessing for updated project '{}': {}", 
                                        savedProject.getName(), semanticError.getMessage());
                            }
                        }
                    }
                    
                } catch (Exception e) {
                    log.error("Error processing repository: {} - {}", gitHubRepo.getName(), e.getMessage());
                }
            }
            
            // Optional: Remove projects that are no longer starred
            removeUnstarredProjects(gitHubRepos);
            
            log.info("Sync completed successfully. Created: {}, Updated: {}, Semantically Processed: {}", 
                    created, updated, semanticallyProcessed);
            
        } catch (Exception e) {
            log.error("Failed to sync starred repositories", e);
        }
    }
    
    public List<StarredProject> getAllStarredProjects() {
        return repository.findAllByOrderByStarredAtDesc();
    }
    
    public List<StarredProject> getStarredProjectsByLanguage(String language) {
        if (!StringUtils.hasText(language)) {
            return getAllStarredProjects();
        }
        return repository.findByPrimaryLanguageOrderByStarredAtDesc(language);
    }
    
    public List<String> getAvailableLanguages() {
        return repository.findDistinctPrimaryLanguages();
    }
    
    public Optional<StarredProject> getStarredProjectById(Long id) {
        return repository.findById(id);
    }
    
    public long getStarredProjectsCount() {
        return repository.count();
    }
    
    public long getRecentlyStarredCount(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return repository.countByStarredAtAfter(since);
    }
    
    @Transactional
    public void manualSync() {
        log.info("Manual sync triggered");
        syncStarredRepositories();
    }
    
    private StarredProject mapToEntity(GitHubRepositoryDto dto, StarredProject existing) {
        String homepageUrl = determineHomepageUrl(dto);
        
        StarredProject.StarredProjectBuilder builder = StarredProject.builder()
                .githubId(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .repositoryUrl(dto.getHtmlUrl())
                .homepageUrl(homepageUrl)
                .primaryLanguage(dto.getLanguage())
                .topics(dto.getTopics())
                .starredAt(existing != null ? existing.getStarredAt() : LocalDateTime.now());
        
        if (existing != null) {
            builder.createdAt(existing.getCreatedAt());
        }
        
        return builder.build();
    }
    
    private String determineHomepageUrl(GitHubRepositoryDto dto) {
        if (StringUtils.hasText(dto.getHomepage()) && isValidUrl(dto.getHomepage())) {
            return dto.getHomepage();
        }
        
        // Check for GitHub Pages pattern
        if (dto.getHtmlUrl() != null) {
            String ownerRepo = dto.getHtmlUrl().replace("https://github.com/", "");
            String possibleGitHubPages = "https://" + ownerRepo.split("/")[0] + ".github.io/" + dto.getName();
            
            // In a real implementation, you might want to check if this URL exists
            // For now, we'll just return the repo URL as fallback
        }
        
        return dto.getHtmlUrl(); // Fallback to repository URL
    }
    
    private boolean isValidUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }
    
    private boolean hasChanges(StarredProject existing, StarredProject updated) {
        return !existing.getName().equals(updated.getName()) ||
               !equals(existing.getDescription(), updated.getDescription()) ||
               !equals(existing.getHomepageUrl(), updated.getHomepageUrl()) ||
               !equals(existing.getPrimaryLanguage(), updated.getPrimaryLanguage()) ||
               !existing.getTopics().equals(updated.getTopics());
    }
    
    private boolean hasSignificantChanges(StarredProject existing, StarredProject updated) {
        // Only reprocess with Claude if description, language, or topics change
        // (these affect semantic analysis the most)
        return !equals(existing.getDescription(), updated.getDescription()) ||
               !equals(existing.getPrimaryLanguage(), updated.getPrimaryLanguage()) ||
               !existing.getTopics().equals(updated.getTopics());
    }
    
    private boolean equals(Object a, Object b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }
    
    @Transactional
    private void removeUnstarredProjects(List<GitHubRepositoryDto> currentlyStarred) {
        List<Long> currentGitHubIds = currentlyStarred.stream()
                .map(GitHubRepositoryDto::getId)
                .toList();
        
        List<StarredProject> allProjects = repository.findAll();
        List<StarredProject> toRemove = allProjects.stream()
                .filter(project -> !currentGitHubIds.contains(project.getGithubId()))
                .toList();
        
        if (!toRemove.isEmpty()) {
            repository.deleteAll(toRemove);
            log.info("Removed {} unstarred projects", toRemove.size());
        }
    }
}