package com.portfolio.core.port.out;

import com.portfolio.core.domain.skill.Skill;
import com.portfolio.core.domain.skill.SkillCategory;
import com.portfolio.core.domain.skill.SkillLevel;

import java.util.List;
import java.util.Optional;

public interface SkillRepositoryPort {
    Skill save(Skill skill);
    Optional<Skill> findById(Long id);
    List<Skill> findAll();
    List<Skill> findByCategory(SkillCategory category);
    List<Skill> findByLevel(SkillLevel level);
    List<Skill> findByFeaturedTrue();
    List<Skill> findByYearsOfExperienceGreaterThanEqual(Integer minYears);
    List<Skill> findPaginated(int page, int size);
    boolean existsByName(String name);
    void delete(Skill skill);
    void deleteById(Long id);
}