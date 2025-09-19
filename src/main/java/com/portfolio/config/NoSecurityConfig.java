package com.portfolio.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for when admin security is disabled.
 * This allows all requests without authentication.
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "portfolio.admin.security.enabled", havingValue = "false", matchIfMissing = false)
public class NoSecurityConfig {

    @Bean
    public SecurityFilterChain noSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
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