package com.portfolio.service;

import com.portfolio.dto.SkillDTO;
import com.portfolio.exception.ResourceNotFoundException;
import com.portfolio.model.Skill;
import com.portfolio.mapper.SkillMapper;
import com.portfolio.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    public List<SkillDTO> getAllSkills() {
        return skillRepository.findAll()
                .stream()
                .map(skillMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public SkillDTO createSkill(SkillDTO dto) {
        Skill skill = skillMapper.toEntity(dto);
        skill = skillRepository.save(skill);
        return skillMapper.toDto(skill);
    }

    @Transactional
    public void deleteSkill(Long id) {
        skillRepository.deleteById(id);
    }

    @Transactional
    public SkillDTO updateSkill(Long id, SkillDTO dto) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill no encontrada con id: " + id));
        skill.setName(dto.getName());
        skill.setDescription(dto.getDescription());
        skill = skillRepository.save(skill);
        return skillMapper.toDto(skill);
    }

}
