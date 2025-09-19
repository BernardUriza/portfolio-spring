package com.portfolio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for public API endpoints.
 * These endpoints should ALWAYS be accessible without authentication.
 * This runs with higher priority (Order 0) than admin security.
 */
@Configuration
@EnableWebSecurity
@Order(0) // Higher priority than admin security
public class PublicApiSecurityConfig {

    @Bean
    public SecurityFilterChain publicApiSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .securityMatcher(
                "/api/projects/**",
                "/api/skills/**",
                "/api/experience/**",
                "/api/contact/**",
                "/api/ai/**",
                "/api/public/**",
                "/actuator/health",
                "/api/admin/sync-config/status" // Public status endpoint
            )
            .cors(cors -> {})
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .logout(logout -> logout.disable())
            .build();
    }
}