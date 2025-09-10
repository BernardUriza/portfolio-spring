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
        Style: minimal, sharp, second person, Spanish.
        Goal: narrate the visitor's path and highlight relevant projects, skills and outcomes.
        Do not ask questions; summarize and guide.
        """;
    
    @Autowired
    private AIServicePort aiService;
    
    @Autowired
    private JourneySessionService sessionService;
    
    @Autowired(required = false)
    private ProjectService projectService;
    
    @Value("${app.narration.enabled:true}")
    private boolean narrationEnabled;
    
    // Track active streams per IP for rate limiting
    private final Map<String, AtomicInteger> activeStreams = new ConcurrentHashMap<>();
    private static final int MAX_CONCURRENT_STREAMS = 10;
    
    public SseEmitter createNarrationStream(String sessionId, String clientIp) {
        if (!narrationEnabled) {
            logger.info("Narration disabled, returning mock stream");
            return createMockStream();
        }
        
        // Check concurrent streams limit
        AtomicInteger count = activeStreams.computeIfAbsent(clientIp, k -> new AtomicInteger(0));
        if (count.get() >= MAX_CONCURRENT_STREAMS) {
            logger.warn("Too many concurrent streams from IP: {}", clientIp);
            return null;
        }
        
        JourneySession session = sessionService.getSession(sessionId);
        if (session == null) {
            logger.warn("Session not found: {}", sessionId);
            return null;
        }
        
        count.incrementAndGet();
        SseEmitter emitter = new SseEmitter(300_000L); // 5 minutes timeout
        
        // Cleanup on completion or timeout
        emitter.onCompletion(() -> {
            count.decrementAndGet();
            logger.debug("SSE stream completed for session: {}", sessionId);
        });
        
        emitter.onTimeout(() -> {
            count.decrementAndGet();
            logger.debug("SSE stream timed out for session: {}", sessionId);
        });
        
        emitter.onError((ex) -> {
            count.decrementAndGet();
            logger.error("SSE stream error for session: " + sessionId, ex);
        });
        
        // Start async narration
        generateNarrationAsync(emitter, session);
        
        return emitter;
    }
    
    private void generateNarrationAsync(SseEmitter emitter, JourneySession session) {
        CompletableFuture.runAsync(() -> {
            try {
                // Send initial message
                emitter.send(SseEmitter.event().data("STARTED"));
                
                List<JourneyEvent> events = session.getRecentEvents(10);
                String context = buildNarrationContext(events);
                String userPrompt = buildUserPrompt(context, events);
                
                logger.debug("Generating narration for session: {} with {} events", 
                    session.getSessionId(), events.size());
                
                // Call Claude API
                String response = aiService.chat(SYSTEM_PROMPT, userPrompt);
                
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
                
                emitter.send(SseEmitter.event().data("DONE"));
                emitter.complete();
                
            } catch (Exception e) {
                logger.error("Error generating narration", e);
                try {
                    emitter.send(SseEmitter.event().data("ERROR:Error en análisis AI"));
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
        
        prompt.append("Perfil: Desarrollador full-stack especializado en transformación digital.\n");
        prompt.append("Proyectos destacados: React, Angular, Spring Boot, microservicios.\n");
        prompt.append("Contexto de navegación: ").append(context).append("\n");
        
        if (!events.isEmpty()) {
            prompt.append("Eventos recientes:\n");
            events.stream()
                .limit(5)
                .forEach(event -> {
                    prompt.append("- ").append(event.getType())
                        .append(" en ").append(event.getData())
                        .append("\n");
                });
        }
        
        prompt.append("Instrucciones: 2-4 frases máximo, 1 línea por emisión, ");
        prompt.append("sin preguntas, sin promesas técnicas, enfoque en valor y resultados.");
        
        return prompt.toString();
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