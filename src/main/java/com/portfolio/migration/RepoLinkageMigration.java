package com.portfolio.migration;

import com.portfolio.adapter.out.persistence.jpa.ProjectJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.ProjectJpaEntity.ProjectCompletionStatusJpa;
import com.portfolio.adapter.out.persistence.jpa.ProjectJpaEntity.ProjectPriorityJpa;
import com.portfolio.adapter.out.persistence.jpa.ProjectJpaRepository;
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
 * Rewritten for hexagonal architecture
 * 
 * Creado por Bernard Orozco
 * 
 * Usage: Run with --spring.profiles.active=migration
 * 
 * This migration:
 * 1. Links existing projects with GitHub repositories based on name patterns
 * 2. Sets default field protection values
 * 3. Calculates initial completion percentages
 * 4. Assigns completion status and priority based on existing data
 */
@Component
@Profile("migration")
public class RepoLinkageMigration implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(RepoLinkageMigration.class);
    
    private final ProjectJpaRepository projectRepository;
    private final String githubUsername;
    
    public RepoLinkageMigration(
            ProjectJpaRepository projectRepository,
            @Value("${github.username:}") String githubUsername) {
        this.projectRepository = projectRepository;
        this.githubUsername = githubUsername;
    }
    
    @Override
    public void run(String... args) {
        logger.info("=== Starting Repository Linkage Migration ===");
        logger.info("GitHub username: {}", githubUsername);
        
        try {
            migrateRepositoryLinkage();
        } catch (Exception e) {
            logger.error("Migration failed", e);
            throw new RuntimeException("Migration failed", e);
        }
    }
    
    @Transactional
    public void migrateRepositoryLinkage() {
        List<ProjectJpaEntity> projects = projectRepository.findAll();
        
        if (projects.isEmpty()) {
            logger.info("No projects found to migrate");
            return;
        }
        
        logger.info("Found {} projects to process", projects.size());
        
        AtomicInteger updatedCount = new AtomicInteger(0);
        AtomicInteger linkedCount = new AtomicInteger(0);
        AtomicInteger protectedCount = new AtomicInteger(0);
        
        projects.forEach(project -> {
            boolean updated = false;
            
            // Link to GitHub repository if not already linked
            if (project.getRepositoryUrl() == null && githubUsername != null && !githubUsername.isEmpty()) {
                String repoName = deriveRepoName(project.getTitle());
                String repoUrl = String.format("https://github.com/%s/%s", githubUsername, repoName);
                project.setRepositoryUrl(repoUrl);
                project.setRepositoryFullName(githubUsername + "/" + repoName);
                linkedCount.incrementAndGet();
                updated = true;
                logger.debug("Linked project '{}' to repository: {}", project.getTitle(), repoUrl);
            }
            
            // Set default field protection if not set
            if (project.getProtectDescription() == null) {
                project.setProtectDescription(false);
                project.setProtectLiveDemoUrl(false);
                project.setProtectSkills(false);
                project.setProtectExperiences(false);
                protectedCount.incrementAndGet();
                updated = true;
            }
            
            // Assign completion status based on project state
            if (project.getCompletionStatus() == null) {
                ProjectCompletionStatusJpa status = determineCompletionStatus(project);
                project.setCompletionStatus(status);
                updated = true;
                logger.debug("Set completion status for '{}': {}", project.getTitle(), status);
            }
            
            // Assign priority based on status
            if (project.getPriority() == null) {
                ProjectPriorityJpa priority = determinePriority(project);
                project.setPriority(priority);
                updated = true;
                logger.debug("Set priority for '{}': {}", project.getTitle(), priority);
            }
            
            // Update timestamp
            if (updated) {
                project.setUpdatedAt(LocalDateTime.now());
                updatedCount.incrementAndGet();
            }
        });
        
        // Save all changes
        projectRepository.saveAll(projects);
        
        logger.info("=== Migration completed successfully ===");
        logMigrationStats(projects.size(), updatedCount.get(), linkedCount.get(), protectedCount.get());
    }
    
    private String deriveRepoName(String projectTitle) {
        // Convert project title to GitHub repo naming convention
        return projectTitle.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
    
    private ProjectCompletionStatusJpa determineCompletionStatus(ProjectJpaEntity project) {
        // Determine completion based on available data
        boolean hasDescription = project.getDescription() != null && 
                               project.getDescription().trim().length() >= 50;
        boolean hasLink = project.getLink() != null && 
                        !project.getLink().trim().isEmpty();
        boolean hasRepo = project.getGithubRepo() != null || 
                        project.getRepositoryUrl() != null;
        
        if (hasLink && hasDescription && hasRepo) {
            return ProjectCompletionStatusJpa.LIVE;
        } else if (hasDescription || hasRepo) {
            return ProjectCompletionStatusJpa.IN_PROGRESS;
        } else {
            return ProjectCompletionStatusJpa.BACKLOG;
        }
    }
    
    private ProjectPriorityJpa determinePriority(ProjectJpaEntity project) {
        // Assign priority based on completion status
        if (ProjectCompletionStatusJpa.LIVE.equals(project.getCompletionStatus())) {
            return ProjectPriorityJpa.HIGH;
        } else if (ProjectCompletionStatusJpa.IN_PROGRESS.equals(project.getCompletionStatus())) {
            return ProjectPriorityJpa.MEDIUM;
        } else {
            return ProjectPriorityJpa.LOW;
        }
    }
    
    private void logMigrationStats(int total, int updated, int linked, int protectedCount) {
        logger.info("=== MIGRATION STATISTICS ===");
        logger.info("Total projects: {}", total);
        logger.info("Projects updated: {}", updated);
        logger.info("Projects linked to GitHub: {}", linked);
        logger.info("Projects with field protection: {}", protectedCount);
        
        // Calculate completion statistics
        List<ProjectJpaEntity> allProjects = projectRepository.findAll();
        long liveCount = allProjects.stream()
                .filter(p -> ProjectCompletionStatusJpa.LIVE.equals(p.getCompletionStatus()))
                .count();
        long inProgressCount = allProjects.stream()
                .filter(p -> ProjectCompletionStatusJpa.IN_PROGRESS.equals(p.getCompletionStatus()))
                .count();
        long backlogCount = allProjects.stream()
                .filter(p -> ProjectCompletionStatusJpa.BACKLOG.equals(p.getCompletionStatus()))
                .count();
        
        logger.info("Live projects: {}", liveCount);
        logger.info("In-progress projects: {}", inProgressCount);
        logger.info("Backlog projects: {}", backlogCount);
        logger.info("===========================");
        
        // Log some examples
        logger.info("Sample migrated projects:");
        allProjects.stream()
                .limit(5)
                .forEach(project -> logger.info("  - {} [{}] (Priority: {}) {}", 
                        project.getTitle(),
                        project.getCompletionStatus(),
                        project.getPriority(),
                        project.getRepositoryUrl() != null ? "GitHub: ✓" : "GitHub: ✗"
                ));
    }
}