package com.portfolio.service;

import com.portfolio.adapter.out.persistence.jpa.StarredProjectJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.StarredProjectJpaRepository;
import com.portfolio.adapter.out.persistence.jpa.ProjectJpaRepository;
import com.portfolio.adapter.out.persistence.jpa.ProjectJpaEntity;
import com.portfolio.core.domain.project.Project;
import com.portfolio.core.domain.project.ProjectStatus;
import com.portfolio.core.domain.project.ProjectType;
import com.portfolio.core.domain.skill.Skill;
import com.portfolio.core.domain.skill.SkillCategory;
import com.portfolio.core.domain.skill.SkillLevel;
import com.portfolio.core.domain.experience.Experience;
import com.portfolio.core.domain.experience.ExperienceType;
import com.portfolio.core.port.out.AIServicePort;
import com.portfolio.core.port.out.ProjectRepositoryPort;
import com.portfolio.core.port.out.SkillRepositoryPort;
import com.portfolio.core.port.out.ExperienceRepositoryPort;
import com.portfolio.dto.StarredProjectDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StarredProjectService {
    
    private final StarredProjectJpaRepository starredProjectRepository;
    private final ProjectJpaRepository projectRepository;
    private final SyncMonitorService syncMonitorService;
    private final AIServicePort aiService;
    private final ProjectRepositoryPort projectRepositoryPort;
    private final SkillRepositoryPort skillRepositoryPort;
    private final ExperienceRepositoryPort experienceRepositoryPort;
    
    public List<StarredProjectDto> getAllStarredProjects() {
        List<StarredProjectJpaEntity> entities = starredProjectRepository.findAllOrderByUpdatedAtDesc();
        return entities.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    public Optional<StarredProjectDto> getStarredProject(Long id) {
        return starredProjectRepository.findById(id)
                .map(this::mapToDto);
    }
    
    @Transactional
    public Optional<StarredProjectDto> updateProjectHomepage(Long id, String homepage) {
        Optional<StarredProjectJpaEntity> projectOpt = starredProjectRepository.findById(id);
        
        if (projectOpt.isEmpty()) {
            return Optional.empty();
        }
        
        StarredProjectJpaEntity project = projectOpt.get();
        project.setHomepage(homepage);
        project.setUpdatedAt(LocalDateTime.now());
        
        StarredProjectJpaEntity updated = starredProjectRepository.save(project);
        
        syncMonitorService.appendLog("INFO", 
            String.format("Updated homepage for project '%s': %s", project.getName(), homepage));
        
        return Optional.of(mapToDto(updated));
    }
    
    @Transactional
    public boolean deleteStarredProject(Long id) {
        Optional<StarredProjectJpaEntity> starredProjectOpt = starredProjectRepository.findById(id);
        
        if (starredProjectOpt.isEmpty()) {
            return false;
        }
        
        StarredProjectJpaEntity starredProject = starredProjectOpt.get();
        String projectName = starredProject.getName();
        
        try {
            syncMonitorService.appendLog("INFO", "Deleting starred project: " + projectName + " (ID: " + id + ")");
            
            // Delete related Project entities that reference this StarredProject
            List<ProjectJpaEntity> relatedProjects = projectRepository.findAll().stream()
                    .filter(p -> id.equals(p.getSourceStarredProjectId()))
                    .collect(Collectors.toList());
            
            if (!relatedProjects.isEmpty()) {
                syncMonitorService.appendLog("INFO", 
                    String.format("Found %d related project(s) to delete for starred project: %s", 
                                  relatedProjects.size(), projectName));
                
                for (ProjectJpaEntity project : relatedProjects) {
                    projectRepository.delete(project);
                    syncMonitorService.appendLog("DEBUG", "Deleted related project: " + project.getTitle());
                }
            }
            
            // Delete the StarredProject entity
            starredProjectRepository.delete(starredProject);
            
            syncMonitorService.appendLog("INFO", 
                String.format("Successfully deleted starred project '%s' and %d related project(s)", 
                              projectName, relatedProjects.size()));
            
            return true;
            
        } catch (Exception e) {
            log.error("Error deleting starred project with ID {}: {}", id, e.getMessage(), e);
            syncMonitorService.appendLog("ERROR", 
                String.format("Failed to delete starred project '%s': %s", projectName, e.getMessage()));
            throw new RuntimeException("Failed to delete starred project: " + e.getMessage(), e);
        }
    }
    
    public List<StarredProjectDto> getStarredProjectsByStatus(StarredProjectJpaEntity.SyncStatus status) {
        List<StarredProjectJpaEntity> entities = starredProjectRepository.findBySyncStatusOrderByUpdatedAtDesc(status);
        return entities.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    public long countByStatus(StarredProjectJpaEntity.SyncStatus status) {
        return starredProjectRepository.countBySyncStatus(status);
    }
    
    @Transactional
    public void resetAllProjectsToUnsynced() {
        List<StarredProjectJpaEntity> allProjects = starredProjectRepository.findAll();
        for (StarredProjectJpaEntity project : allProjects) {
            project.setSyncStatus(StarredProjectJpaEntity.SyncStatus.UNSYNCED);
            project.setLastSyncAttempt(null);
            project.setSyncErrorMessage(null);
        }
        starredProjectRepository.saveAll(allProjects);
        
        // Also delete all generated entities
        projectRepository.deleteAll();
        
        syncMonitorService.appendLog("INFO", String.format("Reset %d projects to UNSYNCED status", allProjects.size()));
    }
    
    public Map<String, Object> testClaudeConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Test with a simple prompt
            AIServicePort.ClaudeAnalysisResult testResult = aiService.analyzeRepository(
                "test-repo",
                "A test repository for Claude API",
                "# Test README\nThis is a test",
                List.of("test", "api"),
                "Java"
            );
            
            result.put("success", true);
            result.put("message", "Claude API is working");
            result.put("projectName", testResult.project.name);
            result.put("skills", testResult.skills);
            result.put("experiences", testResult.experiences);
            result.put("usingMockData", testResult.skills.contains("Java") && testResult.experiences.contains("Software Development"));
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("usingMockData", true);
        }
        
        return result;
    }
    
    /**
     * Process unsynced starred projects by analyzing them with Claude and persisting the results
     * to Project, Skill, and Experience entities
     */
    @Transactional
    public void processUnsyncedProjects() {
        List<StarredProjectJpaEntity> unsyncedProjects = starredProjectRepository
                .findBySyncStatus(StarredProjectJpaEntity.SyncStatus.UNSYNCED);
        
        syncMonitorService.appendLog("INFO", String.format("Processing %d unsynced starred projects", 
                                                          unsyncedProjects.size()));
        
        for (StarredProjectJpaEntity starredProject : unsyncedProjects) {
            processStarredProject(starredProject);
        }
    }
    
    /**
     * Process a single starred project with Claude API and persist entities
     */
    @Transactional
    public void processStarredProject(Long starredProjectId) {
        Optional<StarredProjectJpaEntity> starredProjectOpt = starredProjectRepository.findById(starredProjectId);
        if (starredProjectOpt.isPresent()) {
            processStarredProject(starredProjectOpt.get());
        }
    }
    
    @Transactional
    protected void processStarredProject(StarredProjectJpaEntity starredProject) {
        String projectName = starredProject.getName();
        
        try {
            syncMonitorService.appendLog("INFO", "Processing starred project with Claude: " + projectName);
            
            // Mark as processing
            starredProject.setSyncStatus(StarredProjectJpaEntity.SyncStatus.PROCESSING);
            starredProject.setLastSyncAttempt(LocalDateTime.now());
            starredProjectRepository.save(starredProject);
            
            // Call Claude API to analyze repository
            AIServicePort.ClaudeAnalysisResult analysis = aiService.analyzeRepository(
                starredProject.getName(),
                starredProject.getDescription(),
                starredProject.getReadmeMarkdown(),
                starredProject.getTopics(),
                starredProject.getLanguage()
            );
            
            // Process the Claude analysis results
            processClaudeAnalysis(starredProject, analysis);
            
            // Mark as successfully synced
            starredProject.setSyncStatus(StarredProjectJpaEntity.SyncStatus.SYNCED);
            starredProject.setSyncErrorMessage(null);
            starredProjectRepository.save(starredProject);
            
            syncMonitorService.appendLog("INFO", "Successfully processed starred project: " + projectName);
            
        } catch (Exception e) {
            log.error("Error processing starred project: {}", projectName, e);
            
            // Mark as failed with error message
            starredProject.setSyncStatus(StarredProjectJpaEntity.SyncStatus.FAILED);
            starredProject.setSyncErrorMessage(e.getMessage());
            starredProjectRepository.save(starredProject);
            
            syncMonitorService.appendLog("ERROR", 
                String.format("Failed to process starred project '%s': %s", projectName, e.getMessage()));
        }
    }
    
    private void processClaudeAnalysis(StarredProjectJpaEntity starredProject, 
                                      AIServicePort.ClaudeAnalysisResult analysis) {
        
        // 1. Create or update Project entity
        Project project = createOrUpdateProject(starredProject, analysis.project);
        
        // 2. Create or find Skills
        Set<Long> skillIds = processSkills(analysis.skills);
        
        // 3. Create or find Experiences  
        Set<Long> experienceIds = processExperiences(analysis.experiences);
        
        // 4. Link skills and experiences to project
        if (!skillIds.isEmpty() || !experienceIds.isEmpty()) {
            project = linkProjectToEntities(project, skillIds, experienceIds);
        }
        
        syncMonitorService.appendLog("DEBUG", 
            String.format("Created/updated project '%s' with %d skills and %d experiences", 
                         project.getTitle(), skillIds.size(), experienceIds.size()));
    }
    
    private Project createOrUpdateProject(StarredProjectJpaEntity starredProject, 
                                         AIServicePort.ProjectData projectData) {
        
        // Check if project already exists based on source starred project ID
        List<Project> existingProjects = projectRepositoryPort.findAll().stream()
                .filter(p -> starredProject.getId().equals(p.getSourceStarredProjectId()))
                .collect(Collectors.toList());
        
        Project project;
        if (!existingProjects.isEmpty()) {
            // Update existing project with sync immunity
            project = existingProjects.get(0);
            
            // Only update fields that haven't been manually overridden
            String newTitle = projectData.name;
            String newDescription = project.getManualDescriptionOverride() ? 
                    project.getDescription() : projectData.description;
            String newUrl = project.getManualLinkOverride() ? 
                    project.getLink() : projectData.url;
            String githubRepo = starredProject.getGithubRepoUrl();
            
            project = project.updateBasicInfo(newTitle, newDescription, newUrl, githubRepo);
            
            syncMonitorService.appendLog("DEBUG", String.format(
                "Updated project '%s' - protected fields: desc=%s, link=%s", 
                newTitle, 
                project.getManualDescriptionOverride() ? "YES" : "NO",
                project.getManualLinkOverride() ? "YES" : "NO"
            ));
        } else {
            // Create new project
            project = Project.create(
                projectData.name,
                projectData.description,
                projectData.url,
                starredProject.getGithubRepoUrl(),
                LocalDate.now(),
                projectData.technologies
            );
            
            // Set additional properties
            project = project.toBuilder()
                    .estimatedDurationWeeks(projectData.estimatedDurationWeeks)
                    .type(starredProject.getFork() != null && starredProject.getFork() 
                          ? ProjectType.OPEN_SOURCE : ProjectType.PERSONAL)
                    .status(ProjectStatus.ACTIVE)
                    .sourceStarredProjectId(starredProject.getId())
                    .build();
        }
        
        return projectRepositoryPort.save(project);
    }
    
    private Set<Long> processSkills(List<String> skillNames) {
        Set<Long> skillIds = new HashSet<>();
        
        for (String skillName : skillNames) {
            if (skillName == null || skillName.trim().isEmpty()) continue;
            
            String cleanSkillName = skillName.trim();
            
            // Check if skill already exists
            Optional<Skill> existingSkill = skillRepositoryPort.findAll().stream()
                    .filter(s -> s.getName().equalsIgnoreCase(cleanSkillName))
                    .findFirst();
            
            Skill skill;
            if (existingSkill.isPresent()) {
                skill = existingSkill.get();
            } else {
                // Create new skill
                SkillCategory category = inferSkillCategory(cleanSkillName);
                SkillLevel level = SkillLevel.INTERMEDIATE; // Default level
                
                skill = Skill.create(cleanSkillName, "Skill from GitHub repository analysis", category, level);
                skill = skillRepositoryPort.save(skill);
                
                syncMonitorService.appendLog("DEBUG", "Created new skill: " + cleanSkillName);
            }
            
            skillIds.add(skill.getId());
        }
        
        return skillIds;
    }
    
    private Set<Long> processExperiences(List<String> experienceNames) {
        Set<Long> experienceIds = new HashSet<>();
        
        for (String experienceName : experienceNames) {
            if (experienceName == null || experienceName.trim().isEmpty()) continue;
            
            String cleanExperienceName = experienceName.trim();
            
            // Check if similar experience already exists
            Optional<Experience> existingExperience = experienceRepositoryPort.findAll().stream()
                    .filter(e -> e.getJobTitle().equalsIgnoreCase(cleanExperienceName))
                    .findFirst();
            
            Experience experience;
            if (existingExperience.isPresent()) {
                experience = existingExperience.get();
            } else {
                // Create new experience
                experience = Experience.create(
                    cleanExperienceName,
                    "Self-Directed",  // Default company
                    ExperienceType.SELF_EMPLOYED,
                    "Experience inferred from GitHub repository analysis",
                    LocalDate.now().minusYears(1) // Default start date
                );
                experience = experienceRepositoryPort.save(experience);
                
                syncMonitorService.appendLog("DEBUG", "Created new experience: " + cleanExperienceName);
            }
            
            experienceIds.add(experience.getId());
        }
        
        return experienceIds;
    }
    
    private Project linkProjectToEntities(Project project, Set<Long> skillIds, Set<Long> experienceIds) {
        Project.ProjectBuilder builder = project.toBuilder();
        
        // Only update skills if not manually overridden
        if (!project.getManualSkillsOverride()) {
            Set<Long> allSkillIds = new HashSet<>(project.getSkillIds());
            allSkillIds.addAll(skillIds);
            builder.skillIds(allSkillIds);
            
            syncMonitorService.appendLog("DEBUG", 
                String.format("Updated skills for project '%s' (total: %d)", 
                             project.getTitle(), allSkillIds.size()));
        } else {
            syncMonitorService.appendLog("DEBUG", 
                String.format("Skipped skills update for project '%s' - manually protected", 
                             project.getTitle()));
        }
        
        // Only update experiences if not manually overridden
        if (!project.getManualExperiencesOverride()) {
            Set<Long> allExperienceIds = new HashSet<>(project.getExperienceIds());
            allExperienceIds.addAll(experienceIds);
            builder.experienceIds(allExperienceIds);
            
            syncMonitorService.appendLog("DEBUG", 
                String.format("Updated experiences for project '%s' (total: %d)", 
                             project.getTitle(), allExperienceIds.size()));
        } else {
            syncMonitorService.appendLog("DEBUG", 
                String.format("Skipped experiences update for project '%s' - manually protected", 
                             project.getTitle()));
        }
        
        builder.updatedAt(LocalDateTime.now());
        
        return projectRepositoryPort.save(builder.build());
    }
    
    private SkillCategory inferSkillCategory(String skillName) {
        String lowerSkillName = skillName.toLowerCase();
        
        // Programming languages
        if (lowerSkillName.matches(".*(java|python|javascript|typescript|c\\+\\+|c#|go|rust|php|ruby|kotlin|swift|scala).*")) {
            return SkillCategory.PROGRAMMING_LANGUAGE;
        }
        
        // Frameworks
        if (lowerSkillName.matches(".*(react|angular|vue|spring|django|flask|express|laravel|rails|nextjs|nuxt).*")) {
            return SkillCategory.FRAMEWORK;
        }
        
        // Databases
        if (lowerSkillName.matches(".*(mysql|postgresql|mongodb|redis|elasticsearch|sqlite|oracle|cassandra).*")) {
            return SkillCategory.DATABASE;
        }
        
        // Cloud platforms
        if (lowerSkillName.matches(".*(aws|azure|gcp|google cloud|docker|kubernetes|heroku).*")) {
            return SkillCategory.CLOUD_PLATFORM;
        }
        
        // DevOps
        if (lowerSkillName.matches(".*(docker|kubernetes|jenkins|gitlab|github actions|terraform|ansible).*")) {
            return SkillCategory.DEVOPS;
        }
        
        // Frontend
        if (lowerSkillName.matches(".*(html|css|sass|scss|tailwind|bootstrap|webpack|vite).*")) {
            return SkillCategory.FRONTEND;
        }
        
        // Default to tool
        return SkillCategory.TOOL;
    }
    
    private StarredProjectDto mapToDto(StarredProjectJpaEntity entity) {
        return StarredProjectDto.builder()
                .id(entity.getId())
                .githubId(entity.getGithubId())
                .name(entity.getName())
                .fullName(entity.getFullName())
                .description(entity.getDescription())
                .githubRepoUrl(entity.getGithubRepoUrl())
                .homepage(entity.getHomepage())
                .language(entity.getLanguage())
                .fork(entity.getFork())
                .stargazersCount(entity.getStargazersCount())
                .topics(entity.getTopics())
                .githubCreatedAt(entity.getGithubCreatedAt())
                .githubUpdatedAt(entity.getGithubUpdatedAt())
                .readmeMarkdown(entity.getReadmeMarkdown())
                .syncStatus(entity.getSyncStatus())
                .lastSyncAttempt(entity.getLastSyncAttempt())
                .syncErrorMessage(entity.getSyncErrorMessage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}