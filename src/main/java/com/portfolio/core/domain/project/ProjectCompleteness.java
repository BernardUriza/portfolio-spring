package com.portfolio.core.domain.project;

import java.util.ArrayList;
import java.util.List;

public class ProjectCompleteness {
    private final Integer score;
    private final List<String> missing;

    private ProjectCompleteness(Builder b) {
        this.score = b.score;
        this.missing = b.missing != null ? b.missing : new ArrayList<>();
    }

    public static Builder builder() { return new Builder(); }

    public Integer getScore() { return score; }
    public List<String> getMissing() { return missing; }
    
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

    public static final class Builder {
        private Integer score;
        private List<String> missing;
        public Builder score(Integer score) { this.score = score; return this; }
        public Builder missing(List<String> missing) { this.missing = missing; return this; }
        public ProjectCompleteness build() { return new ProjectCompleteness(this); }
    }
}
