package com.portfolio.core.application.usecase;

import com.portfolio.core.domain.skill.Skill;
import com.portfolio.core.domain.skill.SkillCategory;
import com.portfolio.core.domain.skill.SkillLevel;
import com.portfolio.core.port.in.GetSkillsUseCase;
import com.portfolio.core.port.out.SkillRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Creado por Bernard Orozco
 */
@Component
public class GetSkillsUseCaseImpl implements GetSkillsUseCase {

    private final SkillRepositoryPort skillRepository;

    public GetSkillsUseCaseImpl(SkillRepositoryPort skillRepository) {
        this.skillRepository = skillRepository;
    }
    
    @Override
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }
    
    @Override
    public List<Skill> getSkillsPaginated(int page, int size) {
        validatePaginationParams(page, size);
        return skillRepository.findPaginated(page, size);
    }
    
    @Override
    public Optional<Skill> getSkillById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Skill ID cannot be null");
        }
        return skillRepository.findById(id);
    }
    
    @Override
    public List<Skill> getSkillsByCategory(SkillCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("Skill category cannot be null");
        }
        return skillRepository.findByCategory(category);
    }
    
    @Override
    public List<Skill> getSkillsByLevel(SkillLevel level) {
        if (level == null) {
            throw new IllegalArgumentException("Skill level cannot be null");
        }
        return skillRepository.findByLevel(level);
    }
    
    @Override
    public List<Skill> getFeaturedSkills() {
        return skillRepository.findByFeaturedTrue();
    }
    
    @Override
    public List<Skill> getSkillsWithMinimumExperience(Integer minYears) {
        if (minYears == null || minYears < 0) {
            throw new IllegalArgumentException("Minimum years of experience must be non-negative");
        }
        return skillRepository.findByYearsOfExperienceGreaterThanEqual(minYears);
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