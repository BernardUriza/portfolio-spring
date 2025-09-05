package com.portfolio.adapter.out.external.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AIServiceImpl {
    
    public String generateProjectSummary(String title, String description, String technologies) {
        log.info("Generating project summary for: {}", title);
        
        // TODO: Implement actual AI service call (Claude/OpenAI)
        // For now, return a mock response
        return String.format("AI Summary for '%s': This project uses %s and focuses on %s", 
                title, technologies, description.substring(0, Math.min(description.length(), 50)));
    }
    
    public String generateDynamicMessage(String technologies) {
        log.info("Generating dynamic message for technologies: {}", technologies);
        
        // TODO: Implement actual AI service call
        return String.format("Dynamic message: Great choice using %s! These technologies are trending.", 
                technologies != null ? technologies : "modern tech stack");
    }
}