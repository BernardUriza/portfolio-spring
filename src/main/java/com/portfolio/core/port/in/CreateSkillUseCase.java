package com.portfolio.core.port.in;

import com.portfolio.core.domain.skill.Skill;
import com.portfolio.core.domain.skill.SkillCategory;
import com.portfolio.core.domain.skill.SkillLevel;

public interface CreateSkillUseCase {
    Skill createSkill(String name, String description, SkillCategory category, SkillLevel level);
    Skill createSkillWithExperience(String name, String description, SkillCategory category, 
                                   SkillLevel level, Integer yearsOfExperience, Boolean isFeatured);
}