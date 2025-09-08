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
    
    public static ProjectCompleteness calculate(Project project) {
        List<String> missing = new ArrayList<>();
        int completedFields = 0;
        int totalFields = 4; // Description, LiveDemo, Skills, Experiences
        
        // Description - 40% weight
        if (!project.isDescriptionComplete()) {
            missing.add("Description");
        } else {
            completedFields++;
        }
        
        // Live Demo URL - 20% weight  
        if (!project.isLiveDemoComplete()) {
            missing.add("Live Demo");
        } else {
            completedFields++;
        }
        
        // Skills - 20% weight
        if (!project.hasSkills()) {
            missing.add("Skills");
        } else {
            completedFields++;
        }
        
        // Experiences - 20% weight
        if (!project.hasExperiences()) {
            missing.add("Experiences");
        } else {
            completedFields++;
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