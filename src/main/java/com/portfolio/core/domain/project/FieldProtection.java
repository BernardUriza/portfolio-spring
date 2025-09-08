package com.portfolio.core.domain.project;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FieldProtection {
    @Builder.Default
    private final Boolean description = false;
    @Builder.Default
    private final Boolean liveDemoUrl = false;
    @Builder.Default
    private final Boolean skills = false;
    @Builder.Default
    private final Boolean experiences = false;
    
    public FieldProtection withDescription(Boolean value) {
        return this.toBuilder().description(value).build();
    }
    
    public FieldProtection withLiveDemoUrl(Boolean value) {
        return this.toBuilder().liveDemoUrl(value).build();
    }
    
    public FieldProtection withSkills(Boolean value) {
        return this.toBuilder().skills(value).build();
    }
    
    public FieldProtection withExperiences(Boolean value) {
        return this.toBuilder().experiences(value).build();
    }
    
    public static FieldProtection allUnprotected() {
        return FieldProtection.builder().build();
    }
    
    public static FieldProtection allProtected() {
        return FieldProtection.builder()
                .description(true)
                .liveDemoUrl(true)
                .skills(true)
                .experiences(true)
                .build();
    }
}