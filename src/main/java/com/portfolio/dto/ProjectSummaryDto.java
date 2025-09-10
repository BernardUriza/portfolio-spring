/**
 * Creado por Bernard Orozco
 * Compact project summary for narration context
 */
package com.portfolio.dto;

import java.util.List;

public class ProjectSummaryDto {
    private String id;
    private String name;
    private String ownerRepo;
    private List<String> tech;
    private List<String> outcomes;
    
    public ProjectSummaryDto() {}
    
    public ProjectSummaryDto(String id, String name, String ownerRepo, List<String> tech, List<String> outcomes) {
        this.id = id;
        this.name = name;
        this.ownerRepo = ownerRepo;
        this.tech = tech;
        this.outcomes = outcomes;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getOwnerRepo() {
        return ownerRepo;
    }
    
    public void setOwnerRepo(String ownerRepo) {
        this.ownerRepo = ownerRepo;
    }
    
    public List<String> getTech() {
        return tech;
    }
    
    public void setTech(List<String> tech) {
        this.tech = tech;
    }
    
    public List<String> getOutcomes() {
        return outcomes;
    }
    
    public void setOutcomes(List<String> outcomes) {
        this.outcomes = outcomes;
    }
}