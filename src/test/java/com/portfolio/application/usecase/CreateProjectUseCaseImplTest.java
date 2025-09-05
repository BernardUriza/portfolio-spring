package com.portfolio.application.usecase;

import com.portfolio.core.domain.project.Project;
import com.portfolio.core.port.in.CreateProjectUseCase;
import com.portfolio.core.port.out.ProjectRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Create Project Use Case Tests")
class CreateProjectUseCaseImplTest {

    @Mock
    private ProjectRepositoryPort projectRepository;

    @InjectMocks
    private CreateProjectUseCaseImpl createProjectUseCase;

    @Test
    @DisplayName("Should create project successfully")
    void shouldCreateProjectSuccessfully() {
        // Given
        CreateProjectUseCase.CreateProjectCommand command = new CreateProjectUseCase.CreateProjectCommand(
                "Test Project",
                "A test project description",
                "https://example.com",
                "https://github.com/user/repo",
                LocalDate.now(),
                Arrays.asList("Java", "Spring Boot")
        );

        Project mockSavedProject = Project.builder()
                .id(1L)
                .title(command.title())
                .description(command.description())
                .link(command.link())
                .githubRepo(command.githubRepo())
                .createdDate(command.createdDate())
                .mainTechnologies(command.mainTechnologies())
                .build();

        when(projectRepository.save(any(Project.class))).thenReturn(mockSavedProject);

        // When
        Project result = createProjectUseCase.createProject(command);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(command.title(), result.getTitle());
        assertEquals(command.description(), result.getDescription());
        assertEquals(command.link(), result.getLink());
        assertEquals(command.githubRepo(), result.getGithubRepo());
        assertEquals(command.createdDate(), result.getCreatedDate());
        assertEquals(command.mainTechnologies(), result.getMainTechnologies());

        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    @DisplayName("Should throw exception when title is invalid")
    void shouldThrowExceptionWhenTitleIsInvalid() {
        // Given
        CreateProjectUseCase.CreateProjectCommand command = new CreateProjectUseCase.CreateProjectCommand(
                null, // Invalid title
                "A test project description",
                "https://example.com",
                "https://github.com/user/repo",
                LocalDate.now(),
                Arrays.asList("Java", "Spring Boot")
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            createProjectUseCase.createProject(command);
        });

        assertEquals("Project title cannot be null or empty", exception.getMessage());
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Should throw exception when description is invalid")
    void shouldThrowExceptionWhenDescriptionIsInvalid() {
        // Given
        CreateProjectUseCase.CreateProjectCommand command = new CreateProjectUseCase.CreateProjectCommand(
                "Valid Title",
                "", // Invalid description
                "https://example.com",
                "https://github.com/user/repo",
                LocalDate.now(),
                Arrays.asList("Java", "Spring Boot")
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            createProjectUseCase.createProject(command);
        });

        assertEquals("Project description cannot be null or empty", exception.getMessage());
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Should create project with minimal required fields")
    void shouldCreateProjectWithMinimalRequiredFields() {
        // Given
        CreateProjectUseCase.CreateProjectCommand command = new CreateProjectUseCase.CreateProjectCommand(
                "Minimal Project",
                "Basic description",
                null, // Optional
                null, // Optional
                LocalDate.now(),
                null  // Optional
        );

        Project mockSavedProject = Project.create(
                command.title(),
                command.description(),
                command.link(),
                command.githubRepo(),
                command.createdDate(),
                command.mainTechnologies()
        ).toBuilder().id(1L).build();

        when(projectRepository.save(any(Project.class))).thenReturn(mockSavedProject);

        // When
        Project result = createProjectUseCase.createProject(command);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(command.title(), result.getTitle());
        assertEquals(command.description(), result.getDescription());
        assertNull(result.getLink());
        assertNull(result.getGithubRepo());
        assertNotNull(result.getMainTechnologies());
        assertTrue(result.getMainTechnologies().isEmpty());

        verify(projectRepository, times(1)).save(any(Project.class));
    }
}