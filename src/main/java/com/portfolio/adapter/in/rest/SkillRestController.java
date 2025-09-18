package com.portfolio.adapter.in.rest;

import com.portfolio.adapter.in.rest.dto.MessageResponse;
import com.portfolio.adapter.in.rest.dto.SkillRestDto;
import com.portfolio.adapter.in.rest.mapper.SkillRestMapper;
import com.portfolio.core.domain.skill.Skill;
import com.portfolio.core.domain.skill.SkillCategory;
import com.portfolio.core.domain.skill.SkillLevel;
import com.portfolio.core.port.in.CreateSkillUseCase;
import com.portfolio.core.port.in.GetSkillsUseCase;
import com.portfolio.core.port.in.UpdateSkillUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/skills")
@Tag(name = "Skills API", description = "Clean hexagonal architecture skills management")
public class SkillRestController {
    private static final Logger log = LoggerFactory.getLogger(SkillRestController.class);

    private final CreateSkillUseCase createSkillUseCase;
    private final GetSkillsUseCase getSkillsUseCase;
    private final UpdateSkillUseCase updateSkillUseCase;
    private final SkillRestMapper restMapper;

    public SkillRestController(CreateSkillUseCase createSkillUseCase,
                               GetSkillsUseCase getSkillsUseCase,
                               UpdateSkillUseCase updateSkillUseCase,
                               SkillRestMapper restMapper) {
        this.createSkillUseCase = createSkillUseCase;
        this.getSkillsUseCase = getSkillsUseCase;
        this.updateSkillUseCase = updateSkillUseCase;
        this.restMapper = restMapper;
    }
    
    @GetMapping
    @Operation(summary = "Get all skills with pagination")
    public ResponseEntity<PageImpl<SkillRestDto>> getAllSkills(Pageable pageable) {
        log.info("Getting skills with pagination: page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        List<Skill> skills = getSkillsUseCase.getSkillsPaginated(
                pageable.getPageNumber(), 
                pageable.getPageSize()
        );
        
        List<SkillRestDto> skillDtos = skills.stream()
                .map(restMapper::toRestDto)
                .collect(Collectors.toList());
        
        PageImpl<SkillRestDto> pageResponse = new PageImpl<>(
                skillDtos,
                pageable,
                skillDtos.size()
        );
        
        return ResponseEntity.ok(pageResponse);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get skill by ID")
    public ResponseEntity<SkillRestDto> getSkillById(@PathVariable Long id) {
        log.info("Getting skill by ID: {}", id);
        
        return getSkillsUseCase.getSkillById(id)
                .map(restMapper::toRestDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/category/{category}")
    @Operation(summary = "Get skills by category")
    public ResponseEntity<List<SkillRestDto>> getSkillsByCategory(@PathVariable SkillCategory category) {
        log.info("Getting skills by category: {}", category);
        
        List<SkillRestDto> skills = getSkillsUseCase.getSkillsByCategory(category)
                .stream()
                .map(restMapper::toRestDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(skills);
    }
    
    @GetMapping("/level/{level}")
    @Operation(summary = "Get skills by level")
    public ResponseEntity<List<SkillRestDto>> getSkillsByLevel(@PathVariable SkillLevel level) {
        log.info("Getting skills by level: {}", level);
        
        List<SkillRestDto> skills = getSkillsUseCase.getSkillsByLevel(level)
                .stream()
                .map(restMapper::toRestDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(skills);
    }
    
    @GetMapping("/featured")
    @Operation(summary = "Get featured skills")
    public ResponseEntity<List<SkillRestDto>> getFeaturedSkills() {
        log.info("Getting featured skills");
        
        List<SkillRestDto> skills = getSkillsUseCase.getFeaturedSkills()
                .stream()
                .map(restMapper::toRestDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(skills);
    }
    
    @GetMapping("/experience/{minYears}")
    @Operation(summary = "Get skills with minimum years of experience")
    public ResponseEntity<List<SkillRestDto>> getSkillsWithMinimumExperience(@PathVariable Integer minYears) {
        log.info("Getting skills with minimum {} years of experience", minYears);
        
        List<SkillRestDto> skills = getSkillsUseCase.getSkillsWithMinimumExperience(minYears)
                .stream()
                .map(restMapper::toRestDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(skills);
    }
    
    @PostMapping
    @Operation(summary = "Create a new skill")
    public ResponseEntity<SkillRestDto> createSkill(@Valid @RequestBody SkillRestDto skillDto) {
        log.info("Creating new skill: {}", skillDto.getName());
        
        Skill createdSkill = createSkillUseCase.createSkillWithExperience(
                skillDto.getName(),
                skillDto.getDescription(),
                skillDto.getCategory(),
                skillDto.getLevel(),
                skillDto.getYearsOfExperience(),
                skillDto.getIsFeatured()
        );
        
        SkillRestDto responseDto = restMapper.toRestDto(createdSkill);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update skill")
    public ResponseEntity<SkillRestDto> updateSkill(@PathVariable Long id, 
                                                   @Valid @RequestBody SkillRestDto skillDto) {
        log.info("Updating skill with ID: {}", id);
        
        Skill updatedSkill = updateSkillUseCase.updateSkill(
                id,
                skillDto.getName(),
                skillDto.getDescription(),
                skillDto.getCategory(),
                skillDto.getLevel()
        );
        
        SkillRestDto responseDto = restMapper.toRestDto(updatedSkill);
        return ResponseEntity.ok(responseDto);
    }
    
    @PatchMapping("/{id}/featured")
    @Operation(summary = "Set skill featured status")
    public ResponseEntity<SkillRestDto> setSkillFeatured(@PathVariable Long id, 
                                                        @RequestParam Boolean featured) {
        log.info("Setting skill {} featured status to: {}", id, featured);
        
        Skill updatedSkill = updateSkillUseCase.setSkillFeatured(id, featured);
        SkillRestDto responseDto = restMapper.toRestDto(updatedSkill);
        return ResponseEntity.ok(responseDto);
    }
    
    @PatchMapping("/{id}/experience")
    @Operation(summary = "Update skill experience")
    public ResponseEntity<SkillRestDto> updateSkillExperience(@PathVariable Long id, 
                                                             @RequestParam Integer yearsOfExperience) {
        log.info("Updating skill {} experience to: {} years", id, yearsOfExperience);
        
        Skill updatedSkill = updateSkillUseCase.updateSkillExperience(id, yearsOfExperience);
        SkillRestDto responseDto = restMapper.toRestDto(updatedSkill);
        return ResponseEntity.ok(responseDto);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete skill")
    public ResponseEntity<MessageResponse> deleteSkill(@PathVariable Long id) {
        log.info("Deleting skill with ID: {}", id);
        
        updateSkillUseCase.deleteSkill(id);
        
        MessageResponse response = MessageResponse.builder()
                .message("Skill deleted successfully")
                .build();
        
        return ResponseEntity.ok(response);
    }
}
