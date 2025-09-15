package com.portfolio.core.domain.project;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ProjectCompleteness {
    private final Integer score;
    @Builder.Default
    private final List<String> missing = new ArrayList<>();
    
    public static ProjectCompleteness calculate(PortfolioProject project) {
        List<String> missing = new ArrayList<>();
        
        // Description - 40% weight
        if (!project.isDescriptionComplete()) {
            missing.add("Description");
        }
        
        // Live Demo URL - 20% weight  
        if (!project.isLiveDemoComplete()) {
            missing.add("Live Demo");
        }
        
        // Skills - 20% weight
        if (!project.hasSkills()) {
            missing.add("Skills");
        }
        
        // Experiences - 20% weight
        if (!project.hasExperiences()) {
            missing.add("Experiences");
        }
        
        // Calculate weighted score
        int score = 0;
        if (project.isDescriptionComplete()) score += 40;
        if (project.isLiveDemoComplete()) score += 20;
        if (project.hasSkills()) score += 20;
        if (project.hasExperiences()) score += 20;
        
        return ProjectCompleteness.builder()
                .score(score)
                .missing(missing)
                .build();
    }
}
