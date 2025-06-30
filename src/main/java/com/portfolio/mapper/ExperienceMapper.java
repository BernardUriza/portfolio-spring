package com.portfolio.mapper;

import com.portfolio.dto.ExperienceDTO;
import com.portfolio.model.Experience;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExperienceMapper {
    ExperienceDTO toDto(Experience experience);
    Experience toEntity(ExperienceDTO dto);
}
