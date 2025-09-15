package com.portfolio.service;

import com.portfolio.adapter.out.persistence.jpa.SourceRepositoryJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.SourceRepositoryJpaRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubSourceRepositoryService {
    
    private final SourceRepositoryJpaRepository sourceRepositoryRepository;
    private final SyncMonitorService syncMonitorService;
    private final WebClient.Builder webClientBuilder;
    
    @Value("${github.api.token:}")
    private String githubToken;
    
    @Value("${github.username:BernardUriza}")
    private String githubUsername;
    
    
    public void syncStarredRepositories() {
        if (syncMonitorService.isSyncInProgress()) {
            syncMonitorService.appendLog("WARN", "Sync already in progress, skipping");
            return;
        }
        
        if (githubUsername == null || githubUsername.trim().isEmpty()) {
            syncMonitorService.appendLog("ERROR", "GitHub username not configured - cannot sync");
            return;
        }
        
        syncMonitorService.markSyncStarted();
        
        try {
            syncMonitorService.appendLog("INFO", "Starting GitHub sync for user: " + githubUsername);
            
            // Step 1: Fetch starred repositories from GitHub
            List<GitHubRepo> starredRepos = fetchStarredRepositories();
            if (starredRepos.isEmpty()) {
                syncMonitorService.appendLog("WARN", "No starred repositories found for user: " + githubUsername);
                syncMonitorService.markSyncCompleted(0, 0, Collections.emptyList());
                return;
            }
            
            syncMonitorService.appendLog("INFO", "Fetched " + starredRepos.size() + " starred repositories from GitHub");
            
            // Step 2: Update SourceRepository entities
            List<SourceRepositoryJpaEntity> existingRepositories = sourceRepositoryRepository.findAll();
            syncMonitorService.appendLog("INFO", "Found " + existingRepositories.size() + " source repositories in database");
            
            Map<Long, SourceRepositoryJpaEntity> existingRepoMap = existingRepositories.stream()
                .collect(Collectors.toMap(SourceRepositoryJpaEntity::getGithubId, r -> r));
            
            List<SyncMonitorService.UnsyncedProject> unsyncedProjects = new ArrayList<>();
            int syncedCount = 0;
            int skippedCount = 0;
            
            for (GitHubRepo repo : starredRepos) {
                try {
                    SourceRepositoryJpaEntity sourceRepository;
                    
                    if (existingRepoMap.containsKey(repo.id)) {
                        sourceRepository = existingRepoMap.get(repo.id);
                        updateExistingSourceRepository(sourceRepository, repo);
                        syncMonitorService.appendLog("DEBUG", "Updated source repository: " + repo.name);
                    } else {
                        sourceRepository = createNewSourceRepository(repo);
                        syncMonitorService.appendLog("INFO", "Created new source repository: " + repo.name);
                    }
                    
                    // Fetch README if not already fetched or if repository updated significantly
                    if (shouldFetchReadme(sourceRepository, repo)) {
                        fetchAndStoreReadme(sourceRepository, repo);
                    }
                    
                    syncedCount++;
                } catch (Exception e) {
                    log.error("Error syncing repo: " + repo.name, e);
                    unsyncedProjects.add(new SyncMonitorService.UnsyncedProject(
                        repo.id.toString(),
                        repo.name,
                        e.getMessage()
                    ));
                    skippedCount++;
                }
            }
            
            syncMonitorService.appendLog("INFO", 
                String.format("Sync completed: %d synced, %d skipped", syncedCount, skippedCount));
            
            syncMonitorService.markSyncCompleted(
                starredRepos.size(),
                (int) sourceRepositoryRepository.count(),
                unsyncedProjects
            );
            
        } catch (Exception e) {
            log.error("GitHub sync failed", e);
            syncMonitorService.markSyncFailed(e.getMessage());
        }
    }
    
    /**
     * Refresh single repository data from GitHub
     */
    public void refreshSingleRepository(String githubRepoUrl) {
        if (githubRepoUrl == null || githubRepoUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("GitHub repository URL is required");
        }
        
        // Extract owner/repo from URL
        String fullName = extractFullNameFromUrl(githubRepoUrl);
        if (fullName == null) {
            throw new IllegalArgumentException("Invalid GitHub repository URL format: " + githubRepoUrl);
        }
        
        syncMonitorService.appendLog("INFO", "Refreshing single repository: " + fullName);
        
        try {
            // Fetch repository data from GitHub API
            GitHubRepo repo = fetchSingleRepository(fullName);
            if (repo == null) {
                throw new RuntimeException("Repository not found or not accessible: " + fullName);
            }
            
            // Find existing source repository
            Optional<SourceRepositoryJpaEntity> existingOpt = sourceRepositoryRepository.findByGithubRepoUrl(githubRepoUrl);
            
            if (existingOpt.isPresent()) {
                SourceRepositoryJpaEntity existing = existingOpt.get();
                updateExistingSourceRepository(existing, repo);
                
                // Fetch updated README
                fetchAndStoreReadme(existing, repo);
                
                syncMonitorService.appendLog("INFO", "Successfully refreshed repository: " + repo.name);
            } else {
                syncMonitorService.appendLog("WARN", "Repository not found in database, cannot refresh: " + fullName);
                throw new IllegalArgumentException("Repository not found in database: " + fullName);
            }
            
        } catch (Exception e) {
            log.error("Failed to refresh repository {}: {}", fullName, e.getMessage(), e);
            syncMonitorService.appendLog("ERROR", "Failed to refresh repository " + fullName + ": " + e.getMessage());
            throw new RuntimeException("Failed to refresh repository: " + e.getMessage(), e);
        }
    }
    
    private String extractFullNameFromUrl(String githubRepoUrl) {
        try {
            // Handle URLs like https://github.com/owner/repo or https://github.com/owner/repo.git
            String cleanUrl = githubRepoUrl.trim();
            if (cleanUrl.endsWith(".git")) {
                cleanUrl = cleanUrl.substring(0, cleanUrl.length() - 4);
            }
            
            if (cleanUrl.startsWith("https://github.com/")) {
                String path = cleanUrl.substring("https://github.com/".length());
                if (path.matches("^[^/]+/[^/]+$")) {
                    return path;
                }
            }
            
            return null;
        } catch (Exception e) {
            log.warn("Failed to extract full name from URL: {}", githubRepoUrl, e);
            return null;
        }
    }
    
    @Retry(name = "github", fallbackMethod = "fetchSingleRepositoryFallback")
    @CircuitBreaker(name = "github", fallbackMethod = "fetchSingleRepositoryFallback")
    @RateLimiter(name = "github")
    @TimeLimiter(name = "github")
    private GitHubRepo fetchSingleRepository(String fullName) {
        WebClient webClient = createWebClient();
        String uri = "/repos/" + fullName;
        
        try {
            Mono<GitHubRepo> response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(GitHubRepo.class);
            
            GitHubRepo repo = response.block();
            
            if (repo != null && repo.id != null && repo.name != null && repo.hasValidFullName()) {
                return repo;
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("Failed to fetch single repository {}: {}", fullName, e.getMessage());
            throw new RuntimeException("GitHub API request failed: " + e.getMessage(), e);
        }
    }
    
    @Retry(name = "github", fallbackMethod = "fetchStarredRepositoriesFallback")
    @CircuitBreaker(name = "github", fallbackMethod = "fetchStarredRepositoriesFallback")
    @RateLimiter(name = "github")
    @TimeLimiter(name = "github")
    private List<GitHubRepo> fetchStarredRepositories() {
        if (githubUsername == null || githubUsername.trim().isEmpty()) {
            log.warn("GitHub username is null or empty, cannot fetch starred repositories");
            return Collections.emptyList();
        }
        
        WebClient webClient = createWebClient();
        String uri = "/users/" + githubUsername.trim() + "/starred?per_page=100";
        
        try {
            Mono<GitHubRepo[]> response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(GitHubRepo[].class);
            
            GitHubRepo[] repos = response.block();
            
            if (repos == null) {
                log.warn("Received null response from GitHub API for user: {}", githubUsername);
                return Collections.emptyList();
            }
            
            // Filter out repos with missing essential data
            List<GitHubRepo> validRepos = Arrays.stream(repos)
                         .filter(repo -> {
                             if (repo.id == null) {
                                 log.debug("Skipping repo with null ID");
                                 return false;
                             }
                             if (repo.name == null || repo.name.trim().isEmpty()) {
                                 log.debug("Skipping repo with empty name (ID: {})", repo.id);
                                 return false;
                             }
                             if (!repo.hasValidFullName()) {
                                 log.warn("Skipping repo '{}' with invalid full_name format: '{}'", 
                                         repo.name, repo.full_name);
                                 return false;
                             }
                             return true;
                         })
                         .collect(Collectors.toList());
                         
            log.debug("Filtered {} valid repositories out of {} total", validRepos.size(), repos.length);
            return validRepos;
                         
        } catch (Exception e) {
            log.error("Failed to fetch starred repositories for user: {}, Error: {}", githubUsername, e.getMessage());
            throw new RuntimeException("GitHub API request failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Fallback method for GitHub API calls
     */
    @SuppressWarnings("unused")
    private List<GitHubRepo> fetchStarredRepositoriesFallback(Exception ex) {
        log.warn("GitHub API fallback triggered due to: {}", ex.getMessage());
        syncMonitorService.appendLog("WARN", "GitHub API unavailable, using empty result: " + ex.getMessage());
        return Collections.emptyList();
    }
    
    /**
     * Fallback method for single repository fetch
     */
    @SuppressWarnings("unused")
    private GitHubRepo fetchSingleRepositoryFallback(String fullName, Exception ex) {
        log.warn("GitHub API fallback triggered for repository {}: {}", fullName, ex.getMessage());
        syncMonitorService.appendLog("WARN", "GitHub API unavailable for repository " + fullName + ": " + ex.getMessage());
        return null;
    }
    
    private void updateExistingSourceRepository(SourceRepositoryJpaEntity existing, GitHubRepo repo) {
        existing.setName(repo.name);
        existing.setFullName(repo.full_name);
        existing.setDescription(repo.description);
        existing.setGithubRepoUrl(repo.html_url);
        existing.setHomepage(repo.homepage);
        existing.setLanguage(repo.language);
        existing.setFork(repo.fork);
        existing.setStargazersCount(repo.stargazers_count);
        existing.setTopics(repo.topics != null ? new ArrayList<>(repo.topics) : new ArrayList<>());
        existing.setGithubUpdatedAt(repo.updated_at);
        existing.setUpdatedAt(LocalDateTime.now());
        
        sourceRepositoryRepository.save(existing);
    }
    
    private SourceRepositoryJpaEntity createNewSourceRepository(GitHubRepo repo) {
        SourceRepositoryJpaEntity sourceRepository = SourceRepositoryJpaEntity.builder()
            .githubId(repo.id)
            .name(repo.name)
            .fullName(repo.full_name)
            .description(repo.description)
            .githubRepoUrl(repo.html_url)
            .homepage(repo.homepage)
            .language(repo.language)
            .fork(repo.fork)
            .stargazersCount(repo.stargazers_count)
            .topics(repo.topics != null ? new ArrayList<>(repo.topics) : new ArrayList<>())
            .githubCreatedAt(repo.created_at)
            .githubUpdatedAt(repo.updated_at)
            .syncStatus(SourceRepositoryJpaEntity.SyncStatus.UNSYNCED)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        return sourceRepositoryRepository.save(sourceRepository);
    }
    
    private boolean shouldFetchReadme(SourceRepositoryJpaEntity sourceRepository, GitHubRepo repo) {
        return sourceRepository.getReadmeMarkdown() == null || 
               (repo.updated_at != null && 
                !repo.updated_at.equals(sourceRepository.getGithubUpdatedAt()));
    }
    
    private void fetchAndStoreReadme(SourceRepositoryJpaEntity sourceRepository, GitHubRepo repo) {
        try {
            syncMonitorService.appendLog("DEBUG", "Fetching README for: " + repo.name + " (fullName: " + repo.full_name + ")");
            
            String readmeContent = fetchRepositoryReadme(repo.full_name);
            if (readmeContent != null && !readmeContent.trim().isEmpty()) {
                sourceRepository.setReadmeMarkdown(readmeContent);
                sourceRepositoryRepository.save(sourceRepository);
                syncMonitorService.appendLog("DEBUG", "README successfully fetched for: " + repo.name);
            } else {
                sourceRepository.setReadmeMarkdown(null);
                sourceRepositoryRepository.save(sourceRepository);
                syncMonitorService.appendLog("DEBUG", "No README available for: " + repo.name + " - marked as unavailable");
            }
        } catch (Exception e) {
            log.warn("Failed to fetch README for {} ({}): {}", repo.name, repo.full_name, e.getMessage());
            syncMonitorService.appendLog("WARN", "Failed to fetch README for " + repo.name + ": " + e.getMessage());
            
            sourceRepository.setReadmeMarkdown(null);
            sourceRepositoryRepository.save(sourceRepository);
        }
    }
    
    private String fetchRepositoryReadme(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            log.warn("Invalid repository full name provided for README fetch");
            return null;
        }
        
        String cleanFullName = fullName.trim();
        if (!cleanFullName.matches("^[^/]+/[^/]+$")) {
            log.warn("Invalid fullName format: '{}'. Expected format: 'owner/repo'", cleanFullName);
            return null;
        }
        
        WebClient webClient = createWebClient();
        String uri = "/repos/" + cleanFullName + "/readme";
        
        try {
            log.debug("Fetching README from GitHub API: {}", uri);
            
            Mono<GitHubReadmeResponse> response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(GitHubReadmeResponse.class);
            
            GitHubReadmeResponse readmeResponse = response.timeout(java.time.Duration.ofSeconds(15))
                                                          .block();
                                                          
            if (readmeResponse != null && readmeResponse.content != null && !readmeResponse.content.trim().isEmpty()) {
                try {
                    String cleanedContent = readmeResponse.content.replaceAll("\\s", "");
                    byte[] decodedBytes = Base64.getDecoder().decode(cleanedContent);
                    String decodedContent = new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8);
                    
                    if (decodedContent.length() > 50000) {
                        log.debug("Truncating large README for repository: {} (size: {})", cleanFullName, decodedContent.length());
                        return decodedContent.substring(0, 50000) + "\n\n... [README truncated due to size]";
                    }
                    
                    log.debug("Successfully fetched and decoded README for: {}", cleanFullName);
                    return decodedContent;
                } catch (IllegalArgumentException e) {
                    log.warn("Failed to decode base64 README content for {}: {}", cleanFullName, e.getMessage());
                }
            } else {
                log.debug("Empty or null README response for: {}", cleanFullName);
            }
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                log.debug("README not found for repository: {} (404)", cleanFullName);
            } else {
                log.warn("HTTP error {} fetching README for {}: {}", e.getStatusCode().value(), cleanFullName, e.getMessage());
            }
        } catch (Exception e) {
            log.debug("Could not fetch README for {}: {}", cleanFullName, e.getMessage());
        }
        
        return null;
    }
    
    private WebClient createWebClient() {
        WebClient.Builder builder = webClientBuilder
            .baseUrl("https://api.github.com")
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.USER_AGENT, "Portfolio-Application");
        
        if (githubToken != null && !githubToken.isEmpty()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + githubToken);
        }
        
        return builder.build();
    }
    
    // GitHub API response models
    @SuppressWarnings("unused")
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GitHubRepo {
        public Long id;
        public String name;
        public String full_name;
        public String description;
        public String html_url;
        public String homepage;
        public String language;
        public Boolean fork;
        public Integer stargazers_count;
        public List<String> topics;
        public String created_at;
        public String updated_at;
        
        public boolean hasValidFullName() {
            return full_name != null && full_name.matches("^[^/]+/[^/]+$");
        }
        
        public String getOwner() {
            if (!hasValidFullName()) return null;
            return full_name.split("/")[0];
        }
        
        public String getRepoName() {
            if (!hasValidFullName()) return null;
            return full_name.split("/")[1];
        }
    }
    
    @SuppressWarnings("unused")
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GitHubReadmeResponse {
        public String name;
        public String path;
        public String sha;
        public Integer size;
        public String url;
        public String html_url;
        public String git_url;
        public String download_url;
        public String type;
        public String content;
        public String encoding;
    }
}
