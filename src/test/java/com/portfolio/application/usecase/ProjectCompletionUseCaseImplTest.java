package com.portfolio.application.usecase;

import com.portfolio.adapter.out.persistence.jpa.StarredProjectJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.StarredProjectJpaRepository;
import com.portfolio.core.domain.project.Project;
import com.portfolio.core.domain.project.ProjectCompletionStatus;
import com.portfolio.core.domain.project.ProjectPriority;
import com.portfolio.core.port.in.ProjectCompletionUseCase;
import com.portfolio.core.port.out.ProjectRepositoryPort;
import com.portfolio.service.SyncMonitorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectCompletionUseCaseImplTest {
    
    @Mock
    private ProjectRepositoryPort projectRepository;
    @Mock
    private StarredProjectJpaRepository starredProjectRepository;
    @Mock
    private SyncMonitorService syncMonitorService;
    
    private ProjectCompletionUseCaseImpl useCase;
    
    @BeforeEach
    void setUp() {
        useCase = new ProjectCompletionUseCaseImpl(
                projectRepository, starredProjectRepository, syncMonitorService);
    }
    
    @Test
    void linkProjectToRepository_ValidInput_LinksSuccessfully() {
        // Given
        Long projectId = 1L;
        Long repositoryId = 123L;
        String repositoryFullName = "user/repo";
        
        Project project = createTestProject(projectId);
        StarredProjectJpaEntity starredProject = createTestStarredProject(repositoryId, repositoryFullName);
        Project linkedProject = project.linkToRepository(repositoryId, repositoryFullName, 
                "https://github.com/" + repositoryFullName, 100, "main");
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(starredProjectRepository.findByGithubId(repositoryId)).thenReturn(Optional.of(starredProject));
        when(projectRepository.save(any(Project.class))).thenReturn(linkedProject);
        
        ProjectCompletionUseCase.LinkRepositoryCommand command = 
                new ProjectCompletionUseCase.LinkRepositoryCommand(projectId, repositoryId, repositoryFullName);
        
        // When
        Project result = useCase.linkProjectToRepository(command);
        
        // Then
        assertNotNull(result);
        assertEquals(repositoryId, result.getRepositoryId());
        assertEquals(repositoryFullName, result.getRepositoryFullName());
        assertTrue(result.isLinkedToRepository());
        
        verify(projectRepository).findById(projectId);
        verify(starredProjectRepository).findByGithubId(repositoryId);
        verify(projectRepository).save(any(Project.class));
        verify(syncMonitorService).appendLog(eq("INFO"), contains("Linked project"));
    }
    
    @Test
    void linkProjectToRepository_ProjectNotFound_ThrowsException() {
        // Given
        Long projectId = 1L;
        ProjectCompletionUseCase.LinkRepositoryCommand command = 
                new ProjectCompletionUseCase.LinkRepositoryCommand(projectId, 123L, "user/repo");
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
                useCase.linkProjectToRepository(command));
    }
    
    @Test
    void linkProjectToRepository_RepositoryNotFound_ThrowsException() {
        // Given
        Long projectId = 1L;
        Long repositoryId = 123L;
        String repositoryFullName = "user/repo";
        
        Project project = createTestProject(projectId);
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(starredProjectRepository.findByGithubId(repositoryId)).thenReturn(Optional.empty());
        when(starredProjectRepository.findByFullName(repositoryFullName)).thenReturn(Optional.empty());
        
        ProjectCompletionUseCase.LinkRepositoryCommand command = 
                new ProjectCompletionUseCase.LinkRepositoryCommand(projectId, repositoryId, repositoryFullName);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
                useCase.linkProjectToRepository(command));
    }
    
    @Test
    void protectField_ValidField_UpdatesProtection() {
        // Given
        Long projectId = 1L;
        String fieldName = "description";
        Boolean protect = true;
        
        Project project = createTestProject(projectId);
        Project protectedProject = project.protectField(fieldName, protect);
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(protectedProject);
        
        // When
        Project result = useCase.protectField(projectId, fieldName, protect);
        
        // Then
        assertTrue(result.isFieldProtected(fieldName));
        
        verify(projectRepository).findById(projectId);
        verify(projectRepository).save(any(Project.class));
        verify(syncMonitorService).appendLog(eq("INFO"), contains("Set field protection"));
    }
    
    @Test
    void changeCompletionStatus_ValidStatus_UpdatesStatus() {
        // Given
        Long projectId = 1L;
        ProjectCompletionStatus newStatus = ProjectCompletionStatus.IN_PROGRESS;
        
        Project project = createTestProject(projectId);
        Project updatedProject = project.changeCompletionStatus(newStatus);
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(updatedProject);
        
        // When
        Project result = useCase.changeCompletionStatus(projectId, newStatus);
        
        // Then
        assertEquals(newStatus, result.getCompletionStatus());
        
        verify(projectRepository).findById(projectId);
        verify(projectRepository).save(any(Project.class));
        verify(syncMonitorService).appendLog(eq("INFO"), contains("Changed completion status"));
    }
    
    @Test
    void changePriority_ValidPriority_UpdatesPriority() {
        // Given
        Long projectId = 1L;
        ProjectPriority newPriority = ProjectPriority.HIGH;
        
        Project project = createTestProject(projectId);
        Project updatedProject = project.changePriority(newPriority);
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(updatedProject);
        
        // When
        Project result = useCase.changePriority(projectId, newPriority);
        
        // Then
        assertEquals(newPriority, result.getPriority());
        
        verify(projectRepository).findById(projectId);
        verify(projectRepository).save(any(Project.class));
        verify(syncMonitorService).appendLog(eq("INFO"), contains("Changed priority"));
    }
    
    @Test
    void bulkProtectField_MultipleProjects_UpdatesAllSuccessfully() {
        // Given
        List<Long> projectIds = List.of(1L, 2L, 3L);
        String fieldName = "description";
        Boolean protect = true;
        
        Project project1 = createTestProject(1L);
        Project project2 = createTestProject(2L);
        Project project3 = createTestProject(3L);
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project1));
        when(projectRepository.findById(2L)).thenReturn(Optional.of(project2));
        when(projectRepository.findById(3L)).thenReturn(Optional.of(project3));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        useCase.bulkProtectField(projectIds, fieldName, protect);
        
        // Then
        verify(projectRepository, times(3)).findById(anyLong());
        verify(projectRepository, times(3)).save(any(Project.class));
        verify(syncMonitorService, times(3)).appendLog(eq("INFO"), contains("Set field protection"));
    }
    
    private Project createTestProject(Long id) {
        return Project.builder()
                .id(id)
                .title("Test Project " + id)
                .description("Test description")
                .link("https://example.com")
                .githubRepo("https://github.com/user/repo" + id)
                .createdDate(LocalDate.now())
                .mainTechnologies(List.of("Java", "Spring"))
                .skillIds(Set.of(1L, 2L))
                .experienceIds(Set.of(1L))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    private StarredProjectJpaEntity createTestStarredProject(Long githubId, String fullName) {
        return StarredProjectJpaEntity.builder()
                .id(1L)
                .githubId(githubId)
                .name(fullName.split("/")[1])
                .fullName(fullName)
                .description("Test repository")
                .githubRepoUrl("https://github.com/" + fullName)
                .stargazersCount(100)
                .build();
    }
}