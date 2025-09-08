package com.portfolio.service;

import com.portfolio.adapter.out.persistence.jpa.*;
import com.portfolio.core.domain.admin.ResetAudit;
import com.portfolio.core.domain.admin.ResetStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FactoryResetServiceTest {

    @Mock
    private ResetAuditJpaRepository resetAuditRepository;
    
    @Mock
    private ResetAuditJpaMapper resetAuditMapper;
    
    @Mock
    private EntityManager entityManager;
    
    @Mock
    private ProjectJpaRepository projectRepository;
    
    @Mock
    private SkillJpaRepository skillRepository;
    
    @Mock
    private ExperienceJpaRepository experienceRepository;
    
    @Mock
    private StarredProjectJpaRepository starredProjectRepository;

    @InjectMocks
    private FactoryResetService factoryResetService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(factoryResetService, "databasePlatform", "org.hibernate.dialect.H2Dialect");
        ReflectionTestUtils.setField(factoryResetService, "datasourceUrl", "jdbc:h2:mem:testdb");
        factoryResetService.init();
    }

    @Test
    void startFactoryReset_WhenNoActiveJobs_ShouldCreateNewResetAudit() {
        // Given
        String startedBy = "admin";
        String ipAddress = "192.168.1.1";
        
        when(resetAuditRepository.findActiveJobs()).thenReturn(new ArrayList<>());
        
        ResetAuditJpaEntity mockEntity = new ResetAuditJpaEntity();
        mockEntity.setId(1L);
        when(resetAuditRepository.save(any(ResetAuditJpaEntity.class))).thenReturn(mockEntity);
        
        ResetAudit mockDomain = ResetAudit.builder()
                .jobId("test-job-id")
                .startedBy(startedBy)
                .ipAddress(ipAddress)
                .status(ResetStatus.STARTED)
                .startedAt(LocalDateTime.now())
                .build();
        
        when(resetAuditMapper.toDomain(any(ResetAuditJpaEntity.class))).thenReturn(mockDomain);
        when(resetAuditMapper.toJpaEntity(any(ResetAudit.class))).thenReturn(new ResetAuditJpaEntity());

        // When
        ResetAudit result = factoryResetService.startFactoryReset(startedBy, ipAddress);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStartedBy()).isEqualTo(startedBy);
        assertThat(result.getIpAddress()).isEqualTo(ipAddress);
        assertThat(result.getStatus()).isEqualTo(ResetStatus.STARTED);
        
        verify(resetAuditRepository).findActiveJobs();
        verify(resetAuditRepository).save(any(ResetAuditJpaEntity.class));
    }

    @Test
    void startFactoryReset_WhenActiveJobExists_ShouldThrowException() {
        // Given
        String startedBy = "admin";
        String ipAddress = "192.168.1.1";
        
        ResetAuditJpaEntity activeJob = new ResetAuditJpaEntity();
        activeJob.setJobId("active-job-id");
        
        when(resetAuditRepository.findActiveJobs()).thenReturn(List.of(activeJob));
        
        ResetAudit activeDomain = ResetAudit.builder()
                .jobId("active-job-id")
                .status(ResetStatus.STARTED)
                .build();
        
        when(resetAuditMapper.toDomain(activeJob)).thenReturn(activeDomain);

        // When & Then
        assertThatThrownBy(() -> factoryResetService.startFactoryReset(startedBy, ipAddress))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Factory reset already in progress with job ID: active-job-id");
        
        verify(resetAuditRepository).findActiveJobs();
        verify(resetAuditRepository, never()).save(any());
    }

    @Test
    void getActiveJobs_ShouldReturnMappedDomainObjects() {
        // Given
        ResetAuditJpaEntity entity1 = new ResetAuditJpaEntity();
        entity1.setJobId("job1");
        
        ResetAuditJpaEntity entity2 = new ResetAuditJpaEntity();
        entity2.setJobId("job2");
        
        List<ResetAuditJpaEntity> entities = List.of(entity1, entity2);
        
        ResetAudit domain1 = ResetAudit.builder().jobId("job1").build();
        ResetAudit domain2 = ResetAudit.builder().jobId("job2").build();
        
        when(resetAuditRepository.findActiveJobs()).thenReturn(entities);
        when(resetAuditMapper.toDomain(entity1)).thenReturn(domain1);
        when(resetAuditMapper.toDomain(entity2)).thenReturn(domain2);

        // When
        List<ResetAudit> result = factoryResetService.getActiveJobs();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getJobId()).isEqualTo("job1");
        assertThat(result.get(1).getJobId()).isEqualTo("job2");
        
        verify(resetAuditRepository).findActiveJobs();
        verify(resetAuditMapper, times(2)).toDomain(any());
    }

    @Test
    void getResetAuditByJobId_WhenJobExists_ShouldReturnDomainObject() {
        // Given
        String jobId = "test-job-id";
        
        ResetAuditJpaEntity entity = new ResetAuditJpaEntity();
        entity.setJobId(jobId);
        
        ResetAudit domain = ResetAudit.builder().jobId(jobId).build();
        
        when(resetAuditRepository.findByJobId(jobId)).thenReturn(Optional.of(entity));
        when(resetAuditMapper.toDomain(entity)).thenReturn(domain);

        // When
        ResetAudit result = factoryResetService.getResetAuditByJobId(jobId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getJobId()).isEqualTo(jobId);
        
        verify(resetAuditRepository).findByJobId(jobId);
        verify(resetAuditMapper).toDomain(entity);
    }

    @Test
    void getResetAuditByJobId_WhenJobNotFound_ShouldReturnNull() {
        // Given
        String jobId = "non-existent-job";
        
        when(resetAuditRepository.findByJobId(jobId)).thenReturn(Optional.empty());

        // When
        ResetAudit result = factoryResetService.getResetAuditByJobId(jobId);

        // Then
        assertThat(result).isNull();
        
        verify(resetAuditRepository).findByJobId(jobId);
        verify(resetAuditMapper, never()).toDomain(any());
    }

    @Test
    void getResetHistory_ShouldReturnLimitedResults() {
        // Given
        int limit = 10;
        
        ResetAuditJpaEntity entity = new ResetAuditJpaEntity();
        entity.setJobId("history-job");
        
        ResetAudit domain = ResetAudit.builder().jobId("history-job").build();
        
        when(resetAuditRepository.findAllOrderByStartedAtDesc(any(PageRequest.class)))
                .thenReturn(List.of(entity));
        when(resetAuditMapper.toDomain(entity)).thenReturn(domain);

        // When
        List<ResetAudit> result = factoryResetService.getResetHistory(limit);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getJobId()).isEqualTo("history-job");
        
        verify(resetAuditRepository).findAllOrderByStartedAtDesc(PageRequest.of(0, limit));
        verify(resetAuditMapper).toDomain(entity);
    }

    @Test
    void getResetHistory_ShouldEnforceLimits() {
        // Given - Test minimum limit
        when(resetAuditRepository.findAllOrderByStartedAtDesc(any(PageRequest.class)))
                .thenReturn(new ArrayList<>());

        // When
        factoryResetService.getResetHistory(0);

        // Then
        verify(resetAuditRepository).findAllOrderByStartedAtDesc(PageRequest.of(0, 1));

        // Given - Test maximum limit
        reset(resetAuditRepository);
        when(resetAuditRepository.findAllOrderByStartedAtDesc(any(PageRequest.class)))
                .thenReturn(new ArrayList<>());

        // When
        factoryResetService.getResetHistory(200);

        // Then
        verify(resetAuditRepository).findAllOrderByStartedAtDesc(PageRequest.of(0, 100));
    }

    @Test
    void streamResetProgress_ShouldCreateSseEmitter() {
        // Given
        String jobId = "test-job-id";

        // When
        SseEmitter result = factoryResetService.streamResetProgress(jobId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTimeout()).isEqualTo(300000L);
    }

    @Test
    void performH2Reset_ShouldClearAllRepositories() {
        // Given
        String jobId = "test-job-id";

        // When
        int result = factoryResetService.performH2Reset(jobId);

        // Then
        assertThat(result).isEqualTo(4);
        
        verify(starredProjectRepository).deleteAllInBatch();
        verify(projectRepository).deleteAllInBatch();
        verify(experienceRepository).deleteAllInBatch();
        verify(skillRepository).deleteAllInBatch();
        verify(entityManager, times(4)).createNativeQuery(anyString());
    }

    @Test
    void performPostgresReset_ShouldTruncateAllTables() {
        // Given
        ReflectionTestUtils.setField(factoryResetService, "databasePlatform", "org.hibernate.dialect.PostgreSQLDialect");
        ReflectionTestUtils.setField(factoryResetService, "datasourceUrl", "jdbc:postgresql://localhost:5432/db");
        factoryResetService.init();
        
        String jobId = "test-job-id";
        
        Query tableQuery = mock(Query.class);
        Query truncateQuery = mock(Query.class);
        
        List<String> tableNames = List.of("projects", "skills", "experiences", "starred_projects");
        
        when(entityManager.createNativeQuery(contains("pg_tables"))).thenReturn(tableQuery);
        when(tableQuery.getResultList()).thenReturn(tableNames);
        when(entityManager.createNativeQuery(contains("TRUNCATE"))).thenReturn(truncateQuery);

        // When
        int result = factoryResetService.performPostgresReset(jobId);

        // Then
        assertThat(result).isEqualTo(4);
        
        verify(entityManager).createNativeQuery(contains("pg_tables"));
        verify(entityManager).createNativeQuery(contains("TRUNCATE"));
        verify(truncateQuery).executeUpdate();
    }
}