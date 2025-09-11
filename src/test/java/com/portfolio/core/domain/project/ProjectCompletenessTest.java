package com.portfolio.core.domain.project;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProjectCompletenessTest {
    
    @Test
    void calculate_AllFieldsComplete_ReturnsFullScore() {
        // Given
        PortfolioProject project = PortfolioProject.builder()
                .id(1L)
                .title("Test Project")
                .description("A comprehensive test project description")
                .link("https://example.com")
                .githubRepo("https://github.com/user/repo")
                .createdDate(LocalDate.now())
                .mainTechnologies(List.of("Java", "Spring"))
                .skillIds(Set.of(1L, 2L))
                .experienceIds(Set.of(1L))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // When
        ProjectCompleteness completeness = ProjectCompleteness.calculate(project);
        
        // Then
        assertEquals(100, completeness.getScore());
        assertTrue(completeness.getMissing().isEmpty());
    }
    
    @Test
    void calculate_NoFields_ReturnsZeroScore() {
        // Given
        PortfolioProject project = PortfolioProject.builder()
                .id(1L)
                .title("Test Project")
                .description("")  // Empty description
                .link(null)       // No link
                .githubRepo("https://github.com/user/repo")
                .createdDate(LocalDate.now())
                .mainTechnologies(List.of("Java"))
                .skillIds(Set.of())      // No skills
                .experienceIds(Set.of()) // No experiences
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // When
        ProjectCompleteness completeness = ProjectCompleteness.calculate(project);
        
        // Then
        assertEquals(0, completeness.getScore());
        assertEquals(4, completeness.getMissing().size());
        assertTrue(completeness.getMissing().contains("Description"));
        assertTrue(completeness.getMissing().contains("Live Demo"));
        assertTrue(completeness.getMissing().contains("Skills"));
        assertTrue(completeness.getMissing().contains("Experiences"));
    }
    
    @Test
    void calculate_PartialFields_ReturnsPartialScore() {
        // Given - Only description and skills are complete
        PortfolioProject project = PortfolioProject.builder()
                .id(1L)
                .title("Test Project")
                .description("A test project description")
                .link(null)  // No link
                .githubRepo("https://github.com/user/repo")
                .createdDate(LocalDate.now())
                .mainTechnologies(List.of("Java"))
                .skillIds(Set.of(1L))    // Has skills
                .experienceIds(Set.of()) // No experiences
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // When
        ProjectCompleteness completeness = ProjectCompleteness.calculate(project);
        
        // Then
        assertEquals(60, completeness.getScore()); // 40% (description) + 20% (skills)
        assertEquals(2, completeness.getMissing().size());
        assertTrue(completeness.getMissing().contains("Live Demo"));
        assertTrue(completeness.getMissing().contains("Experiences"));
    }
    
    @Test
    void calculate_WeightedScoring_CorrectWeights() {
        // Test individual field weights
        
        // Description only (40%)
        PortfolioProject descOnly = createBaseProject().toBuilder()
                .description("Complete description")
                .build();
        assertEquals(40, ProjectCompleteness.calculate(descOnly).getScore());
        
        // Live Demo only (20%)
        PortfolioProject linkOnly = createBaseProject().toBuilder()
                .link("https://example.com")
                .build();
        assertEquals(20, ProjectCompleteness.calculate(linkOnly).getScore());
        
        // Skills only (20%)
        PortfolioProject skillsOnly = createBaseProject().toBuilder()
                .skillIds(Set.of(1L))
                .build();
        assertEquals(20, ProjectCompleteness.calculate(skillsOnly).getScore());
        
        // Experiences only (20%)
        PortfolioProject expOnly = createBaseProject().toBuilder()
                .experienceIds(Set.of(1L))
                .build();
        assertEquals(20, ProjectCompleteness.calculate(expOnly).getScore());
    }
    
    private PortfolioProject createBaseProject() {
        return PortfolioProject.builder()
                .id(1L)
                .title("Test Project")
                .description("")  // Empty by default
                .link(null)       // Null by default
                .githubRepo("https://github.com/user/repo")
                .createdDate(LocalDate.now())
                .mainTechnologies(List.of("Java"))
                .skillIds(Set.of())      // Empty by default
                .experienceIds(Set.of()) // Empty by default
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}