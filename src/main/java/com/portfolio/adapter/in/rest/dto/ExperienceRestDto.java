package com.portfolio.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.portfolio.core.domain.experience.ExperienceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@Jacksonized
public class ExperienceRestDto {
    
    private Long id;
    
    @NotBlank(message = "Job title cannot be blank")
    @Size(max = 200, message = "Job title cannot exceed 200 characters")
    private String jobTitle;
    
    @NotBlank(message = "Company name cannot be blank")
    @Size(max = 200, message = "Company name cannot exceed 200 characters")
    private String companyName;
    
    private String companyUrl;
    
    private String location;
    
    @NotNull(message = "Experience type is required")
    private ExperienceType type;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    private Boolean isCurrentPosition;
    
    private List<String> achievements;
    
    private List<String> technologies;
    
    private Set<Long> skillIds;
    
    private String companyLogoUrl;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}