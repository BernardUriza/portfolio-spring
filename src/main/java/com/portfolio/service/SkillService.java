package com.portfolio.service;

import com.portfolio.dto.SkillDTO;
import com.portfolio.exception.ResourceNotFoundException;
import com.portfolio.model.Skill;
import com.portfolio.mapper.SkillMapper;
import com.portfolio.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    public Page<SkillDTO> getAllSkills(Pageable pageable) {
        return skillRepository.findAll(pageable)
                .map(skillMapper::toDto);
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
