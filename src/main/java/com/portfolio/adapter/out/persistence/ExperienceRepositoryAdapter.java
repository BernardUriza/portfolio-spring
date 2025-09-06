package com.portfolio.adapter.out.persistence;

import com.portfolio.adapter.out.persistence.jpa.ExperienceJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.ExperienceJpaMapper;
import com.portfolio.adapter.out.persistence.jpa.ExperienceJpaRepository;
import com.portfolio.core.domain.experience.Experience;
import com.portfolio.core.domain.experience.ExperienceType;
import com.portfolio.core.port.out.ExperienceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ExperienceRepositoryAdapter implements ExperienceRepositoryPort {
    
    private final ExperienceJpaRepository jpaRepository;
    private final ExperienceJpaMapper mapper;
    
    @Override
    public Experience save(Experience experience) {
        ExperienceJpaEntity entity = mapper.toEntity(experience);
        ExperienceJpaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
    
    @Override
    public Optional<Experience> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    @Override
    public List<Experience> findAll() {
        return jpaRepository.findAllOrderByStartDateDesc()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Experience> findByType(ExperienceType type) {
        return jpaRepository.findByTypeOrderByStartDate(type)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Experience> findByIsCurrentPositionTrue() {
        return jpaRepository.findCurrentExperiencesOrderedByStartDate()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Experience> findByCompanyNameIgnoreCase(String companyName) {
        return jpaRepository.findByCompanyNameIgnoreCase(companyName)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Experience> findLongTermExperiences() {
        return jpaRepository.findLongTermExperiences()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Experience> findOrderedByStartDateDesc() {
        return jpaRepository.findAllOrderByStartDateDesc()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Experience> findPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jpaRepository.findAll(pageable)
                .getContent()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public void delete(Experience experience) {
        ExperienceJpaEntity entity = mapper.toEntity(experience);
        jpaRepository.delete(entity);
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}