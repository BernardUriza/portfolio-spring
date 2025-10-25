package com.portfolio.config;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

/**
 * Enhanced Resilience4j configuration for GitHub API integration
 * Handles rate limiting (429), exponential backoff, and detailed metrics
 *
 * Created by Bernard Orozco
 */
@Configuration
public class GitHubResilienceConfig {

    private static final Logger log = LoggerFactory.getLogger(GitHubResilienceConfig.class);

    private final MeterRegistry meterRegistry;

    public GitHubResilienceConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Bean
    public RetryRegistry githubRetryRegistry() {
        RetryConfig retryConfig = RetryConfig.custom()
            .maxAttempts(5)
            // Exponential backoff: 1s, 2s, 4s, 8s, 16s
            .intervalFunction(IntervalFunction.ofExponentialRandomBackoff(
                Duration.ofSeconds(1),    // initial interval
                2.0,                       // multiplier
                Duration.ofSeconds(30)     // max interval
            ))
            // Retry on specific GitHub API errors
            .retryOnException(throwable -> {
                if (throwable instanceof WebClientResponseException webClientEx) {
                    int statusCode = webClientEx.getStatusCode().value();

                    // Retry on rate limit (429) and server errors (5xx)
                    if (statusCode == 429) {
                        log.warn("GitHub rate limit hit (429), will retry with backoff");
                        meterRegistry.counter("github.rate_limit_hit").increment();
                        return true;
                    }

                    if (statusCode >= 500) {
                        log.warn("GitHub server error ({}), will retry", statusCode);
                        meterRegistry.counter("github.server_error").increment();
                        return true;
                    }

                    // Don't retry on client errors (4xx except 429)
                    if (statusCode >= 400 && statusCode < 500) {
                        log.debug("GitHub client error ({}), will not retry", statusCode);
                        return false;
                    }
                }

                // Retry on network errors
                if (throwable instanceof java.net.SocketTimeoutException ||
                    throwable instanceof java.io.IOException) {
                    log.warn("Network error: {}, will retry", throwable.getMessage());
                    meterRegistry.counter("github.network_error").increment();
                    return true;
                }

                return false;
            })
            .retryOnResult(result -> result == null)
            .build();

        return RetryRegistry.of(retryConfig);
    }

    @Bean
    public Retry githubApiRetry(RetryRegistry githubRetryRegistry) {
        Retry retry = githubRetryRegistry.retry("githubApi");

        // Register event listeners for metrics
        retry.getEventPublisher()
            .onRetry(event -> {
                log.debug("GitHub API retry attempt {} for {}: {}",
                    event.getNumberOfRetryAttempts(),
                    event.getName(),
                    event.getLastThrowable().getMessage());
                meterRegistry.counter("github.retry.attempts",
                    "attempt", String.valueOf(event.getNumberOfRetryAttempts())
                ).increment();
            })
            .onSuccess(event -> {
                if (event.getNumberOfRetryAttempts() > 0) {
                    log.info("GitHub API call succeeded after {} retries",
                        event.getNumberOfRetryAttempts());
                    meterRegistry.counter("github.retry.success").increment();
                }
            })
            .onError(event -> {
                log.error("GitHub API call failed after {} retries: {}",
                    event.getNumberOfRetryAttempts(),
                    event.getLastThrowable().getMessage());
                meterRegistry.counter("github.retry.exhausted").increment();
            });

        return retry;
    }
}
