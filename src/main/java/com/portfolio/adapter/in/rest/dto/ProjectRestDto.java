package com.portfolio.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Jacksonized
public class ProjectRestDto {
    
    private Long id;
    
    @NotBlank(message = "Project title cannot be blank")
    @Size(max = 200, message = "Project title cannot exceed 200 characters")
    private String title;
    
    @NotBlank(message = "Project description cannot be blank")
    @Size(max = 1000, message = "Project description cannot exceed 1000 characters")
    private String description;
    
    private String link;
    
    private String githubRepo;
    
    @NotNull(message = "Created date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;
    
    private Integer estimatedDurationWeeks;
    
    private String status;
    
    private String type;
    
    private List<String> mainTechnologies;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}