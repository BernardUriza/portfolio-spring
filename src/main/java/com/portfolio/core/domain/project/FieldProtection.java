package com.portfolio.core.domain.project;

public class FieldProtection {
    private final Boolean description;
    private final Boolean liveDemoUrl;
    private final Boolean skills;
    private final Boolean experiences;

    private FieldProtection(Builder b) {
        this.description = b.description != null ? b.description : false;
        this.liveDemoUrl = b.liveDemoUrl != null ? b.liveDemoUrl : false;
        this.skills = b.skills != null ? b.skills : false;
        this.experiences = b.experiences != null ? b.experiences : false;
    }

    public static Builder builder() { return new Builder(); }
    public Builder toBuilder() { return new Builder(this); }

    public Boolean getDescription() { return description; }
    public Boolean getLiveDemoUrl() { return liveDemoUrl; }
    public Boolean getSkills() { return skills; }
    public Boolean getExperiences() { return experiences; }
    
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

    public static final class Builder {
        private Boolean description;
        private Boolean liveDemoUrl;
        private Boolean skills;
        private Boolean experiences;

        public Builder() {}
        public Builder(FieldProtection fp) {
            this.description = fp.description;
            this.liveDemoUrl = fp.liveDemoUrl;
            this.skills = fp.skills;
            this.experiences = fp.experiences;
        }
        public Builder description(Boolean description) { this.description = description; return this; }
        public Builder liveDemoUrl(Boolean liveDemoUrl) { this.liveDemoUrl = liveDemoUrl; return this; }
        public Builder skills(Boolean skills) { this.skills = skills; return this; }
        public Builder experiences(Boolean experiences) { this.experiences = experiences; return this; }
        public FieldProtection build() { return new FieldProtection(this); }
    }
}
