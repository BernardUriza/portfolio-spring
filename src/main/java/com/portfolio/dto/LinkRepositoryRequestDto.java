/**
 * Creado por Bernard Orozco
 * DTO for linking repository requests
 */
package com.portfolio.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class LinkRepositoryRequestDto {

    @NotNull(message = "Source repository ID is required")
    @Positive(message = "Source repository ID must be positive")
    private Long sourceRepositoryId;

    @NotNull(message = "Link type is required")
    private String linkType; // "AUTO" or "MANUAL"

    // Default constructor
    public LinkRepositoryRequestDto() {
    }

    // Getters and Setters
    public Long getSourceRepositoryId() {
        return sourceRepositoryId;
    }

    public void setSourceRepositoryId(Long sourceRepositoryId) {
        this.sourceRepositoryId = sourceRepositoryId;
    }

    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }
}