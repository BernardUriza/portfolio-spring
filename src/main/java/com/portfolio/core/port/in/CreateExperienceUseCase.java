package com.portfolio.core.port.in;

import com.portfolio.core.domain.experience.Experience;
import com.portfolio.core.domain.experience.ExperienceType;

import java.time.LocalDate;

public interface CreateExperienceUseCase {
    Experience createExperience(String jobTitle, String companyName, ExperienceType type, 
                              String description, LocalDate startDate);
    Experience createCompletedExperience(String jobTitle, String companyName, ExperienceType type, 
                                       String description, LocalDate startDate, LocalDate endDate);
}