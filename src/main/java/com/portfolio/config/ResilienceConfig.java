package com.portfolio.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ResilienceConfig {
    
    private final MeterRegistry meterRegistry;

    @Bean
    public RegistryEventConsumer<Retry> retryRegistryEventConsumer() {
        return new RegistryEventConsumer<Retry>() {
            @Override
            public void onEntryAddedEvent(EntryAddedEvent<Retry> entryAddedEvent) {
                Retry retry = entryAddedEvent.getAddedEntry();
                retry.getEventPublisher()
                    .onRetry(event -> {
                        log.debug("Retry attempt {} for {}: {}", 
                                 event.getNumberOfRetryAttempts(), 
                                 retry.getName(), 
                                 event.getLastThrowable().getMessage());
                        
                        meterRegistry.counter("resilience4j.retry", 
                                            "name", retry.getName(),
                                            "attempt", String.valueOf(event.getNumberOfRetryAttempts()))
                                    .increment();
                    })
                    .onSuccess(event -> {
                        log.debug("Retry succeeded for {} after {} attempts", 
                                 retry.getName(), 
                                 event.getNumberOfRetryAttempts());
                        
                        meterRegistry.counter("resilience4j.retry.success", 
                                            "name", retry.getName())
                                    .increment();
                    })
                    .onError(event -> {
                        log.warn("Retry failed for {} after {} attempts: {}", 
                                retry.getName(), 
                                event.getNumberOfRetryAttempts(),
                                event.getLastThrowable().getMessage());
                        
                        meterRegistry.counter("resilience4j.retry.failure", 
                                            "name", retry.getName())
                                    .increment();
                    });
            }

            @Override
            public void onEntryRemovedEvent(EntryRemovedEvent<Retry> entryRemoveEvent) {
                // No action needed
            }

            @Override
            public void onEntryReplacedEvent(EntryReplacedEvent<Retry> entryReplacedEvent) {
                // No action needed
            }
        };
    }

}