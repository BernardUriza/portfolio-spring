package com.portfolio.mapper;

import com.portfolio.dto.SkillDTO;
import com.portfolio.model.Skill;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    SkillDTO toDto(Skill skill);
    Skill toEntity(SkillDTO dto);
}
