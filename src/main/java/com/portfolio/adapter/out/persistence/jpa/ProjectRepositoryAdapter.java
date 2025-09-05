package com.portfolio.adapter.out.persistence.jpa;

import com.portfolio.core.domain.project.Project;
import com.portfolio.core.domain.project.ProjectStatus;
import com.portfolio.core.domain.project.ProjectType;
import com.portfolio.core.port.out.ProjectRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectRepositoryAdapter implements ProjectRepositoryPort {
    
    private final ProjectJpaRepository jpaRepository;
    private final ProjectJpaMapper mapper;
    
    @Override
    public Project save(Project project) {
        log.debug("Saving project with ID: {}", project.getId());
        
        ProjectJpaEntity jpaEntity = mapper.toJpaEntity(project);
        ProjectJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        
        log.debug("Project saved with ID: {}", savedEntity.getId());
        return mapper.toDomainEntity(savedEntity);
    }
    
    @Override
    public Optional<Project> findById(Long id) {
        log.debug("Finding project by ID: {}", id);
        
        return jpaRepository.findById(id)
                .map(mapper::toDomainEntity);
    }
    
    @Override
    public List<Project> findAll() {
        log.debug("Finding all projects");
        
        return jpaRepository.findAll()
                .stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Project> findAllPaginated(int page, int size) {
        log.debug("Finding projects paginated: page={}, size={}", page, size);
        
        PageRequest pageRequest = PageRequest.of(page, size);
        return jpaRepository.findAllBy(pageRequest)
                .stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(Long id) {
        log.debug("Deleting project by ID: {}", id);
        jpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        log.debug("Checking if project exists by ID: {}", id);
        return jpaRepository.existsById(id);
    }
    
    @Override
    public long count() {
        log.debug("Counting all projects");
        return jpaRepository.count();
    }
    
    @Override
    public List<Project> findByStatus(ProjectStatus status) {
        log.debug("Finding projects by status: {}", status);
        
        ProjectJpaEntity.ProjectStatusJpa jpaStatus = mapper.toJpaStatus(status);
        return jpaRepository.findByStatus(jpaStatus)
                .stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Project> findByType(ProjectType type) {
        log.debug("Finding projects by type: {}", type);
        
        ProjectJpaEntity.ProjectTypeJpa jpaType = mapper.toJpaType(type);
        return jpaRepository.findByType(jpaType)
                .stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Project> findByTechnology(String technology) {
        log.debug("Finding projects by technology: {}", technology);
        
        return jpaRepository.findByTechnology(technology)
                .stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }
}