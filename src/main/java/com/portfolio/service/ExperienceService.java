package com.portfolio.service;

import com.portfolio.dto.ExperienceDTO;
import com.portfolio.model.Experience;
import com.portfolio.mapper.ExperienceMapper;
import com.portfolio.repository.ExperienceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final ExperienceMapper experienceMapper;

    public ExperienceService(ExperienceRepository experienceRepository, ExperienceMapper experienceMapper) {
        this.experienceRepository = experienceRepository;
        this.experienceMapper = experienceMapper;
    }

    public List<ExperienceDTO> getAllExperiences() {
        return experienceRepository.findAll()
                .stream()
                .map(experienceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExperienceDTO createExperience(ExperienceDTO dto) {
        Experience experience = experienceMapper.toEntity(dto);
        experience = experienceRepository.save(experience);
        return experienceMapper.toDto(experience);
    }

    @Transactional
    public ExperienceDTO updateExperience(Long id, ExperienceDTO dto) {
        Experience experience = experienceRepository.findById(id).orElseThrow();
        experience.setTitle(dto.getTitle());
        experience.setCompany(dto.getCompany());
        experience.setDescription(dto.getDescription());
        Experience updated = experienceRepository.save(experience);
        return experienceMapper.toDto(updated);
    }

    @Transactional
    public void deleteExperience(Long id) {
        experienceRepository.deleteById(id);
    }

}
