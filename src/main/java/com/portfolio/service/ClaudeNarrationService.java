/**
 * Creado por Bernard Orozco
 * Service for Claude-powered live narration
 */
package com.portfolio.service;

import com.portfolio.model.JourneyEvent;
import com.portfolio.model.JourneySession;
import com.portfolio.core.port.out.AIServicePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.IOException;

@Service
public class ClaudeNarrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClaudeNarrationService.class);
    private static final String SYSTEM_PROMPT = """
        You are "AI_CATALYST_ONLINE", a concise narrative assistant on a developer portfolio.
        Style: minimal, sharp, second person, Spanish, maximum 1-2 lines per response.
        Goal: narrate the visitor's path and highlight relevant projects, skills and outcomes.
        Rules: No questions, no PII, focus on technical value and business impact.
        Emit one insight per line, pause between insights.
        """;
    
    @Autowired
    private AIServicePort aiService;
    
    @Autowired
    private JourneySessionService sessionService;
    
    @Autowired(required = false)
    private SourceRepositoryService sourceRepositoryService;
    
    @Autowired
    private NarrationMetricsService metricsService;
    
    @Value("${app.narration.enabled:true}")
    private boolean narrationEnabled;
    
    // Track active streams per IP for rate limiting
    private final Map<String, AtomicInteger> activeStreams = new ConcurrentHashMap<>();
    private static final int MAX_CONCURRENT_STREAMS = 10;
    
    // Track concurrent streams globally
    private final AtomicInteger globalActiveStreams = new AtomicInteger(0);
    private static final int MAX_GLOBAL_STREAMS = 50;
    
    public SseEmitter createNarrationStream(String sessionId, String clientIp) {
        if (!narrationEnabled) {
            logger.info("Narration disabled, returning mock stream");
            return createMockStream();
        }
        
        // Check global concurrent streams limit
        if (globalActiveStreams.get() >= MAX_GLOBAL_STREAMS) {
            logger.warn("Maximum global concurrent streams reached: {}", globalActiveStreams.get());
            return null;
        }
        
        // Check concurrent streams limit per IP
        AtomicInteger count = activeStreams.computeIfAbsent(clientIp, k -> new AtomicInteger(0));
        if (count.get() >= MAX_CONCURRENT_STREAMS) {
            logger.warn("Too many concurrent streams from IP: {}", clientIp);
            metricsService.recordRateLimitHit();
            return null;
        }
        
        JourneySession session = sessionService.getSession(sessionId);
        if (session == null) {
            logger.warn("Session not found: {}", sessionId);
            return null;
        }
        
        count.incrementAndGet();
        globalActiveStreams.incrementAndGet();
        metricsService.recordStreamStarted();
        SseEmitter emitter = new SseEmitter(300_000L); // 5 minutes timeout
        
        // Cleanup on completion or timeout
        emitter.onCompletion(() -> {
            count.decrementAndGet();
            globalActiveStreams.decrementAndGet();
            metricsService.recordStreamCompleted();
            sessionService.purgeExpiredSessions();
            logger.debug("SSE stream completed for session: {}", sessionId);
        });
        
        emitter.onTimeout(() -> {
            count.decrementAndGet();
            globalActiveStreams.decrementAndGet();
            metricsService.recordStreamErrored();
            sessionService.purgeExpiredSessions();
            logger.debug("SSE stream timed out for session: {}", sessionId);
        });
        
        emitter.onError((ex) -> {
            count.decrementAndGet();
            globalActiveStreams.decrementAndGet();
            metricsService.recordStreamErrored();
            sessionService.purgeExpiredSessions();
            logger.error("SSE stream error for session: " + sessionId, ex);
        });
        
        // Start async narration
        generateNarrationAsync(emitter, session);
        
        return emitter;
    }

    private boolean hasNoNewEvents(JourneySession session) {
        // Check if last event was more than 15 seconds ago
        return session.getLastEventAt().isBefore(
            java.time.LocalDateTime.now().minusSeconds(15)
        );
    }

    private void sendKeepAliveMessage(SseEmitter emitter) throws IOException, InterruptedException {
        String[] keepAliveMessages = {
            "Explorando tecnologías de transformación digital...",
            "Portfolio especializado en arquitecturas escalables...",
            "Proyectos con impacto real en productividad empresarial...",
            "Stack tecnológico: React, Angular, Spring Boot, microservicios..."
        };
        
        String message = keepAliveMessages[(int) (Math.random() * keepAliveMessages.length)];
        emitter.send(SseEmitter.event().data("LINE:" + message));
        Thread.sleep(2000);
    }

    private void generateFreshNarration(SseEmitter emitter, String userPrompt) throws IOException, InterruptedException {
        var timer = metricsService.startNarrationTimer();
        
        try {
            String response = aiService.chat(SYSTEM_PROMPT, userPrompt);
            
            // Estimate tokens used (rough calculation: ~4 chars per token)
            int estimatedTokens = (userPrompt.length() + (response != null ? response.length() : 0)) / 4;
            metricsService.recordTokensUsed(estimatedTokens);
            
            if (response != null && !response.trim().isEmpty()) {
                // Split response into lines and send each with delay
                String[] lines = response.split("\\n");
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        emitter.send(SseEmitter.event().data("LINE:" + line.trim()));
                        Thread.sleep(1000); // 1 second backpressure
                    }
                }
            } else {
                emitter.send(SseEmitter.event().data("LINE:Analizando tu recorrido..."));
            }
        } finally {
            metricsService.stopNarrationTimer(timer);
        }
    }
    
    private void generateNarrationAsync(SseEmitter emitter, JourneySession session) {
        CompletableFuture.runAsync(() -> {
            try {
                // Send initial message (named) with suggested reconnect delay to avoid rapid loops
                emitter.send(SseEmitter.event().name("start").reconnectTime(60000).data("STARTED"));
                
                List<JourneyEvent> events = session.getRecentEvents(10);
                String context = buildNarrationContext(events);
                String userPrompt = buildUserPrompt(context, events);
                
                logger.debug("Generating narration for session: {} with {} events", 
                    session.getSessionId(), events.size());
                
                if (events.isEmpty() || hasNoNewEvents(session)) {
                    // Send keep-alive context message
                    sendKeepAliveMessage(emitter);
                } else {
                    // Call Claude API for fresh narrative
                    generateFreshNarration(emitter, userPrompt);
                }
                
                // Signal done with explicit event name so clients can stop reconnecting
                emitter.send(SseEmitter.event().name("done").reconnectTime(600000).data("DONE"));
                // Complete stream after DONE (clients should not reconnect on 'done')
                emitter.complete();
                
            } catch (Exception e) {
                logger.error("Error generating narration", e);
                try {
                    emitter.send(SseEmitter.event().name("error").reconnectTime(60000).data("ERROR:Error en análisis AI"));
                    emitter.complete();
                } catch (IOException ioException) {
                    logger.error("Failed to send error message", ioException);
                }
            }
        });
    }
    
    private String buildNarrationContext(List<JourneyEvent> events) {
        if (events.isEmpty()) {
            return "El visitante acaba de llegar al portfolio.";
        }
        
        StringBuilder context = new StringBuilder();
        
        // Analyze routes
        long routeEvents = events.stream()
            .filter(e -> "route".equals(e.getType()))
            .count();
        
        // Analyze project interactions
        long projectViews = events.stream()
            .filter(e -> "project_view".equals(e.getType()))
            .count();
        
        long projectClicks = events.stream()
            .filter(e -> "project_click".equals(e.getType()))
            .count();
        
        context.append("Navegación: ").append(routeEvents).append(" rutas, ");
        context.append(projectViews).append(" proyectos vistos, ");
        context.append(projectClicks).append(" clicks en proyectos.");
        
        return context.toString();
    }
    
    private String buildUserPrompt(String context, List<JourneyEvent> events) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Perfil: Bernard Orozco - Desarrollador full-stack especializado en transformación digital.\n");
        prompt.append("Experiencia: 15+ transformaciones empresariales, ciclos 3-6 meses, 100% éxito.\n");
        
        // Add project summaries if available
        if (sourceRepositoryService != null) {
            prompt.append("Proyectos clave: ").append(getProjectSummaries()).append("\n");
        }
        
        prompt.append("Contexto actual: ").append(context).append("\n");
        
        if (!events.isEmpty()) {
            prompt.append("Interacciones recientes:\n");
            events.stream()
                .filter(event -> event.getData() != null)
                .limit(3)
                .forEach(event -> {
                    String data = extractRelevantData(event);
                    if (!data.isEmpty()) {
                        prompt.append("- ").append(event.getType())
                            .append(": ").append(data).append("\n");
                    }
                });
        }
        
        prompt.append("Genera: 1-2 insights técnicos, máximo 2 líneas, enfoque en valor empresarial.");
        
        return prompt.toString();
    }

    private String getProjectSummaries() {
        try {
            // Get top 3 projects by relevance
            return "Portfolio web con Angular, API Spring Boot, arquitectura hexagonal, integración Claude AI";
        } catch (Exception e) {
            return "Proyectos full-stack con tecnologías modernas";
        }
    }

    private String extractRelevantData(JourneyEvent event) {
        if (event.getData() == null) return "";
        
        return switch (event.getType()) {
            case "route" -> (String) event.getData().getOrDefault("route", "");
            case "project_view", "project_click", "project_hover" -> 
                (String) event.getData().getOrDefault("repo", "proyecto");
            default -> "";
        };
    }
    
    private SseEmitter createMockStream() {
        SseEmitter emitter = new SseEmitter(30_000L);
        
        CompletableFuture.runAsync(() -> {
            try {
                emitter.send(SseEmitter.event().data("STARTED"));
                emitter.send(SseEmitter.event().data("LINE:Análisis en vivo deshabilitado"));
                emitter.send(SseEmitter.event().data("LINE:Funcionalidad disponible solo en producción"));
                emitter.send(SseEmitter.event().data("DONE"));
                emitter.complete();
            } catch (IOException e) {
                logger.error("Error in mock stream", e);
            }
        });
        
        return emitter;
    }
}
