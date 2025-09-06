package com.portfolio.core.port.in;

import com.portfolio.core.domain.skill.Skill;
import com.portfolio.core.domain.skill.SkillCategory;
import com.portfolio.core.domain.skill.SkillLevel;

import java.util.List;
import java.util.Optional;

public interface GetSkillsUseCase {
    List<Skill> getAllSkills();
    List<Skill> getSkillsPaginated(int page, int size);
    Optional<Skill> getSkillById(Long id);
    List<Skill> getSkillsByCategory(SkillCategory category);
    List<Skill> getSkillsByLevel(SkillLevel level);
    List<Skill> getFeaturedSkills();
    List<Skill> getSkillsWithMinimumExperience(Integer minYears);
}