package com.portfolio.adapter.out.external.ai;

import com.portfolio.core.port.out.AIServicePort;
import com.portfolio.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AIServiceAdapter implements AIServicePort {
    
    private final AIService aiService;
    
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
}