package com.portfolio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Configuration for HTTP security headers.
 * Implements OWASP security best practices for production deployments.
 *
 * @author Bernard Orozco
 */
@Configuration
public class SecurityHeadersConfig {

    @Value("${app.security.csp.enabled:true}")
    private boolean cspEnabled;

    @Value("${app.security.csp.policy:default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:; connect-src 'self' https://api.anthropic.com https://api.github.com}")
    private String cspPolicy;

    /**
     * Custom Content Security Policy header writer.
     */
    @Bean
    public HeaderWriter contentSecurityPolicyHeaderWriter() {
        return new HeaderWriter() {
            @Override
            public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
                if (cspEnabled && !response.containsHeader("Content-Security-Policy")) {
                    response.setHeader("Content-Security-Policy", cspPolicy);
                }
            }
        };
    }

    /**
     * Custom Permissions Policy header writer.
     */
    @Bean
    public HeaderWriter permissionsPolicyHeaderWriter() {
        return new HeaderWriter() {
            @Override
            public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
                if (!response.containsHeader("Permissions-Policy")) {
                    // Disable potentially dangerous browser features
                    String policy = "geolocation=(), microphone=(), camera=(), payment=(), usb=(), " +
                                  "magnetometer=(), gyroscope=(), accelerometer=()";
                    response.setHeader("Permissions-Policy", policy);
                }
            }
        };
    }

    /**
     * X-Content-Type-Options header writer.
     */
    @Bean
    public HeaderWriter contentTypeOptionsHeaderWriter() {
        return new HeaderWriter() {
            @Override
            public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
                if (!response.containsHeader("X-Content-Type-Options")) {
                    response.setHeader("X-Content-Type-Options", "nosniff");
                }
            }
        };
    }

    /**
     * Strict-Transport-Security header writer (HSTS).
     * Only applied in production with HTTPS.
     */
    @Bean
    public HeaderWriter strictTransportSecurityHeaderWriter() {
        return new HeaderWriter() {
            @Override
            public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
                // Only apply HSTS over HTTPS
                if (request.isSecure() && !response.containsHeader("Strict-Transport-Security")) {
                    // max-age=31536000 (1 year), includeSubDomains, preload
                    response.setHeader("Strict-Transport-Security",
                        "max-age=31536000; includeSubDomains; preload");
                }
            }
        };
    }

    /**
     * Referrer-Policy configuration.
     */
    @Bean
    public ReferrerPolicyHeaderWriter referrerPolicyHeaderWriter() {
        return new ReferrerPolicyHeaderWriter(
            ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN
        );
    }

    /**
     * X-XSS-Protection header writer.
     * Note: Modern browsers use CSP instead, but this provides defense-in-depth.
     */
    @Bean
    public XXssProtectionHeaderWriter xssProtectionHeaderWriter() {
        return new XXssProtectionHeaderWriter();
    }
}
