package com.portfolio.adapter.out.persistence;

import com.portfolio.adapter.out.persistence.jpa.SkillJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.SkillJpaMapper;
import com.portfolio.adapter.out.persistence.jpa.SkillJpaRepository;
import com.portfolio.core.domain.skill.Skill;
import com.portfolio.core.domain.skill.SkillCategory;
import com.portfolio.core.domain.skill.SkillLevel;
import com.portfolio.core.port.out.SkillRepositoryPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SkillRepositoryAdapter implements SkillRepositoryPort {
    
    private final SkillJpaRepository jpaRepository;
    private final SkillJpaMapper mapper;

    public SkillRepositoryAdapter(SkillJpaRepository jpaRepository, SkillJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public Skill save(Skill skill) {
        SkillJpaEntity entity = mapper.toEntity(skill);
        SkillJpaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
    
    @Override
    public Optional<Skill> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    @Override
    public List<Skill> findAll() {
        return jpaRepository.findAllOrderByCreatedAtDesc()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Skill> findByCategory(SkillCategory category) {
        return jpaRepository.findByCategoryOrderByLevelAndExperience(category)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Skill> findByLevel(SkillLevel level) {
        return jpaRepository.findByLevel(level)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Skill> findByFeaturedTrue() {
        return jpaRepository.findFeaturedSkillsOrdered()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Skill> findByYearsOfExperienceGreaterThanEqual(Integer minYears) {
        return jpaRepository.findByYearsOfExperienceGreaterThanEqual(minYears)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Skill> findPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jpaRepository.findAll(pageable)
                .getContent()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByNameIgnoreCase(name);
    }
    
    @Override
    public void delete(Skill skill) {
        SkillJpaEntity entity = mapper.toEntity(skill);
        jpaRepository.delete(entity);
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
