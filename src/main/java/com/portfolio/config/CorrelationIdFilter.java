package com.portfolio.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(1) // Execute early in filter chain
public class CorrelationIdFilter implements Filter {
    
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";
    private static final String REQUEST_ID_MDC_KEY = "requestId";
    private static final String USER_ID_MDC_KEY = "userId";
    private static final String REQUEST_URI_MDC_KEY = "requestUri";
    private static final String REQUEST_METHOD_MDC_KEY = "requestMethod";
    private static final String CLIENT_IP_MDC_KEY = "clientIp";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            // Generate or extract correlation ID
            String correlationId = extractOrGenerateCorrelationId(httpRequest);
            
            // Generate unique request ID for this specific request
            String requestId = UUID.randomUUID().toString().substring(0, 8);
            
            // Extract client information
            String clientIp = extractClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            // Set MDC context for logging
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
            MDC.put(REQUEST_ID_MDC_KEY, requestId);
            MDC.put(REQUEST_URI_MDC_KEY, httpRequest.getRequestURI());
            MDC.put(REQUEST_METHOD_MDC_KEY, httpRequest.getMethod());
            MDC.put(CLIENT_IP_MDC_KEY, clientIp);
            
            // Add correlation ID to response headers
            httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
            httpResponse.setHeader("X-Request-ID", requestId);
            
            // Log request start
            long startTime = System.currentTimeMillis();
            log.info("REQUEST_START: {} {} - correlation_id={}, request_id={}, client_ip={}, user_agent={}", 
                    httpRequest.getMethod(), httpRequest.getRequestURI(), 
                    correlationId, requestId, clientIp, 
                    userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 100)) : "unknown");
            
            // Continue filter chain
            chain.doFilter(request, response);
            
            // Log request completion
            long duration = System.currentTimeMillis() - startTime;
            log.info("REQUEST_END: {} {} - status={}, duration={}ms, correlation_id={}, request_id={}", 
                    httpRequest.getMethod(), httpRequest.getRequestURI(), 
                    httpResponse.getStatus(), duration, correlationId, requestId);
            
        } finally {
            // Clean up MDC to prevent memory leaks
            MDC.clear();
        }
    }
    
    private String extractOrGenerateCorrelationId(HttpServletRequest request) {
        // Try to get correlation ID from headers (for distributed tracing)
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        
        if (correlationId == null || correlationId.trim().isEmpty()) {
            // Check alternative header names
            correlationId = request.getHeader("X-Trace-ID");
            if (correlationId == null || correlationId.trim().isEmpty()) {
                correlationId = request.getHeader("X-Request-ID");
            }
        }
        
        // Generate new correlation ID if none found
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = generateCorrelationId();
        }
        
        return correlationId;
    }
    
    private String generateCorrelationId() {
        // Generate UUID-based correlation ID
        return UUID.randomUUID().toString();
    }
    
    private String extractClientIp(HttpServletRequest request) {
        // Check for IP in various headers (for load balancers/proxies)
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP", 
            "X-Client-IP",
            "CF-Connecting-IP", // Cloudflare
            "True-Client-IP"    // Cloudflare
        };
        
        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs, take the first one
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        
        // Fall back to remote address
        return request.getRemoteAddr();
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("Correlation ID filter initialized");
    }
    
    @Override
    public void destroy() {
        log.info("Correlation ID filter destroyed");
    }
}