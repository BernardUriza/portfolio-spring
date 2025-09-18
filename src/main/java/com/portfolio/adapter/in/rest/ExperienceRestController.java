package com.portfolio.adapter.in.rest;

import com.portfolio.adapter.in.rest.dto.ExperienceRestDto;
import com.portfolio.adapter.in.rest.mapper.ExperienceRestMapper;
import com.portfolio.core.domain.experience.Experience;
import com.portfolio.core.domain.experience.ExperienceType;
import com.portfolio.core.port.in.CreateExperienceUseCase;
import com.portfolio.core.port.in.GetExperiencesUseCase;
import com.portfolio.core.port.in.UpdateExperienceUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/experiences")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class ExperienceRestController {

    private final CreateExperienceUseCase createExperienceUseCase;
    private final GetExperiencesUseCase getExperiencesUseCase;
    private final UpdateExperienceUseCase updateExperienceUseCase;
    private final ExperienceRestMapper mapper;

    public ExperienceRestController(CreateExperienceUseCase createExperienceUseCase,
                                    GetExperiencesUseCase getExperiencesUseCase,
                                    UpdateExperienceUseCase updateExperienceUseCase,
                                    ExperienceRestMapper mapper) {
        this.createExperienceUseCase = createExperienceUseCase;
        this.getExperiencesUseCase = getExperiencesUseCase;
        this.updateExperienceUseCase = updateExperienceUseCase;
        this.mapper = mapper;
    }
    
    @PostMapping
    public ResponseEntity<ExperienceRestDto> createExperience(@Valid @RequestBody ExperienceRestDto experienceDto) {
        Experience createdExperience;
        
        if (experienceDto.getEndDate() != null) {
            createdExperience = createExperienceUseCase.createCompletedExperience(
                    experienceDto.getJobTitle(),
                    experienceDto.getCompanyName(),
                    experienceDto.getType(),
                    experienceDto.getDescription(),
                    experienceDto.getStartDate(),
                    experienceDto.getEndDate()
            );
        } else {
            createdExperience = createExperienceUseCase.createExperience(
                    experienceDto.getJobTitle(),
                    experienceDto.getCompanyName(),
                    experienceDto.getType(),
                    experienceDto.getDescription(),
                    experienceDto.getStartDate()
            );
        }
        
        ExperienceRestDto responseDto = mapper.toRestDto(createdExperience);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
    
    @GetMapping
    public ResponseEntity<List<ExperienceRestDto>> getAllExperiences() {
        List<Experience> experiences = getExperiencesUseCase.getAllExperiences();
        List<ExperienceRestDto> responseDtos = experiences.stream()
                .map(mapper::toRestDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responseDtos);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ExperienceRestDto> getExperienceById(@PathVariable Long id) {
        return getExperiencesUseCase.getExperienceById(id)
                .map(experience -> ResponseEntity.ok(mapper.toRestDto(experience)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<List<ExperienceRestDto>> getExperiencesByType(@PathVariable ExperienceType type) {
        List<Experience> experiences = getExperiencesUseCase.getExperiencesByType(type);
        List<ExperienceRestDto> responseDtos = experiences.stream()
                .map(mapper::toRestDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responseDtos);
    }
    
    @GetMapping("/current")
    public ResponseEntity<List<ExperienceRestDto>> getCurrentExperiences() {
        List<Experience> experiences = getExperiencesUseCase.getCurrentExperiences();
        List<ExperienceRestDto> responseDtos = experiences.stream()
                .map(mapper::toRestDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responseDtos);
    }
    
    @GetMapping("/company/{companyName}")
    public ResponseEntity<List<ExperienceRestDto>> getExperiencesByCompany(@PathVariable String companyName) {
        List<Experience> experiences = getExperiencesUseCase.getExperiencesByCompany(companyName);
        List<ExperienceRestDto> responseDtos = experiences.stream()
                .map(mapper::toRestDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responseDtos);
    }
    
    @GetMapping("/long-term")
    public ResponseEntity<List<ExperienceRestDto>> getLongTermExperiences() {
        List<Experience> experiences = getExperiencesUseCase.getLongTermExperiences();
        List<ExperienceRestDto> responseDtos = experiences.stream()
                .map(mapper::toRestDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responseDtos);
    }
    
    @GetMapping("/ordered")
    public ResponseEntity<List<ExperienceRestDto>> getOrderedExperiences() {
        List<Experience> experiences = getExperiencesUseCase.getExperiencesOrderedByStartDate();
        List<ExperienceRestDto> responseDtos = experiences.stream()
                .map(mapper::toRestDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responseDtos);
    }
    
    @GetMapping("/paginated")
    public ResponseEntity<List<ExperienceRestDto>> getPaginatedExperiences(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Experience> experiences = getExperiencesUseCase.getExperiencesPaginated(page, size);
        List<ExperienceRestDto> responseDtos = experiences.stream()
                .map(mapper::toRestDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responseDtos);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ExperienceRestDto> updateExperience(
            @PathVariable Long id, 
            @Valid @RequestBody ExperienceRestDto experienceDto) {
        
        Experience updatedExperience = updateExperienceUseCase.updateExperience(
                id,
                experienceDto.getJobTitle(),
                experienceDto.getCompanyName(),
                experienceDto.getType(),
                experienceDto.getDescription()
        );
        
        ExperienceRestDto responseDto = mapper.toRestDto(updatedExperience);
        return ResponseEntity.ok(responseDto);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExperience(@PathVariable Long id) {
        updateExperienceUseCase.deleteExperience(id);
        return ResponseEntity.noContent().build();
    }
}
