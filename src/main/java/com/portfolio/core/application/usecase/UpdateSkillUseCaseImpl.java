package com.portfolio.core.application.usecase;

import com.portfolio.core.domain.skill.Skill;
import com.portfolio.core.domain.skill.SkillCategory;
import com.portfolio.core.domain.skill.SkillLevel;
import com.portfolio.core.port.in.UpdateSkillUseCase;
import com.portfolio.core.port.out.SkillRepositoryPort;
import org.springframework.stereotype.Component;

/**
 * Creado por Bernard Orozco
 */
@Component
public class UpdateSkillUseCaseImpl implements UpdateSkillUseCase {

    private final SkillRepositoryPort skillRepository;

    public UpdateSkillUseCaseImpl(SkillRepositoryPort skillRepository) {
        this.skillRepository = skillRepository;
    }
    
    @Override
    public Skill updateSkill(Long id, String name, String description, SkillCategory category, SkillLevel level) {
        Skill existingSkill = findSkillOrThrow(id);
        
        validateNameUniqueness(name, id);
        
        Skill updatedSkill = existingSkill.updateInfo(name, description, category, level);
        return skillRepository.save(updatedSkill);
    }
    
    @Override
    public Skill updateSkillExperience(Long id, Integer yearsOfExperience) {
        Skill existingSkill = findSkillOrThrow(id);
        
        Skill updatedSkill = existingSkill.updateExperience(yearsOfExperience);
        return skillRepository.save(updatedSkill);
    }
    
    @Override
    public Skill setSkillFeatured(Long id, Boolean featured) {
        Skill existingSkill = findSkillOrThrow(id);
        
        Skill updatedSkill = existingSkill.setFeatured(featured);
        return skillRepository.save(updatedSkill);
    }
    
    @Override
    public Skill updateSkillUrls(Long id, String iconUrl, String documentationUrl) {
        Skill existingSkill = findSkillOrThrow(id);
        
        Skill updatedSkill = existingSkill.updateUrls(iconUrl, documentationUrl);
        return skillRepository.save(updatedSkill);
    }
    
    @Override
    public void deleteSkill(Long id) {
        Skill existingSkill = findSkillOrThrow(id);
        skillRepository.delete(existingSkill);
    }
    
    private Skill findSkillOrThrow(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Skill ID cannot be null");
        }
        return skillRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found with ID: " + id));
    }
    
    private void validateNameUniqueness(String name, Long currentSkillId) {
        // Only check uniqueness if name is being changed
        skillRepository.findAll().stream()
                .filter(skill -> skill.getName().equalsIgnoreCase(name))
                .filter(skill -> !skill.getId().equals(currentSkillId))
                .findFirst()
                .ifPresent(skill -> {
                    throw new IllegalArgumentException("Skill with name '" + name + "' already exists");
                });
    }
}