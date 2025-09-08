package com.portfolio.core.domain.project;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldProtectionTest {
    
    @Test
    void builder_DefaultValues_AllUnprotected() {
        // When
        FieldProtection protection = FieldProtection.builder().build();
        
        // Then
        assertFalse(protection.getDescription());
        assertFalse(protection.getLiveDemoUrl());
        assertFalse(protection.getSkills());
        assertFalse(protection.getExperiences());
    }
    
    @Test
    void allUnprotected_ReturnsAllFalse() {
        // When
        FieldProtection protection = FieldProtection.allUnprotected();
        
        // Then
        assertFalse(protection.getDescription());
        assertFalse(protection.getLiveDemoUrl());
        assertFalse(protection.getSkills());
        assertFalse(protection.getExperiences());
    }
    
    @Test
    void allProtected_ReturnsAllTrue() {
        // When
        FieldProtection protection = FieldProtection.allProtected();
        
        // Then
        assertTrue(protection.getDescription());
        assertTrue(protection.getLiveDemoUrl());
        assertTrue(protection.getSkills());
        assertTrue(protection.getExperiences());
    }
    
    @Test
    void withDescription_UpdatesDescriptionOnly() {
        // Given
        FieldProtection original = FieldProtection.allUnprotected();
        
        // When
        FieldProtection updated = original.withDescription(true);
        
        // Then
        assertTrue(updated.getDescription());
        assertFalse(updated.getLiveDemoUrl());
        assertFalse(updated.getSkills());
        assertFalse(updated.getExperiences());
    }
    
    @Test
    void withLiveDemoUrl_UpdatesLiveDemoUrlOnly() {
        // Given
        FieldProtection original = FieldProtection.allUnprotected();
        
        // When
        FieldProtection updated = original.withLiveDemoUrl(true);
        
        // Then
        assertFalse(updated.getDescription());
        assertTrue(updated.getLiveDemoUrl());
        assertFalse(updated.getSkills());
        assertFalse(updated.getExperiences());
    }
    
    @Test
    void withSkills_UpdatesSkillsOnly() {
        // Given
        FieldProtection original = FieldProtection.allUnprotected();
        
        // When
        FieldProtection updated = original.withSkills(true);
        
        // Then
        assertFalse(updated.getDescription());
        assertFalse(updated.getLiveDemoUrl());
        assertTrue(updated.getSkills());
        assertFalse(updated.getExperiences());
    }
    
    @Test
    void withExperiences_UpdatesExperiencesOnly() {
        // Given
        FieldProtection original = FieldProtection.allUnprotected();
        
        // When
        FieldProtection updated = original.withExperiences(true);
        
        // Then
        assertFalse(updated.getDescription());
        assertFalse(updated.getLiveDemoUrl());
        assertFalse(updated.getSkills());
        assertTrue(updated.getExperiences());
    }
    
    @Test
    void chainedUpdates_WorkCorrectly() {
        // Given
        FieldProtection original = FieldProtection.allUnprotected();
        
        // When
        FieldProtection updated = original
                .withDescription(true)
                .withSkills(true);
        
        // Then
        assertTrue(updated.getDescription());
        assertFalse(updated.getLiveDemoUrl());
        assertTrue(updated.getSkills());
        assertFalse(updated.getExperiences());
    }
    
    @Test
    void immutability_OriginalUnchanged() {
        // Given
        FieldProtection original = FieldProtection.allUnprotected();
        
        // When
        FieldProtection updated = original.withDescription(true);
        
        // Then
        assertFalse(original.getDescription()); // Original unchanged
        assertTrue(updated.getDescription());   // New instance changed
        assertNotSame(original, updated);       // Different instances
    }
}