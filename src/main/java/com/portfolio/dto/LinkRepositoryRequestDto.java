package com.portfolio.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class LinkRepositoryRequestDto {
    
    @NotNull(message = "Source repository ID is required")
    @Positive(message = "Source repository ID must be positive")
    private Long sourceRepositoryId;
    
    @NotNull(message = "Link type is required")
    private String linkType; // "AUTO" or "MANUAL"
}