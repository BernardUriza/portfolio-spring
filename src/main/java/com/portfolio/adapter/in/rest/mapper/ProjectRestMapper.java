package com.portfolio.adapter.in.rest.mapper;

import com.portfolio.adapter.in.rest.dto.ProjectRestDto;
import com.portfolio.core.domain.project.Project;
import com.portfolio.core.domain.project.ProjectStatus;
import com.portfolio.core.domain.project.ProjectType;
import com.portfolio.core.port.in.CreateProjectUseCase;
import com.portfolio.core.port.in.UpdateProjectUseCase;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ProjectRestMapper {
    
    public ProjectRestDto toRestDto(Project domain) {
        if (domain == null) return null;
        
        return ProjectRestDto.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .link(domain.getLink())
                .githubRepo(domain.getGithubRepo())
                .createdDate(domain.getCreatedDate())
                .estimatedDurationWeeks(domain.getEstimatedDurationWeeks())
                .status(domain.getStatus() != null ? domain.getStatus().name() : null)
                .type(domain.getType() != null ? domain.getType().name() : null)
                .mainTechnologies(domain.getMainTechnologies() != null ? 
                        new ArrayList<>(domain.getMainTechnologies()) : new ArrayList<>())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
    
    public CreateProjectUseCase.CreateProjectCommand toCreateCommand(ProjectRestDto dto) {
        if (dto == null) return null;
        
        return new CreateProjectUseCase.CreateProjectCommand(
                dto.getTitle(),
                dto.getDescription(),
                dto.getLink(),
                dto.getGithubRepo(),
                dto.getCreatedDate(),
                dto.getMainTechnologies() != null ? new ArrayList<>(dto.getMainTechnologies()) : new ArrayList<>()
        );
    }
    
    public UpdateProjectUseCase.UpdateProjectCommand toUpdateCommand(Long id, ProjectRestDto dto) {
        if (dto == null) return null;
        
        return new UpdateProjectUseCase.UpdateProjectCommand(
                id,
                dto.getTitle(),
                dto.getDescription(),
                dto.getLink(),
                dto.getGithubRepo()
        );
    }
    
    private ProjectStatus mapStringToStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return ProjectStatus.ACTIVE;
        }
        
        try {
            return ProjectStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ProjectStatus.ACTIVE;
        }
    }
    
    private ProjectType mapStringToType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return ProjectType.PERSONAL;
        }
        
        try {
            return ProjectType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ProjectType.PERSONAL;
        }
    }
}