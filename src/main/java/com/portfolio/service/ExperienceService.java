package com.portfolio.service;

import com.portfolio.dto.ExperienceDTO;
import com.portfolio.model.Experience;
import com.portfolio.repository.ExperienceRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExperienceService {

    private final ExperienceRepository experienceRepository;

    public ExperienceService(ExperienceRepository experienceRepository) {
        this.experienceRepository = experienceRepository;
    }

    public List<ExperienceDTO> getAllExperiences() {
        return experienceRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ExperienceDTO createExperience(ExperienceDTO dto) {
        Experience experience = new Experience(null, dto.getTitle(), dto.getCompany(), dto.getDescription());
        Experience saved = experienceRepository.save(experience);
        return convertToDTO(saved);
    }

    public ExperienceDTO updateExperience(Long id, ExperienceDTO dto) {
        Experience experience = experienceRepository.findById(id).orElseThrow();
        experience.setTitle(dto.getTitle());
        experience.setCompany(dto.getCompany());
        experience.setDescription(dto.getDescription());
        Experience updated = experienceRepository.save(experience);
        return convertToDTO(updated);
    }

    public void deleteExperience(Long id) {
        experienceRepository.deleteById(id);
    }

    private ExperienceDTO convertToDTO(Experience experience) {
        return new ExperienceDTO(
                experience.getId(),
                experience.getTitle(),
                experience.getCompany(),
                experience.getDescription()
        );
    }
}
