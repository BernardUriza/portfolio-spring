package com.portfolio.mapper;

import com.portfolio.dto.ProjectDTO;
import com.portfolio.model.Project;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    ProjectDTO toDto(Project project);
    Project toEntity(ProjectDTO dto);
}
