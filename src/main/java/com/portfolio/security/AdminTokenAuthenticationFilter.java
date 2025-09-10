package com.portfolio.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminTokenAuthenticationFilter extends OncePerRequestFilter {
    
    @Value("${portfolio.admin.token:}")
    private String adminToken;
    
    @Value("${portfolio.admin.security.enabled:true}")
    private boolean securityEnabled;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        if (!securityEnabled) {
            log.debug("Admin security disabled, allowing request");
            filterChain.doFilter(request, response);
            return;
        }
        
        String path = request.getRequestURI();
        
        // Skip authentication for public admin endpoints
        if (isPublicAdminEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Only apply to admin endpoints
        if (path.startsWith("/api/admin/")) {
            String authHeader = request.getHeader("Authorization");
            String tokenHeader = request.getHeader("X-Admin-Token");
            
            String token = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            } else if (tokenHeader != null) {
                token = tokenHeader;
            }
            
            if (token != null && isValidAdminToken(token)) {
                // Create authentication
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        "admin", 
                        null, 
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                    );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Admin authentication successful for path: {}", path);
            } else {
                log.warn("Invalid or missing admin token for path: {} from IP: {}", 
                        path, getClientIpAddress(request));
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Valid admin token required\"}");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isPublicAdminEndpoint(String path) {
        return path.equals("/api/admin/sync-config/status") ||
               path.equals("/api/admin/factory-reset/audit") ||
               path.startsWith("/api/admin/factory-reset/stream/"); // SSE endpoints
    }
    
    private boolean isValidAdminToken(String token) {
        if (adminToken == null || adminToken.trim().isEmpty()) {
            log.warn("No admin token configured - denying access");
            return false;
        }
        
        return adminToken.equals(token);
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}