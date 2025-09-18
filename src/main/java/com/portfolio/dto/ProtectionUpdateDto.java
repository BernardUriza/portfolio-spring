package com.portfolio.dto;

/**
 * Creado por Bernard Orozco
 */
public class ProtectionUpdateDto {

    private Boolean protectDescription;
    private Boolean protectLiveDemoUrl;
    private Boolean protectSkills;
    private Boolean protectExperiences;

    public ProtectionUpdateDto() {
    }

    public ProtectionUpdateDto(Boolean protectDescription, Boolean protectLiveDemoUrl,
                              Boolean protectSkills, Boolean protectExperiences) {
        this.protectDescription = protectDescription;
        this.protectLiveDemoUrl = protectLiveDemoUrl;
        this.protectSkills = protectSkills;
        this.protectExperiences = protectExperiences;
    }

    public Boolean getProtectDescription() {
        return protectDescription;
    }

    public void setProtectDescription(Boolean protectDescription) {
        this.protectDescription = protectDescription;
    }

    public Boolean getProtectLiveDemoUrl() {
        return protectLiveDemoUrl;
    }

    public void setProtectLiveDemoUrl(Boolean protectLiveDemoUrl) {
        this.protectLiveDemoUrl = protectLiveDemoUrl;
    }

    public Boolean getProtectSkills() {
        return protectSkills;
    }

    public void setProtectSkills(Boolean protectSkills) {
        this.protectSkills = protectSkills;
    }

    public Boolean getProtectExperiences() {
        return protectExperiences;
    }

    public void setProtectExperiences(Boolean protectExperiences) {
        this.protectExperiences = protectExperiences;
    }
}