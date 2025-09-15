package com.portfolio.config;

import com.portfolio.security.AdminTokenAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse; // <-- Jakarta, not javax
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
            .cors(cors -> {})
            .securityMatcher("/api/admin/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                // Public admin endpoints (read-only)
                .requestMatchers("/api/admin/sync-config/status", "/api/admin/sync-config/status/").permitAll()
                .requestMatchers("/api/admin/factory-reset/audit").permitAll()

                // Protected admin endpoints (require token)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .requestCache(cache -> cache.disable())
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .logout(logout -> logout.disable())
            .exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, e) -> {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // <-- fixed
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Valid admin token required\"}");
            }))
            .addFilterBefore(adminTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .cors(cors -> {})
            .securityMatcher("/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                // Public API endpoints
                .requestMatchers("/api/projects/**").permitAll()
                .requestMatchers("/api/contact/**").permitAll()
                .requestMatchers("/api/ai/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().permitAll()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .requestCache(cache -> cache.disable())
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .logout(logout -> logout.disable())
            .exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, e) -> {
                // For any unexpected auth challenge outside admin, do not redirect to /login
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"Unauthorized\"}");
            }))
            // H2 console: replace deprecated frameOptions() with new API
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))
            .build();
    }
}
