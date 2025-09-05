package com.portfolio.controller;

import com.portfolio.model.StarredProject;
import com.portfolio.service.GitHubApiService;
import com.portfolio.service.StarredProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Starred Projects", description = "GitHub starred repositories management")
@CrossOrigin(origins = "*")
public class StarredProjectController {
    
    private final StarredProjectService starredProjectService;
    private final GitHubApiService gitHubApiService;
    
    @GetMapping("/starred")
    @Operation(summary = "Get all starred repositories", description = "Retrieve all starred GitHub repositories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved starred repositories"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<StarredProject>> getStarredProjects() {
        try {
            List<StarredProject> projects = starredProjectService.getAllStarredProjects();
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            log.error("Error fetching starred projects", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/starred/language/{language}")
    @Operation(summary = "Get starred repositories by language", 
               description = "Retrieve starred repositories filtered by programming language")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved filtered repositories"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<StarredProject>> getStarredProjectsByLanguage(
            @Parameter(description = "Programming language to filter by", example = "Java")
            @PathVariable String language) {
        try {
            List<StarredProject> projects = starredProjectService.getStarredProjectsByLanguage(language);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            log.error("Error fetching starred projects by language: {}", language, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/starred/{id}")
    @Operation(summary = "Get starred repository by ID", 
               description = "Retrieve a specific starred repository by its database ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the repository"),
            @ApiResponse(responseCode = "404", description = "Repository not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<StarredProject> getStarredProjectById(
            @Parameter(description = "Database ID of the starred project", example = "1")
            @PathVariable Long id) {
        try {
            Optional<StarredProject> project = starredProjectService.getStarredProjectById(id);
            return project.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching starred project by ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/starred/languages")
    @Operation(summary = "Get available programming languages", 
               description = "Retrieve list of all programming languages used in starred repositories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved languages"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<String>> getAvailableLanguages() {
        try {
            List<String> languages = starredProjectService.getAvailableLanguages();
            return ResponseEntity.ok(languages);
        } catch (Exception e) {
            log.error("Error fetching available languages", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/starred/stats")
    @Operation(summary = "Get starred projects statistics", 
               description = "Retrieve statistics about starred repositories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getStarredProjectsStats() {
        try {
            long totalCount = starredProjectService.getStarredProjectsCount();
            long recentCount = starredProjectService.getRecentlyStarredCount(30);
            List<String> languages = starredProjectService.getAvailableLanguages();
            
            Map<String, Object> stats = Map.of(
                    "totalStarredProjects", totalCount,
                    "recentlyStarred30Days", recentCount,
                    "uniqueLanguages", languages.size(),
                    "availableLanguages", languages
            );
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching starred projects statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/starred/sync")
    @Operation(summary = "Manually trigger sync", 
               description = "Manually trigger synchronization with GitHub starred repositories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sync completed successfully"),
            @ApiResponse(responseCode = "500", description = "Sync failed")
    })
    public ResponseEntity<Map<String, String>> manualSync() {
        try {
            starredProjectService.manualSync();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Sync completed successfully"
            ));
        } catch (Exception e) {
            log.error("Error during manual sync", e);
            return ResponseEntity.internalServerError()
                                 .body(Map.of(
                                         "status", "error",
                                         "message", "Sync failed: " + e.getMessage()
                                 ));
        }
    }
    
    @GetMapping("/starred/rate-limit")
    @Operation(summary = "Get GitHub API rate limit info", 
               description = "Retrieve current GitHub API rate limit status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved rate limit info"),
            @ApiResponse(responseCode = "500", description = "Failed to retrieve rate limit info")
    })
    public ResponseEntity<GitHubApiService.GitHubRateLimit> getRateLimit() {
        try {
            GitHubApiService.GitHubRateLimit rateLimit = gitHubApiService.getRateLimit();
            if (rateLimit != null) {
                return ResponseEntity.ok(rateLimit);
            } else {
                return ResponseEntity.internalServerError().build();
            }
        } catch (Exception e) {
            log.error("Error fetching rate limit info", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}