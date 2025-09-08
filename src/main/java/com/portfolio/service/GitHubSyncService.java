package com.portfolio.service;

import com.portfolio.adapter.out.persistence.jpa.ProjectJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.ProjectJpaRepository;
import com.portfolio.adapter.out.persistence.jpa.StarredProjectJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.StarredProjectJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubSyncService {
    
    private final ProjectJpaRepository projectRepository;
    private final StarredProjectJpaRepository starredProjectRepository;
    private final SyncMonitorService syncMonitorService;
    private final WebClient.Builder webClientBuilder;
    private final StarredProjectService starredProjectService;
    
    @Value("${github.api.token:}")
    private String githubToken;
    
    @Value("${github.username:BernardUriza}")
    private String githubUsername;
    
    
    public void syncStarredProjects() {
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
            
            // Step 2: Update StarredProject entities
            List<StarredProjectJpaEntity> existingStarredProjects = starredProjectRepository.findAll();
            syncMonitorService.appendLog("INFO", "Found " + existingStarredProjects.size() + " starred projects in database");
            
            Map<Long, StarredProjectJpaEntity> existingStarredMap = existingStarredProjects.stream()
                .collect(Collectors.toMap(StarredProjectJpaEntity::getGithubId, p -> p));
            
            List<SyncMonitorService.UnsyncedProject> unsyncedProjects = new ArrayList<>();
            int syncedCount = 0;
            int skippedCount = 0;
            
            for (GitHubRepo repo : starredRepos) {
                try {
                    StarredProjectJpaEntity starredProject;
                    
                    if (existingStarredMap.containsKey(repo.id)) {
                        starredProject = existingStarredMap.get(repo.id);
                        updateExistingStarredProject(starredProject, repo);
                        syncMonitorService.appendLog("DEBUG", "Updated starred project: " + repo.name);
                    } else {
                        starredProject = createNewStarredProject(repo);
                        syncMonitorService.appendLog("INFO", "Created new starred project: " + repo.name);
                    }
                    
                    // Fetch README if not already fetched or if project updated significantly
                    if (shouldFetchReadme(starredProject, repo)) {
                        fetchAndStoreReadme(starredProject, repo);
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
            
            // Process unsynced projects with Claude API
            try {
                syncMonitorService.appendLog("INFO", "Starting Claude analysis of unsynced projects");
                starredProjectService.processUnsyncedProjects();
                syncMonitorService.appendLog("INFO", "Claude analysis completed");
            } catch (Exception e) {
                log.error("Error during Claude processing", e);
                syncMonitorService.appendLog("ERROR", "Claude processing failed: " + e.getMessage());
            }
            
            syncMonitorService.markSyncCompleted(
                starredRepos.size(),
                (int) starredProjectRepository.count(),
                unsyncedProjects
            );
            
        } catch (Exception e) {
            log.error("GitHub sync failed", e);
            syncMonitorService.markSyncFailed(e.getMessage());
        }
    }
    
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
            
            GitHubRepo[] repos = response.timeout(java.time.Duration.ofSeconds(30))
                                        .block();
            
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
    
    private void updateExistingStarredProject(StarredProjectJpaEntity existing, GitHubRepo repo) {
        existing.setName(repo.name);
        existing.setFullName(repo.full_name);
        existing.setDescription(repo.description);
        existing.setGithubRepoUrl(repo.html_url); // Fix: ensure URL is always updated from GitHub
        existing.setHomepage(repo.homepage);
        existing.setLanguage(repo.language);
        existing.setFork(repo.fork);
        existing.setStargazersCount(repo.stargazers_count);
        existing.setTopics(repo.topics != null ? new ArrayList<>(repo.topics) : new ArrayList<>());
        existing.setGithubUpdatedAt(repo.updated_at);
        existing.setUpdatedAt(LocalDateTime.now());
        
        starredProjectRepository.save(existing);
    }
    
    private StarredProjectJpaEntity createNewStarredProject(GitHubRepo repo) {
        StarredProjectJpaEntity starredProject = StarredProjectJpaEntity.builder()
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
            .syncStatus(StarredProjectJpaEntity.SyncStatus.UNSYNCED)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        return starredProjectRepository.save(starredProject);
    }
    
    private boolean shouldFetchReadme(StarredProjectJpaEntity starredProject, GitHubRepo repo) {
        // Fetch README if:
        // 1. Never fetched before (readmeMarkdown is null)
        // 2. Repository was updated on GitHub after our last sync
        return starredProject.getReadmeMarkdown() == null || 
               (repo.updated_at != null && 
                !repo.updated_at.equals(starredProject.getGithubUpdatedAt()));
    }
    
    private void fetchAndStoreReadme(StarredProjectJpaEntity starredProject, GitHubRepo repo) {
        try {
            syncMonitorService.appendLog("DEBUG", "Fetching README for: " + repo.name + " (fullName: " + repo.full_name + ")");
            
            String readmeContent = fetchRepositoryReadme(repo.full_name);
            if (readmeContent != null && !readmeContent.trim().isEmpty()) {
                starredProject.setReadmeMarkdown(readmeContent);
                starredProjectRepository.save(starredProject);
                syncMonitorService.appendLog("DEBUG", "README successfully fetched for: " + repo.name);
            } else {
                // Clear any existing README content if it's no longer available
                starredProject.setReadmeMarkdown(null);
                starredProjectRepository.save(starredProject);
                syncMonitorService.appendLog("DEBUG", "No README available for: " + repo.name + " - marked as unavailable");
            }
        } catch (Exception e) {
            log.warn("Failed to fetch README for {} ({}): {}", repo.name, repo.full_name, e.getMessage());
            syncMonitorService.appendLog("WARN", "Failed to fetch README for " + repo.name + ": " + e.getMessage());
            
            // Ensure README is marked as unavailable on fetch failure
            starredProject.setReadmeMarkdown(null);
            starredProjectRepository.save(starredProject);
        }
    }
    
    /**
     * Fetch README content from GitHub API using repository full name.
     * 
     * IMPORTANT: Uses GitHub API endpoint, NOT raw.githubusercontent.com
     * 
     * @param fullName Repository full name in format "owner/repo" (e.g., "BernardUriza/portfolio-backend")
     * @return README markdown content or null if not available
     * 
     * Examples:
     * ✅ Correct usage: fetchRepositoryReadme("BernardUriza/portfolio-backend")
     * ❌ Incorrect: Using html_url or constructing raw URLs like:
     *   "https://raw.githubusercontent.com/https://github.com/owner/repo/master/README.md"
     */
    private String fetchRepositoryReadme(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            log.warn("Invalid repository full name provided for README fetch");
            return null;
        }
        
        // Ensure fullName is in correct format (owner/repo)
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
                    // Decode base64 content with better error handling
                    String cleanedContent = readmeResponse.content.replaceAll("\\s", "");
                    byte[] decodedBytes = Base64.getDecoder().decode(cleanedContent);
                    String decodedContent = new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8);
                    
                    // Limit README size to prevent memory issues
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
        
        /**
         * Validates that the full_name is in the correct format for GitHub API calls.
         * @return true if full_name is in "owner/repo" format
         */
        public boolean hasValidFullName() {
            return full_name != null && full_name.matches("^[^/]+/[^/]+$");
        }
        
        /**
         * Gets the repository owner from full_name.
         * @return owner name or null if invalid format
         */
        public String getOwner() {
            if (!hasValidFullName()) return null;
            return full_name.split("/")[0];
        }
        
        /**
         * Gets the repository name from full_name.
         * @return repository name or null if invalid format
         */
        public String getRepoName() {
            if (!hasValidFullName()) return null;
            return full_name.split("/")[1];
        }
    }
    
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