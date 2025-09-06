package com.portfolio.core.application.usecase;

import com.portfolio.core.domain.skill.Skill;
import com.portfolio.core.domain.skill.SkillCategory;
import com.portfolio.core.domain.skill.SkillLevel;
import com.portfolio.core.port.in.CreateSkillUseCase;
import com.portfolio.core.port.out.SkillRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateSkillUseCaseImpl implements CreateSkillUseCase {
    
    private final SkillRepositoryPort skillRepository;
    
    @Override
    public Skill createSkill(String name, String description, SkillCategory category, SkillLevel level) {
        validateSkillDoesNotExist(name);
        
        Skill skill = Skill.create(name, description, category, level);
        return skillRepository.save(skill);
    }
    
    @Override
    public Skill createSkillWithExperience(String name, String description, SkillCategory category, 
                                          SkillLevel level, Integer yearsOfExperience, Boolean isFeatured) {
        validateSkillDoesNotExist(name);
        
        Skill skill = Skill.create(name, description, category, level)
                .updateExperience(yearsOfExperience)
                .setFeatured(isFeatured);
                
        return skillRepository.save(skill);
    }
    
    private void validateSkillDoesNotExist(String name) {
        if (skillRepository.existsByName(name)) {
            throw new IllegalArgumentException("Skill with name '" + name + "' already exists");
        }
    }
}