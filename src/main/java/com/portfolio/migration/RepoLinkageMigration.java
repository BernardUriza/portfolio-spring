package com.portfolio.migration;

import com.portfolio.model.Project;
import com.portfolio.core.domain.project.ProjectCompletionStatus;
import com.portfolio.core.domain.project.ProjectPriority;
import com.portfolio.core.domain.project.FieldProtection;
import com.portfolio.core.domain.project.ProjectCompleteness;
import com.portfolio.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Migration component for backfilling repository linkage data
 * 
 * Usage: Run with --spring.profiles.active=migration
 * Or call migratRepositoryLinkage() method manually
 * 
 * This migration:
 * 1. Links existing projects with GitHub repositories based on name patterns
 * 2. Sets default field protection values
 * 3. Calculates initial completion percentages
 * 4. Assigns completion status and priority based on existing data
 */
@Component
@Profile("migration") // Only run when migration profile is active
public class RepoLinkageMigration implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(RepoLinkageMigration.class);
    
    private final ProjectRepository projectRepository;
    private final String githubUsername;
    
    public RepoLinkageMigration(
            ProjectRepository projectRepository,
            @Value("${github.username:}") String githubUsername) {
        this.projectRepository = projectRepository;
        this.githubUsername = githubUsername;
    }
    
    @Override
    public void run(String... args) {
        logger.info("Starting Repository Linkage Migration...");
        migrateRepositoryLinkage();
        logger.info("Repository Linkage Migration completed.");
    }
    
    @Transactional
    public void migrateRepositoryLinkage() {
        List<Project> allProjects = projectRepository.findAll();
        logger.info("Found {} projects to migrate", allProjects.size());
        
        AtomicInteger updatedCount = new AtomicInteger(0);
        AtomicInteger linkedCount = new AtomicInteger(0);
        AtomicInteger protectedCount = new AtomicInteger(0);
        
        allProjects.forEach(project -> {
            boolean updated = false;
            
            // Step 1: Link GitHub repository if not already linked
            if (project.getGithubUrl() == null || project.getGithubUrl().trim().isEmpty()) {
                String githubUrl = generateGithubUrl(project.getName());
                if (githubUrl != null) {
                    project.setGithubUrl(githubUrl);
                    linkedCount.incrementAndGet();
                    updated = true;
                    logger.debug("Linked project '{}' to repository: {}", project.getName(), githubUrl);
                }
            }
            
            // Step 2: Set default field protection if not set
            if (project.getFieldProtection() == null) {
                // Default: no fields protected (allow sync to update)
                FieldProtection defaultProtection = FieldProtection.builder()
                        .description(false)
                        .liveDemoUrl(false)
                        .build();
                project.setFieldProtection(defaultProtection);
                updated = true;
            } else {
                // Check if any fields are protected
                if (project.getFieldProtection().getDescription() || 
                    project.getFieldProtection().getLiveDemoUrl()) {
                    protectedCount.incrementAndGet();
                }
            }
            
            // Step 3: Set completion status if not set
            if (project.getCompletionStatus() == null) {
                ProjectCompletionStatus status = determineCompletionStatus(project);
                project.setCompletionStatus(status);
                updated = true;
                logger.debug("Set completion status for '{}': {}", project.getName(), status);
            }
            
            // Step 4: Set priority if not set
            if (project.getPriority() == null) {
                ProjectPriority priority = determinePriority(project);
                project.setPriority(priority);
                updated = true;
                logger.debug("Set priority for '{}': {}", project.getName(), priority);
            }
            
            // Step 5: Calculate completion percentage
            ProjectCompleteness completeness = calculateCompleteness(project);
            project.setCompletionPercentage(completeness.getPercentage());
            updated = true;
            
            // Step 6: Update timestamp
            if (updated) {
                project.setUpdatedAt(LocalDateTime.now());
                updatedCount.incrementAndGet();
            }
        });
        
        // Save all changes
        projectRepository.saveAll(allProjects);
        
        // Log migration statistics
        logMigrationStats(allProjects.size(), updatedCount.get(), linkedCount.get(), protectedCount.get());
    }
    
    private String generateGithubUrl(String projectName) {
        if (projectName == null || projectName.trim().isEmpty() || githubUsername.isEmpty()) {
            return null;
        }
        
        // Convert project name to repository-friendly format
        String repoName = projectName
                .toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s]", "") // Remove special characters
                .replaceAll("\\s+", "-") // Replace spaces with hyphens
                .replaceAll("-+", "-") // Replace multiple hyphens with single
                .replaceAll("^-|-$", ""); // Remove leading/trailing hyphens
        
        // Skip if the name doesn't make sense as a repository name
        if (repoName.length() < 2 || repoName.contains("/") || repoName.contains("@")) {
            return null;
        }
        
        return String.format("https://github.com/%s/%s", githubUsername, repoName);
    }
    
    private ProjectCompletionStatus determineCompletionStatus(Project project) {
        // Projects with live demo are considered LIVE
        if (hasValidLiveDemo(project)) {
            return ProjectCompletionStatus.LIVE;
        }
        
        // Projects with substantial description are IN_PROGRESS
        if (hasSubstantialDescription(project)) {
            return ProjectCompletionStatus.IN_PROGRESS;
        }
        
        // Otherwise, they're in BACKLOG
        return ProjectCompletionStatus.BACKLOG;
    }
    
    private ProjectPriority determinePriority(Project project) {
        // Projects with live demo get HIGH priority
        if (hasValidLiveDemo(project)) {
            return ProjectPriority.HIGH;
        }
        
        // Projects with detailed description get MEDIUM priority
        if (project.getDescription() != null && project.getDescription().trim().length() > 100) {
            return ProjectPriority.MEDIUM;
        }
        
        // Others get LOW priority
        return ProjectPriority.LOW;
    }
    
    private ProjectCompleteness calculateCompleteness(Project project) {
        return ProjectCompleteness.builder()
                .hasDescription(hasSubstantialDescription(project))
                .hasLiveDemo(hasValidLiveDemo(project))
                .hasSkills(true) // Assume all projects have some skills for migration
                .hasExperiences(project.getGithubUrl() != null) // GitHub presence indicates experience
                .build();
    }
    
    private boolean hasValidLiveDemo(Project project) {
        return project.getLiveDemoUrl() != null && 
               project.getLiveDemoUrl().trim().length() > 0 &&
               (project.getLiveDemoUrl().startsWith("http://") || 
                project.getLiveDemoUrl().startsWith("https://"));
    }
    
    private boolean hasSubstantialDescription(Project project) {
        return project.getDescription() != null && 
               project.getDescription().trim().length() >= 50;
    }
    
    private void logMigrationStats(int total, int updated, int linked, int protected) {
        logger.info("=== MIGRATION STATISTICS ===");
        logger.info("Total projects: {}", total);
        logger.info("Projects updated: {}", updated);
        logger.info("Projects linked to GitHub: {}", linked);
        logger.info("Projects with field protection: {}", protected);
        
        // Calculate completion statistics
        List<Project> allProjects = projectRepository.findAll();
        long liveCount = allProjects.stream()
                .filter(p -> ProjectCompletionStatus.LIVE.equals(p.getCompletionStatus()))
                .count();
        long inProgressCount = allProjects.stream()
                .filter(p -> ProjectCompletionStatus.IN_PROGRESS.equals(p.getCompletionStatus()))
                .count();
        long backlogCount = allProjects.stream()
                .filter(p -> ProjectCompletionStatus.BACKLOG.equals(p.getCompletionStatus()))
                .count();
        
        double avgCompletion = allProjects.stream()
                .mapToInt(p -> p.getCompletionPercentage() != null ? p.getCompletionPercentage() : 0)
                .average()
                .orElse(0.0);
        
        logger.info("Live projects: {}", liveCount);
        logger.info("In-progress projects: {}", inProgressCount);
        logger.info("Backlog projects: {}", backlogCount);
        logger.info("Average completion: {:.1f}%", avgCompletion);
        logger.info("===========================");
        
        // Log some examples
        logger.info("Sample migrated projects:");
        allProjects.stream()
                .limit(5)
                .forEach(project -> logger.info("  - {} [{}] ({}% complete) {}", 
                        project.getName(),
                        project.getCompletionStatus(),
                        project.getCompletionPercentage(),
                        project.getGithubUrl() != null ? "GitHub: ✓" : "GitHub: ✗"
                ));
    }
}