package com.portfolio.core.port.in;

import com.portfolio.core.domain.skill.Skill;
import com.portfolio.core.domain.skill.SkillCategory;
import com.portfolio.core.domain.skill.SkillLevel;

public interface UpdateSkillUseCase {
    Skill updateSkill(Long id, String name, String description, SkillCategory category, SkillLevel level);
    Skill updateSkillExperience(Long id, Integer yearsOfExperience);
    Skill setSkillFeatured(Long id, Boolean featured);
    Skill updateSkillUrls(Long id, String iconUrl, String documentationUrl);
    void deleteSkill(Long id);
}