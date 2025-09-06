package com.portfolio.core.port.out;

import com.portfolio.core.domain.experience.Experience;
import com.portfolio.core.domain.experience.ExperienceType;

import java.util.List;
import java.util.Optional;

public interface ExperienceRepositoryPort {
    Experience save(Experience experience);
    Optional<Experience> findById(Long id);
    List<Experience> findAll();
    List<Experience> findByType(ExperienceType type);
    List<Experience> findByIsCurrentPositionTrue();
    List<Experience> findByCompanyNameIgnoreCase(String companyName);
    List<Experience> findLongTermExperiences();
    List<Experience> findOrderedByStartDateDesc();
    List<Experience> findPaginated(int page, int size);
    void delete(Experience experience);
    void deleteById(Long id);
}