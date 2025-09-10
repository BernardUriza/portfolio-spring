package com.portfolio.config;

import com.portfolio.security.AdminTokenAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@ConditionalOnProperty(name = "portfolio.admin.security.enabled", havingValue = "true", matchIfMissing = true)
public class AdminSecurityConfig {
    
    private final AdminTokenAuthenticationFilter adminTokenAuthenticationFilter;
    
    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .securityMatcher("/api/admin/**")
            .authorizeHttpRequests(auth -> auth
                // Public admin endpoints (read-only)
                .requestMatchers("/api/admin/sync-config/status").permitAll()
                .requestMatchers("/api/admin/factory-reset/audit").permitAll()
                
                // Protected admin endpoints (require token)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .addFilterBefore(adminTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
    
    @Bean
    @Order(2)
    public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .securityMatcher("/**")
            .authorizeHttpRequests(auth -> auth
                // Public API endpoints
                .requestMatchers("/api/projects/**").permitAll()
                .requestMatchers("/api/contact/**").permitAll()
                .requestMatchers("/api/ai/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().permitAll()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions().disable()) // For H2 console
            .build();
    }
}