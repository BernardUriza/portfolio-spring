package com.portfolio.core.application.usecase;

import com.portfolio.core.domain.experience.Experience;
import com.portfolio.core.domain.experience.ExperienceType;
import com.portfolio.core.port.in.CreateExperienceUseCase;
import com.portfolio.core.port.out.ExperienceRepositoryPort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Creado por Bernard Orozco
 */
@Component
public class CreateExperienceUseCaseImpl implements CreateExperienceUseCase {

    private final ExperienceRepositoryPort experienceRepository;

    public CreateExperienceUseCaseImpl(ExperienceRepositoryPort experienceRepository) {
        this.experienceRepository = experienceRepository;
    }
    
    @Override
    public Experience createExperience(String jobTitle, String companyName, ExperienceType type, 
                                     String description, LocalDate startDate) {
        Experience experience = Experience.create(jobTitle, companyName, type, description, startDate);
        return experienceRepository.save(experience);
    }
    
    @Override
    public Experience createCompletedExperience(String jobTitle, String companyName, ExperienceType type, 
                                              String description, LocalDate startDate, LocalDate endDate) {
        Experience experience = Experience.create(jobTitle, companyName, type, description, startDate)
                .updateDates(startDate, endDate);
        
        return experienceRepository.save(experience);
    }
}