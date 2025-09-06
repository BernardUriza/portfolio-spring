package com.portfolio.core.application.usecase;

import com.portfolio.core.domain.experience.Experience;
import com.portfolio.core.domain.experience.ExperienceType;
import com.portfolio.core.port.in.CreateExperienceUseCase;
import com.portfolio.core.port.out.ExperienceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class CreateExperienceUseCaseImpl implements CreateExperienceUseCase {
    
    private final ExperienceRepositoryPort experienceRepository;
    
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