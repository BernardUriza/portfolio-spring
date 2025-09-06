package com.portfolio.core.port.in;

import com.portfolio.core.domain.experience.Experience;
import com.portfolio.core.domain.experience.ExperienceType;

import java.util.List;
import java.util.Optional;

public interface GetExperiencesUseCase {
    List<Experience> getAllExperiences();
    List<Experience> getExperiencesPaginated(int page, int size);
    Optional<Experience> getExperienceById(Long id);
    List<Experience> getExperiencesByType(ExperienceType type);
    List<Experience> getCurrentExperiences();
    List<Experience> getExperiencesByCompany(String companyName);
    List<Experience> getLongTermExperiences();
    List<Experience> getExperiencesOrderedByStartDate();
}