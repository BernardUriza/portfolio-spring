package com.portfolio.core.application.usecase;

import com.portfolio.core.domain.experience.Experience;
import com.portfolio.core.domain.experience.ExperienceType;
import com.portfolio.core.port.in.GetExperiencesUseCase;
import com.portfolio.core.port.out.ExperienceRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Creado por Bernard Orozco
 */
@Component
public class GetExperiencesUseCaseImpl implements GetExperiencesUseCase {

    private final ExperienceRepositoryPort experienceRepository;

    public GetExperiencesUseCaseImpl(ExperienceRepositoryPort experienceRepository) {
        this.experienceRepository = experienceRepository;
    }
    
    @Override
    public List<Experience> getAllExperiences() {
        return experienceRepository.findOrderedByStartDateDesc();
    }
    
    @Override
    public List<Experience> getExperiencesPaginated(int page, int size) {
        validatePaginationParams(page, size);
        return experienceRepository.findPaginated(page, size);
    }
    
    @Override
    public Optional<Experience> getExperienceById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Experience ID cannot be null");
        }
        return experienceRepository.findById(id);
    }
    
    @Override
    public List<Experience> getExperiencesByType(ExperienceType type) {
        if (type == null) {
            throw new IllegalArgumentException("Experience type cannot be null");
        }
        return experienceRepository.findByType(type);
    }
    
    @Override
    public List<Experience> getCurrentExperiences() {
        return experienceRepository.findByIsCurrentPositionTrue();
    }
    
    @Override
    public List<Experience> getExperiencesByCompany(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be null or empty");
        }
        return experienceRepository.findByCompanyNameIgnoreCase(companyName.trim());
    }
    
    @Override
    public List<Experience> getLongTermExperiences() {
        return experienceRepository.findLongTermExperiences();
    }
    
    @Override
    public List<Experience> getExperiencesOrderedByStartDate() {
        return experienceRepository.findOrderedByStartDateDesc();
    }
    
    private void validatePaginationParams(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }
        if (size > 100) {
            throw new IllegalArgumentException("Page size cannot exceed 100");
        }
    }
}