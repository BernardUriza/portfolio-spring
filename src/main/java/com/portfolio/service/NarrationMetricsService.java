/**
 * Creado por Bernard Orozco
 * Service for narration metrics and observability
 */
package com.portfolio.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class NarrationMetricsService {
    
    private final Counter sessionsCreated;
    private final Counter eventsReceived;
    private final Counter tokensUsed;
    private final Counter streamsCompleted;
    private final Counter streamsErrored;
    private final Timer narrationGenerationTime;
    
    private final AtomicInteger activeStreams = new AtomicInteger(0);
    private final AtomicLong totalTokensLastMinute = new AtomicLong(0);
    private final AtomicInteger rateLimitHits = new AtomicInteger(0);
    
    @Autowired
    private JourneySessionService journeySessionService;
    
    public NarrationMetricsService(MeterRegistry meterRegistry) {
        // Counters
        this.sessionsCreated = Counter.builder("portfolio.narration.sessions.created")
                .description("Total number of journey sessions created")
                .register(meterRegistry);
                
        this.eventsReceived = Counter.builder("portfolio.narration.events.received")
                .description("Total number of journey events received")
                .register(meterRegistry);
                
        this.tokensUsed = Counter.builder("portfolio.narration.tokens.used")
                .description("Total number of AI tokens consumed")
                .register(meterRegistry);
                
        this.streamsCompleted = Counter.builder("portfolio.narration.streams.completed")
                .description("Total number of SSE streams completed successfully")
                .register(meterRegistry);
                
        this.streamsErrored = Counter.builder("portfolio.narration.streams.errored")
                .description("Total number of SSE streams that errored")
                .register(meterRegistry);
        
        // Timer
        this.narrationGenerationTime = Timer.builder("portfolio.narration.generation.time")
                .description("Time taken to generate AI narration")
                .register(meterRegistry);
        
        // Gauges
        Gauge.builder("portfolio.narration.streams.active")
                .description("Current number of active SSE streams")
                .register(meterRegistry, this, NarrationMetricsService::getActiveStreams);
                
        Gauge.builder("portfolio.narration.sessions.active")
                .description("Current number of active journey sessions")
                .register(meterRegistry, this, metrics -> journeySessionService.getActiveSessionCount());
                
        Gauge.builder("portfolio.narration.tokens.per.minute")
                .description("AI tokens used in the last minute")
                .register(meterRegistry, this, NarrationMetricsService::getTokensLastMinute);
                
        Gauge.builder("portfolio.narration.rate.limit.hits")
                .description("Number of rate limit hits")
                .register(meterRegistry, this, NarrationMetricsService::getRateLimitHits);
    }
    
    public void recordSessionCreated() {
        sessionsCreated.increment();
    }
    
    public void recordEventsReceived(int eventCount) {
        eventsReceived.increment(eventCount);
    }
    
    public void recordTokensUsed(int tokenCount) {
        tokensUsed.increment(tokenCount);
        totalTokensLastMinute.addAndGet(tokenCount);
        
        // Reset tokens counter every minute (simplified)
        // In production, this would use a sliding window
        if (System.currentTimeMillis() % 60000 < 1000) {
            totalTokensLastMinute.set(0);
        }
    }
    
    public void recordStreamStarted() {
        activeStreams.incrementAndGet();
    }
    
    public void recordStreamCompleted() {
        activeStreams.decrementAndGet();
        streamsCompleted.increment();
    }
    
    public void recordStreamErrored() {
        activeStreams.decrementAndGet();
        streamsErrored.increment();
    }
    
    public void recordRateLimitHit() {
        rateLimitHits.incrementAndGet();
    }
    
    public Timer.Sample startNarrationTimer() {
        return Timer.start(narrationGenerationTime);
    }
    
    public void stopNarrationTimer(Timer.Sample sample) {
        sample.stop(narrationGenerationTime);
    }
    
    // Gauge methods
    public double getActiveStreams() {
        return activeStreams.get();
    }
    
    public double getTokensLastMinute() {
        return totalTokensLastMinute.get();
    }
    
    public double getRateLimitHits() {
        return rateLimitHits.get();
    }
    
    // Health check method
    public boolean isHealthy() {
        return activeStreams.get() < 100 && // Not too many active streams
               journeySessionService.getActiveSessionCount() < 1000; // Not too many sessions
    }
}