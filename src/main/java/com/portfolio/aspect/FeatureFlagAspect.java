package com.portfolio.aspect;

import com.portfolio.service.FeatureFlagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class FeatureFlagAspect {
    
    private final FeatureFlagService featureFlagService;
    
    @Around("@annotation(requiresFeature)")
    public Object checkFeatureFlag(ProceedingJoinPoint joinPoint, RequiresFeature requiresFeature) throws Throwable {
        
        try {
            // Validate that the feature is enabled
            featureFlagService.validateFeatureEnabled(requiresFeature.value());
            
            // Feature is enabled, proceed with method execution
            return joinPoint.proceed();
            
        } catch (UnsupportedOperationException e) {
            log.warn("Feature flag validation failed for {}: {}", requiresFeature.value(), e.getMessage());
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                    "error", "Feature unavailable",
                    "message", e.getMessage(),
                    "feature", requiresFeature.value(),
                    "status", "disabled"
                ));
        }
    }
}