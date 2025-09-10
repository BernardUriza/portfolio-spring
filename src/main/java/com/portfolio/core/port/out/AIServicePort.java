package com.portfolio.core.port.out;

import com.portfolio.core.domain.project.PortfolioProject;
import com.portfolio.core.domain.skill.Skill;
import com.portfolio.core.domain.experience.Experience;
import java.util.List;

public interface AIServicePort {
    
    String generateProjectSummary(String title, String description, String technologies);
    
    String generateDynamicMessage(String technologies);
    
    String analyzeTechnologies(java.util.List<String> technologies);
    
    /**
     * Analyzes a GitHub repository to extract structured project data, skills, and experiences
     * @param repoName Repository name
     * @param description Repository description
     * @param readmeContent README markdown content
     * @param topics Repository topics/tags
     * @param language Primary programming language
     * @return ClaudeAnalysisResult containing extracted entities
     */
    ClaudeAnalysisResult analyzeRepository(String repoName, String description, 
                                         String readmeContent, List<String> topics, String language);
    
    /**
     * General chat method for AI interactions
     * @param systemPrompt System-level instructions for the AI
     * @param userPrompt User message or prompt
     * @return AI response
     */
    String chat(String systemPrompt, String userPrompt);
    
    /**
     * Result of Claude analysis containing structured data
     */
    class ClaudeAnalysisResult {
        public final ProjectData project;
        public final List<String> skills;
        public final List<String> experiences;
        
        public ClaudeAnalysisResult(ProjectData project, List<String> skills, List<String> experiences) {
            this.project = project;
            this.skills = skills;
            this.experiences = experiences;
        }
    }
    
    /**
     * Extracted project data from Claude
     */
    class ProjectData {
        public final String name;
        public final String description;
        public final Integer estimatedDurationWeeks;
        public final List<String> technologies;
        public final String url;
        
        public ProjectData(String name, String description, Integer estimatedDurationWeeks, 
                          List<String> technologies, String url) {
            this.name = name;
            this.description = description;
            this.estimatedDurationWeeks = estimatedDurationWeeks;
            this.technologies = technologies;
            this.url = url;
        }
    }
}