package com.portfolio.service;

import com.portfolio.dto.SkillDTO;
import com.portfolio.model.Skill;
import com.portfolio.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;

    public List<SkillDTO> getAllSkills() {
        return skillRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public SkillDTO createSkill(SkillDTO dto) {
        Skill skill = toEntity(dto);
        skill = skillRepository.save(skill);
        return toDto(skill);
    }

    public void deleteSkill(Long id) {
        skillRepository.deleteById(id);
    }

    public SkillDTO updateSkill(Long id, SkillDTO dto) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Skill no encontrada con id: " + id));
        skill.setName(dto.getName());
        skill.setDescription(dto.getDescription());
        skill = skillRepository.save(skill);
        return toDto(skill);
    }

    private Skill toEntity(SkillDTO dto) {
        return Skill.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }

    private SkillDTO toDto(Skill skill) {
        SkillDTO dto = new SkillDTO();
        dto.setId(skill.getId());
        dto.setName(skill.getName());
        dto.setDescription(skill.getDescription());
        return dto;
    }
}
