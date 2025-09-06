package com.portfolio.core.port.in;

import com.portfolio.core.domain.experience.Experience;
import com.portfolio.core.domain.experience.ExperienceType;

import java.time.LocalDate;

public interface UpdateExperienceUseCase {
    Experience updateExperience(Long id, String jobTitle, String companyName, ExperienceType type, String description);
    Experience updateExperienceDates(Long id, LocalDate startDate, LocalDate endDate);
    Experience endExperience(Long id, LocalDate endDate);
    Experience updateCompanyDetails(Long id, String companyUrl, String location, String companyLogoUrl);
    Experience addAchievement(Long id, String achievement);
    Experience removeAchievement(Long id, String achievement);
    Experience addTechnology(Long id, String technology);
    void deleteExperience(Long id);
}