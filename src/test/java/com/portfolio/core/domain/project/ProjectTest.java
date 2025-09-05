package com.portfolio.core.domain.project;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Project Domain Entity Tests")
class ProjectTest {

    @Test
    @DisplayName("Should create project with valid data")
    void shouldCreateProjectWithValidData() {
        // Given
        String title = "Test Project";
        String description = "A test project description";
        String link = "https://example.com";
        String githubRepo = "https://github.com/user/repo";
        LocalDate createdDate = LocalDate.now();
        List<String> technologies = Arrays.asList("Java", "Spring Boot");

        // When
        Project project = Project.create(title, description, link, githubRepo, createdDate, technologies);

        // Then
        assertNotNull(project);
        assertEquals(title, project.getTitle());
        assertEquals(description, project.getDescription());
        assertEquals(link, project.getLink());
        assertEquals(githubRepo, project.getGithubRepo());
        assertEquals(createdDate, project.getCreatedDate());
        assertEquals(technologies, project.getMainTechnologies());
        assertEquals(ProjectStatus.ACTIVE, project.getStatus());
        assertEquals(ProjectType.PERSONAL, project.getType());
        assertTrue(project.isActive());
        assertFalse(project.isCompleted());
        assertTrue(project.hasGithubRepo());
        assertTrue(project.hasExternalLink());
    }

    @Test
    @DisplayName("Should throw exception when title is null")
    void shouldThrowExceptionWhenTitleIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Project.create(null, "Description", "https://example.com", "https://github.com", LocalDate.now(), List.of());
        });
        
        assertEquals("Project title cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when title is empty")
    void shouldThrowExceptionWhenTitleIsEmpty() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Project.create("", "Description", "https://example.com", "https://github.com", LocalDate.now(), List.of());
        });
        
        assertEquals("Project title cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when title exceeds 200 characters")
    void shouldThrowExceptionWhenTitleExceedsLimit() {
        // Given
        String longTitle = "a".repeat(201);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Project.create(longTitle, "Description", "https://example.com", "https://github.com", LocalDate.now(), List.of());
        });
        
        assertEquals("Project title cannot exceed 200 characters", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when description is null")
    void shouldThrowExceptionWhenDescriptionIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Project.create("Title", null, "https://example.com", "https://github.com", LocalDate.now(), List.of());
        });
        
        assertEquals("Project description cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should update basic info successfully")
    void shouldUpdateBasicInfoSuccessfully() throws InterruptedException {
        // Given
        Project originalProject = Project.create("Original Title", "Original Description", 
                "https://original.com", "https://github.com/original", LocalDate.now(), List.of("Java"));
        
        String newTitle = "Updated Title";
        String newDescription = "Updated Description";
        String newLink = "https://updated.com";
        String newGithubRepo = "https://github.com/updated";

        // Wait a small amount to ensure different timestamps
        Thread.sleep(10);

        // When
        Project updatedProject = originalProject.updateBasicInfo(newTitle, newDescription, newLink, newGithubRepo);

        // Then
        assertEquals(newTitle, updatedProject.getTitle());
        assertEquals(newDescription, updatedProject.getDescription());
        assertEquals(newLink, updatedProject.getLink());
        assertEquals(newGithubRepo, updatedProject.getGithubRepo());
        assertTrue(updatedProject.getUpdatedAt().isAfter(originalProject.getUpdatedAt()));
    }

    @Test
    @DisplayName("Should add skill successfully")
    void shouldAddSkillSuccessfully() {
        // Given
        Project project = Project.create("Title", "Description", null, null, LocalDate.now(), List.of());
        Long skillId = 1L;

        // When
        Project updatedProject = project.addSkill(skillId);

        // Then
        assertTrue(updatedProject.getSkillIds().contains(skillId));
        assertEquals(1, updatedProject.getSkillIds().size());
    }

    @Test
    @DisplayName("Should throw exception when adding null skill")
    void shouldThrowExceptionWhenAddingNullSkill() {
        // Given
        Project project = Project.create("Title", "Description", null, null, LocalDate.now(), List.of());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            project.addSkill(null);
        });
        
        assertEquals("Skill ID cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should change status successfully when transition is valid")
    void shouldChangeStatusSuccessfullyWhenTransitionIsValid() {
        // Given
        Project project = Project.create("Title", "Description", null, null, LocalDate.now(), List.of());
        
        // When
        Project completedProject = project.changeStatus(ProjectStatus.COMPLETED);

        // Then
        assertEquals(ProjectStatus.COMPLETED, completedProject.getStatus());
        assertTrue(completedProject.isCompleted());
        assertFalse(completedProject.isActive());
    }

    @Test
    @DisplayName("Should throw exception when changing status to null")
    void shouldThrowExceptionWhenChangingStatusToNull() {
        // Given
        Project project = Project.create("Title", "Description", null, null, LocalDate.now(), List.of());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            project.changeStatus(null);
        });
        
        assertEquals("Project status cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should detect external link correctly")
    void shouldDetectExternalLinkCorrectly() {
        // Given
        Project projectWithLink = Project.create("Title", "Description", "https://example.com", null, LocalDate.now(), List.of());
        Project projectWithoutLink = Project.create("Title", "Description", null, null, LocalDate.now(), List.of());
        Project projectWithEmptyLink = Project.create("Title", "Description", "", null, LocalDate.now(), List.of());

        // Then
        assertTrue(projectWithLink.hasExternalLink());
        assertFalse(projectWithoutLink.hasExternalLink());
        assertFalse(projectWithEmptyLink.hasExternalLink());
    }

    @Test
    @DisplayName("Should detect GitHub repo correctly")
    void shouldDetectGitHubRepoCorrectly() {
        // Given
        Project projectWithRepo = Project.create("Title", "Description", null, "https://github.com/user/repo", LocalDate.now(), List.of());
        Project projectWithoutRepo = Project.create("Title", "Description", null, null, LocalDate.now(), List.of());
        Project projectWithEmptyRepo = Project.create("Title", "Description", null, "   ", LocalDate.now(), List.of());

        // Then
        assertTrue(projectWithRepo.hasGithubRepo());
        assertFalse(projectWithoutRepo.hasGithubRepo());
        assertFalse(projectWithEmptyRepo.hasGithubRepo());
    }
}