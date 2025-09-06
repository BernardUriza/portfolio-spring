package com.portfolio.core.application.usecase;

import com.portfolio.core.domain.experience.Experience;
import com.portfolio.core.domain.experience.ExperienceType;
import com.portfolio.core.port.in.UpdateExperienceUseCase;
import com.portfolio.core.port.out.ExperienceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class UpdateExperienceUseCaseImpl implements UpdateExperienceUseCase {
    
    private final ExperienceRepositoryPort experienceRepository;
    
    @Override
    public Experience updateExperience(Long id, String jobTitle, String companyName, 
                                     ExperienceType type, String description) {
        Experience existingExperience = findExperienceOrThrow(id);
        
        Experience updatedExperience = existingExperience.updateBasicInfo(jobTitle, companyName, type, description);
        return experienceRepository.save(updatedExperience);
    }
    
    @Override
    public Experience updateExperienceDates(Long id, LocalDate startDate, LocalDate endDate) {
        Experience existingExperience = findExperienceOrThrow(id);
        
        Experience updatedExperience = existingExperience.updateDates(startDate, endDate);
        return experienceRepository.save(updatedExperience);
    }
    
    @Override
    public Experience endExperience(Long id, LocalDate endDate) {
        Experience existingExperience = findExperienceOrThrow(id);
        
        Experience updatedExperience = existingExperience.endPosition(endDate);
        return experienceRepository.save(updatedExperience);
    }
    
    @Override
    public Experience updateCompanyDetails(Long id, String companyUrl, String location, String companyLogoUrl) {
        Experience existingExperience = findExperienceOrThrow(id);
        
        Experience updatedExperience = existingExperience.updateCompanyDetails(companyUrl, location, companyLogoUrl);
        return experienceRepository.save(updatedExperience);
    }
    
    @Override
    public Experience addAchievement(Long id, String achievement) {
        Experience existingExperience = findExperienceOrThrow(id);
        
        Experience updatedExperience = existingExperience.addAchievement(achievement);
        return experienceRepository.save(updatedExperience);
    }
    
    @Override
    public Experience removeAchievement(Long id, String achievement) {
        Experience existingExperience = findExperienceOrThrow(id);
        
        Experience updatedExperience = existingExperience.removeAchievement(achievement);
        return experienceRepository.save(updatedExperience);
    }
    
    @Override
    public Experience addTechnology(Long id, String technology) {
        Experience existingExperience = findExperienceOrThrow(id);
        
        Experience updatedExperience = existingExperience.addTechnology(technology);
        return experienceRepository.save(updatedExperience);
    }
    
    @Override
    public void deleteExperience(Long id) {
        Experience existingExperience = findExperienceOrThrow(id);
        experienceRepository.delete(existingExperience);
    }
    
    private Experience findExperienceOrThrow(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Experience ID cannot be null");
        }
        return experienceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Experience not found with ID: " + id));
    }
}