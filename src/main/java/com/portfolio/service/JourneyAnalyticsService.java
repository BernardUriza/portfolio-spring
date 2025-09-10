/**
 * Creado por Bernard Orozco
 * Service for analyzing journey sessions and creating visitor insights
 */
package com.portfolio.service;

import com.portfolio.model.JourneyEvent;
import com.portfolio.model.JourneySession;
import com.portfolio.model.VisitorInsight;
import com.portfolio.repository.VisitorInsightRepository;
import com.portfolio.core.port.out.AIServicePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JourneyAnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(JourneyAnalyticsService.class);
    private static final String ANALYTICS_PROMPT = """
        Analiza esta sesión de navegación en un portfolio de desarrollador:
        
        Perfil: Bernard Orozco - Desarrollador full-stack especializado en transformación digital.
        Duración: %d segundos
        Páginas visitadas: %d
        Proyectos vistos: %s
        Acciones clave: %s
        
        Genera una conclusión de 2-3 líneas sobre:
        1. Nivel de interés técnico del visitante
        2. Áreas de enfoque principales
        3. Potencial fit para colaboración
        
        Respuesta en español, profesional, máximo 300 palabras.
        """;
    
    @Autowired
    private VisitorInsightRepository insightRepository;
    
    @Autowired
    private JourneySessionService sessionService;
    
    @Autowired(required = false)
    private AIServicePort aiService;
    
    @Autowired(required = false)
    private NarrationMetricsService metricsService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // Buffer for session events before finalizing
    private final Map<String, List<JourneyEvent>> sessionBuffer = new ConcurrentHashMap<>();
    
    public VisitorInsight finalizeSession(String sessionId) {
        logger.info("Finalizing session: {}", sessionId);
        
        // Check if insight already exists
        if (insightRepository.findBySessionId(sessionId).isPresent()) {
            logger.warn("Insight already exists for session: {}", sessionId);
            return insightRepository.findBySessionId(sessionId).get();
        }
        
        JourneySession session = sessionService.getSession(sessionId);
        if (session == null) {
            logger.warn("Session not found for finalization: {}", sessionId);
            return null;
        }
        
        VisitorInsight insight = analyzeSession(session);
        
        // Generate AI conclusion asynchronously
        if (aiService != null) {
            CompletableFuture.runAsync(() -> generateAIConclusion(insight));
        } else {
            insight.setAiConclusion("AI analysis not available");
        }
        
        VisitorInsight saved = insightRepository.save(insight);
        
        // Clean up session
        sessionService.invalidateSession(sessionId);
        sessionBuffer.remove(sessionId);
        
        if (metricsService != null) {
            metricsService.recordSessionFinalized();
        }
        
        logger.info("Session finalized with insight ID: {}", saved.getId());
        return saved;
    }
    
    private VisitorInsight analyzeSession(JourneySession session) {
        VisitorInsight insight = new VisitorInsight();
        insight.setSessionId(session.getSessionId());
        insight.setStartedAt(session.getStartedAt());
        insight.setEndedAt(LocalDateTime.now());
        insight.calculateDuration();
        
        List<JourneyEvent> events = session.getEvents();
        
        // Analyze pages visited
        long uniqueRoutes = events.stream()
            .filter(e -> "route".equals(e.getType()))
            .map(e -> e.getData().get("route"))
            .distinct()
            .count();
        insight.setPagesVisited((int) uniqueRoutes);
        
        // Analyze projects viewed (top 5)
        List<String> topProjects = events.stream()
            .filter(e -> List.of("project_view", "project_click").contains(e.getType()))
            .map(e -> (String) e.getData().get("repo"))
            .filter(repo -> repo != null)
            .collect(Collectors.groupingBy(repo -> repo, Collectors.counting()))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        insight.setProjectsViewed(topProjects);
        
        // Summarize actions
        Map<String, Long> actionSummary = events.stream()
            .collect(Collectors.groupingBy(JourneyEvent::getType, Collectors.counting()));
        
        try {
            insight.setActions(objectMapper.writeValueAsString(actionSummary));
        } catch (Exception e) {
            logger.error("Error serializing action summary", e);
            insight.setActions("{}");
        }
        
        return insight;
    }
    
    private void generateAIConclusion(VisitorInsight insight) {
        try {
            String projectsText = insight.getProjectsViewed().isEmpty() ? 
                "ninguno específico" : String.join(", ", insight.getProjectsViewed());
            
            String prompt = String.format(ANALYTICS_PROMPT,
                insight.getDurationSeconds() != null ? insight.getDurationSeconds() : 0,
                insight.getPagesVisited(),
                projectsText,
                insight.getActions()
            );
            
            String conclusion = aiService.chat("", prompt);
            
            if (conclusion != null && !conclusion.trim().isEmpty()) {
                insight.setAiConclusion(conclusion.trim());
                insightRepository.save(insight);
                logger.info("AI conclusion generated for insight: {}", insight.getId());
            }
            
        } catch (Exception e) {
            logger.error("Error generating AI conclusion for insight: " + insight.getId(), e);
            insight.setAiConclusion("Error generando análisis AI: " + e.getMessage());
            insightRepository.save(insight);
        }
    }
    
    public void linkContactMessage(String sessionId, Long contactMessageId) {
        insightRepository.findBySessionId(sessionId).ifPresent(insight -> {
            insight.setContactMessageId(contactMessageId);
            insightRepository.save(insight);
            logger.info("Linked contact message {} to insight for session: {}", contactMessageId, sessionId);
        });
    }
    
    public void linkContactByEmail(String email, Long contactMessageId) {
        // This would require extending the system to track email in sessions
        // For now, we'll just log the attempt
        logger.info("Attempted to link contact message {} by email: {}", contactMessageId, email);
    }
    
    public long getInsightCount() {
        return insightRepository.count();
    }
    
    public Double getAverageSessionDuration() {
        return insightRepository.getAverageSessionDuration();
    }
}