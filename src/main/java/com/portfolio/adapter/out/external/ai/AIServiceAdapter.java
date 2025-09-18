package com.portfolio.adapter.out.external.ai;

import com.portfolio.core.port.out.AIServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AIServiceAdapter implements AIServicePort {
    private static final Logger log = LoggerFactory.getLogger(AIServiceAdapter.class);
    private final AIServiceImpl aiService;

    public AIServiceAdapter(AIServiceImpl aiService) {
        this.aiService = aiService;
    }
    
    @Override
    public String generateProjectSummary(String title, String description, String technologies) {
        log.debug("Generating project summary for title: {}", title);
        
        try {
            return aiService.generateProjectSummary(title, description, technologies);
        } catch (Exception e) {
            log.error("Error generating project summary", e);
            return "Unable to generate project summary at this time.";
        }
    }
    
    @Override
    public String generateDynamicMessage(String technologies) {
        log.debug("Generating dynamic message for technologies: {}", technologies);
        
        try {
            return aiService.generateDynamicMessage(technologies);
        } catch (Exception e) {
            log.error("Error generating dynamic message", e);
            return "Unable to generate dynamic message at this time.";
        }
    }
    
    @Override
    public String analyzeTechnologies(List<String> technologies) {
        log.debug("Analyzing technologies: {}", technologies);
        
        try {
            String techString = String.join(", ", technologies);
            return aiService.generateDynamicMessage(techString);
        } catch (Exception e) {
            log.error("Error analyzing technologies", e);
            return "Unable to analyze technologies at this time.";
        }
    }
    
    @Override
    public AIServicePort.ClaudeAnalysisResult analyzeRepository(String repoName, String description, 
                                                               String readmeContent, List<String> topics, String language) {
        log.debug("Analyzing repository with AI: {}", repoName);
        
        try {
            return aiService.analyzeRepository(repoName, description, readmeContent, topics, language);
        } catch (Exception e) {
            log.error("Error analyzing repository with AI", e);
            // Return minimal fallback data
            List<String> skills = topics != null ? new ArrayList<>(topics) : new ArrayList<>();
            if (language != null && !skills.contains(language)) {
                skills.add(language);
            }
            
            AIServicePort.ProjectData projectData = new AIServicePort.ProjectData(
                repoName, 
                description != null ? description : "GitHub repository: " + repoName,
                null,
                skills,
                ""
            );
            
            return new AIServicePort.ClaudeAnalysisResult(projectData, skills, List.of("Software Development"));
        }
    }
    
    @Override
    public String chat(String systemPrompt, String userPrompt) {
        log.debug("Chat request with system prompt length: {}, user prompt length: {}", 
                 systemPrompt != null ? systemPrompt.length() : 0, 
                 userPrompt != null ? userPrompt.length() : 0);
        
        try {
            return aiService.chat(systemPrompt, userPrompt);
        } catch (Exception e) {
            log.error("Error in chat request", e);
            return "Lo siento, no puedo generar una respuesta en este momento.";
        }
    }
}
