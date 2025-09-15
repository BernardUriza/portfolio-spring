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
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.List;
import jakarta.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminTokenAuthenticationFilter extends OncePerRequestFilter {
    
    @Value("${portfolio.admin.token:${PORTFOLIO_ADMIN_TOKEN:}}")
    private String adminToken;
    
    @Value("${portfolio.admin.security.enabled:${PORTFOLIO_ADMIN_SECURITY_ENABLED:true}}")
    private boolean securityEnabled;
    
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, 
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        // Always let CORS preflight pass
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

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
                // Set minimal CORS headers to avoid opaque failures in browsers
                String origin = request.getHeader("Origin");
                if (origin != null && !origin.isBlank()) {
                    response.setHeader("Access-Control-Allow-Origin", origin);
                    response.setHeader("Vary", "Origin");
                    response.setHeader("Access-Control-Allow-Credentials", "true");
                    response.setHeader("Access-Control-Allow-Headers", "Authorization, X-Admin-Token, Content-Type");
                    response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
                }
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Valid admin token required\"}");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    @PostConstruct
    public void logSecurityConfig() {
        if (securityEnabled) {
            if (adminToken == null || adminToken.trim().isEmpty()) {
                log.warn("Admin security is ENABLED but no admin token is configured");
            } else {
                String masked = maskToken(adminToken.trim());
                log.info("Admin security is ENABLED; admin token configured: {} (len={})", masked, adminToken.trim().length());
            }
        } else {
            log.warn("Admin security is DISABLED (portfolio.admin.security.enabled=false)");
        }
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
        String expected = adminToken.trim();
        String provided = token == null ? "" : token.trim();
        return expected.equals(provided);
    }
    
    private String maskToken(String token) {
        if (token == null || token.isEmpty()) return "";
        int len = token.length();
        if (len <= 8) return "********"; // short tokens fully masked
        String start = token.substring(0, 4);
        String end = token.substring(len - 4);
        return start + "…" + end;
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
