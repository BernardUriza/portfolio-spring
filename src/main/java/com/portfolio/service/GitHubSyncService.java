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
import org.springframework.scheduling.annotation.Scheduled;
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
    
    @Value("${github.api.token:}")
    private String githubToken;
    
    @Value("${github.username:BernardUriza}")
    private String githubUsername;
    
    @Scheduled(fixedDelayString = "${github.sync.interval:300000}") // 5 minutes
    public void scheduledSync() {
        syncStarredProjects();
    }
    
    public void syncStarredProjects() {
        if (syncMonitorService.isSyncInProgress()) {
            syncMonitorService.appendLog("WARN", "Sync already in progress, skipping");
            return;
        }
        
        syncMonitorService.markSyncStarted();
        
        try {
            syncMonitorService.appendLog("INFO", "Starting GitHub sync for user: " + githubUsername);
            
            // Step 1: Fetch starred repositories from GitHub
            List<GitHubRepo> starredRepos = fetchStarredRepositories();
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
        WebClient webClient = createWebClient();
        
        String uri = "/users/" + githubUsername + "/starred?per_page=100";
        
        Mono<GitHubRepo[]> response = webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(GitHubRepo[].class);
        
        GitHubRepo[] repos = response.block();
        return repos != null ? Arrays.asList(repos) : Collections.emptyList();
    }
    
    private void updateExistingStarredProject(StarredProjectJpaEntity existing, GitHubRepo repo) {
        existing.setName(repo.name);
        existing.setFullName(repo.full_name);
        existing.setDescription(repo.description);
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
            syncMonitorService.appendLog("DEBUG", "Fetching README for: " + repo.name);
            
            String readmeContent = fetchRepositoryReadme(repo.full_name);
            if (readmeContent != null && !readmeContent.trim().isEmpty()) {
                starredProject.setReadmeMarkdown(readmeContent);
                starredProjectRepository.save(starredProject);
                syncMonitorService.appendLog("DEBUG", "README fetched for: " + repo.name);
            } else {
                syncMonitorService.appendLog("DEBUG", "No README found for: " + repo.name);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch README for {}: {}", repo.name, e.getMessage());
            syncMonitorService.appendLog("WARN", "Failed to fetch README for " + repo.name + ": " + e.getMessage());
        }
    }
    
    private String fetchRepositoryReadme(String fullName) {
        WebClient webClient = createWebClient();
        
        try {
            String uri = "/repos/" + fullName + "/readme";
            
            Mono<GitHubReadmeResponse> response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(GitHubReadmeResponse.class);
            
            GitHubReadmeResponse readmeResponse = response.block();
            if (readmeResponse != null && readmeResponse.content != null) {
                // Decode base64 content
                byte[] decodedBytes = Base64.getDecoder().decode(readmeResponse.content.replace("\n", ""));
                return new String(decodedBytes);
            }
        } catch (Exception e) {
            log.debug("Could not fetch README for {}: {}", fullName, e.getMessage());
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