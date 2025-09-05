package com.portfolio.service;

import com.portfolio.model.*;
import com.portfolio.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SemanticTransformationService {
    
    private final ClaudeAnalysisService claudeAnalysisService;
    private final SkillRepository skillRepository;
    private final ExperienceRepository experienceRepository;
    private final ProjectRepository projectRepository;
    
    @Transactional
    public void processStarredProject(StarredProject starredProject) {
        log.info("Starting semantic transformation for starred project: {}", starredProject.getName());
        
        try {
            if (projectRepository.existsBySourceStarredProject(starredProject)) {
                log.debug("Project already exists for starred repo: {}, skipping", starredProject.getName());
                return;
            }
            
            ClaudeAnalysisService.SemanticAnalysisResult analysisResult = 
                claudeAnalysisService.analyzeRepository(
                    starredProject.getName(),
                    starredProject.getDescription(), 
                    starredProject.getTopics(),
                    starredProject.getPrimaryLanguage(),
                    starredProject.getHomepageUrl()
                );
            
            if (analysisResult == null) {
                log.warn("Claude analysis returned null for: {}", starredProject.getName());
                return;
            }
            
            Set<Skill> projectSkills = processSkills(analysisResult.skills());
            Set<Experience> projectExperiences = processExperiences(analysisResult.experiences(), projectSkills);
            Project project = createProject(analysisResult.project(), starredProject, projectSkills, projectExperiences);
            
            log.info("Successfully transformed starred project '{}' into {} skills, {} experiences, and 1 project", 
                    starredProject.getName(), projectSkills.size(), projectExperiences.size());
                    
        } catch (Exception e) {
            log.error("Failed to process semantic transformation for starred project '{}': {}", 
                    starredProject.getName(), e.getMessage(), e);
        }
    }
    
    private Set<Skill> processSkills(List<String> skillNames) {
        Set<Skill> skills = new HashSet<>();
        
        if (skillNames == null || skillNames.isEmpty()) {
            return skills;
        }
        
        for (String skillName : skillNames) {
            if (skillName == null || skillName.trim().isEmpty()) {
                continue;
            }
            
            String cleanSkillName = skillName.trim();
            Optional<Skill> existingSkill = skillRepository.findByNameIgnoreCase(cleanSkillName);
            
            if (existingSkill.isPresent()) {
                skills.add(existingSkill.get());
                log.debug("Using existing skill: {}", cleanSkillName);
            } else {
                Skill newSkill = Skill.builder()
                        .name(cleanSkillName)
                        .category(inferSkillCategory(cleanSkillName))
                        .level(Skill.SkillLevel.INTERMEDIATE)
                        .isActive(true)
                        .build();
                
                Skill savedSkill = skillRepository.save(newSkill);
                skills.add(savedSkill);
                log.debug("Created new skill: {}", cleanSkillName);
            }
        }
        
        return skills;
    }
    
    private Set<Experience> processExperiences(List<String> experienceDescriptions, Set<Skill> relatedSkills) {
        Set<Experience> experiences = new HashSet<>();
        
        if (experienceDescriptions == null || experienceDescriptions.isEmpty()) {
            return experiences;
        }
        
        for (String description : experienceDescriptions) {
            if (description == null || description.trim().isEmpty()) {
                continue;
            }
            
            String cleanDescription = description.trim();
            String experienceTitle = generateExperienceTitle(cleanDescription);
            
            Optional<Experience> existingExperience = experienceRepository.findByTitleIgnoreCase(experienceTitle);
            
            if (existingExperience.isPresent()) {
                experiences.add(existingExperience.get());
                log.debug("Using existing experience: {}", experienceTitle);
            } else {
                Experience newExperience = Experience.builder()
                        .title(experienceTitle)
                        .description(cleanDescription)
                        .type(Experience.ExperienceType.OPEN_SOURCE)
                        .level(Experience.ExperienceLevel.INTERMEDIATE)
                        .skills(new HashSet<>(relatedSkills))
                        .isActive(true)
                        .build();
                
                Experience savedExperience = experienceRepository.save(newExperience);
                experiences.add(savedExperience);
                log.debug("Created new experience: {}", experienceTitle);
            }
        }
        
        return experiences;
    }
    
    private Project createProject(ClaudeAnalysisService.ProjectInfo projectInfo, 
                                StarredProject sourceStarredProject,
                                Set<Skill> skills, 
                                Set<Experience> experiences) {
        
        if (projectInfo == null) {
            log.warn("ProjectInfo is null, creating minimal project for: {}", sourceStarredProject.getName());
            projectInfo = new ClaudeAnalysisService.ProjectInfo(
                sourceStarredProject.getName(),
                sourceStarredProject.getDescription() != null ? sourceStarredProject.getDescription() : "No description",
                4,
                List.of(sourceStarredProject.getPrimaryLanguage() != null ? sourceStarredProject.getPrimaryLanguage() : "Unknown"),
                sourceStarredProject.getRepositoryUrl()
            );
        }
        
        Project project = Project.builder()
                .title(projectInfo.name() != null ? projectInfo.name() : sourceStarredProject.getName())
                .description(projectInfo.description() != null ? projectInfo.description() : "No description")
                .githubRepo(projectInfo.githubUrl() != null ? projectInfo.githubUrl() : sourceStarredProject.getRepositoryUrl())
                .link(sourceStarredProject.getHomepageUrl())
                .estimatedDurationWeeks(projectInfo.estimatedDurationWeeks())
                .mainTechnologies(projectInfo.mainTechnologies() != null ? projectInfo.mainTechnologies() : List.of())
                .createdDate(LocalDate.now())
                .status(Project.ProjectStatus.ACTIVE)
                .type(Project.ProjectType.OPEN_SOURCE)
                .skills(skills)
                .experiences(experiences)
                .sourceStarredProject(sourceStarredProject)
                .build();
        
        Project savedProject = projectRepository.save(project);
        log.debug("Created project: {} with {} skills and {} experiences", 
                savedProject.getTitle(), skills.size(), experiences.size());
        
        return savedProject;
    }
    
    private Skill.SkillCategory inferSkillCategory(String skillName) {
        String lowerSkillName = skillName.toLowerCase();
        
        if (lowerSkillName.contains("spring") || lowerSkillName.contains("react") || 
            lowerSkillName.contains("angular") || lowerSkillName.contains("vue") ||
            lowerSkillName.contains("express") || lowerSkillName.contains("django")) {
            return Skill.SkillCategory.FRAMEWORK;
        }
        
        if (lowerSkillName.contains("java") || lowerSkillName.contains("python") || 
            lowerSkillName.contains("javascript") || lowerSkillName.contains("typescript") ||
            lowerSkillName.contains("kotlin") || lowerSkillName.contains("swift")) {
            return Skill.SkillCategory.LANGUAGE;
        }
        
        if (lowerSkillName.contains("mysql") || lowerSkillName.contains("postgresql") || 
            lowerSkillName.contains("mongodb") || lowerSkillName.contains("redis")) {
            return Skill.SkillCategory.DATABASE;
        }
        
        if (lowerSkillName.contains("docker") || lowerSkillName.contains("kubernetes") || 
            lowerSkillName.contains("git") || lowerSkillName.contains("jenkins")) {
            return Skill.SkillCategory.TOOL;
        }
        
        if (lowerSkillName.contains("aws") || lowerSkillName.contains("azure") || 
            lowerSkillName.contains("gcp") || lowerSkillName.contains("heroku")) {
            return Skill.SkillCategory.PLATFORM;
        }
        
        return Skill.SkillCategory.TECHNICAL;
    }
    
    private String generateExperienceTitle(String description) {
        if (description.length() <= 50) {
            return description;
        }
        
        String[] words = description.split("\\s+");
        StringBuilder title = new StringBuilder();
        
        for (String word : words) {
            if (title.length() + word.length() + 1 <= 50) {
                if (title.length() > 0) title.append(" ");
                title.append(word);
            } else {
                break;
            }
        }
        
        return title.toString();
    }
}