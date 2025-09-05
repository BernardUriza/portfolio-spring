package com.portfolio.service;

import com.portfolio.dto.GitHubRepositoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubApiService {
    
    private static final String GITHUB_API_BASE_URL = "https://api.github.com";
    private static final int DEFAULT_PER_PAGE = 100;
    private final RestTemplate restTemplate;
    
    @Value("${github.username}")
    private String githubUsername;
    
    @Value("${github.token:#{null}}")
    private String githubToken;
    
    public List<GitHubRepositoryDto> getStarredRepositories() {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(GITHUB_API_BASE_URL)
                    .pathSegment("users", githubUsername, "starred")
                    .queryParam("per_page", DEFAULT_PER_PAGE)
                    .queryParam("sort", "created")
                    .queryParam("direction", "desc")
                    .toUriString();
            
            HttpHeaders headers = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            log.info("Fetching starred repositories for user: {}", githubUsername);
            
            ResponseEntity<List<GitHubRepositoryDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<GitHubRepositoryDto>>() {}
            );
            
            List<GitHubRepositoryDto> repositories = response.getBody();
            log.info("Successfully fetched {} starred repositories", 
                    repositories != null ? repositories.size() : 0);
            
            return repositories != null ? repositories : Collections.emptyList();
            
        } catch (HttpClientErrorException.TooManyRequests e) {
            log.warn("GitHub API rate limit exceeded. Headers: {}", e.getResponseHeaders());
            throw new RuntimeException("GitHub API rate limit exceeded", e);
            
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("GitHub API authentication failed. Please check your token.");
            throw new RuntimeException("GitHub API authentication failed", e);
            
        } catch (HttpClientErrorException e) {
            log.error("GitHub API request failed with status: {} - {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("GitHub API request failed", e);
            
        } catch (Exception e) {
            log.error("Unexpected error occurred while fetching starred repositories", e);
            throw new RuntimeException("Failed to fetch starred repositories", e);
        }
    }
    
    public GitHubRateLimit getRateLimit() {
        try {
            String url = GITHUB_API_BASE_URL + "/rate_limit";
            HttpHeaders headers = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<GitHubRateLimit> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitHubRateLimit.class
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            log.warn("Failed to fetch GitHub rate limit info", e);
            return null;
        }
    }
    
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("User-Agent", "Portfolio-Backend/1.0");
        
        if (githubToken != null && !githubToken.trim().isEmpty()) {
            headers.setBearerAuth(githubToken);
        }
        
        return headers;
    }
    
    public static class GitHubRateLimit {
        public RateInfo rate;
        
        public static class RateInfo {
            public int limit;
            public int remaining;
            public long reset;
        }
    }
}