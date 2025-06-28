package com.portfolio.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import com.portfolio.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    private final OpenAiService openAiService;

    private final Map<String, UserSession> userSessions = new ConcurrentHashMap<>();

    public String generateDynamicMessage(String stack) {
        String prompt = buildOptimizedPrompt(stack);
        return sendPrompt(prompt, 600);
    }

    public String generateMessageFromStack(List<String> stackList) {
        String stack = String.join(", ", stackList);
        return generateDynamicMessage(stack);
    }

    public String generateFromPrompt(String prompt) {
        return sendPrompt(prompt, 200);
    }

    public String generateProjectSummary(String title, String description, String stack) {
        String prompt = "Summarize the following project for a portfolio user. " +
                "Title: " + title + ". Description: " + description +
                ". Tech stack: " + stack + ". " +
                "Limit to 500 characters.";
        String response = sendPrompt(prompt, 300);
        return response.length() > 500 ? response.substring(0, 500) : response;
    }

    private String sendPrompt(String prompt, int maxTokens) {
        ChatMessage userMessage = new ChatMessage("user", prompt);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(userMessage))
                .maxTokens(maxTokens)
                .build();

        ChatCompletionResult result = openAiService.createChatCompletion(request);
        return result.getChoices().getFirst().getMessage().getContent().trim();
    }

    String buildOptimizedPrompt(String stack) {
        String[] parts = stack.split(",");
        String primaryStack = parts.length > 0 ? parts[0].trim() : stack;
        return "Generate a personalized professional message for a software developer's portfolio. " +
                "Context: User is viewing a project which uses " + stack + ".\n" +
                "Primary technology: " + primaryStack + "\n" +
                "Requirements:\n" +
                "- Professional tone, 2-3 sentences max\n" +
                "- Highlight expertise in " + primaryStack + "\n" +
                "- Include relevant technical keywords\n" +
                "- Available in English and Spanish\n" +
                "- JSON format: {\"en\": \"message\", \"es\": \"mensaje\"}\n" +
                "Focus on demonstrating deep knowledge and practical experience with the technology stack.";
    }

    /* === New contextual intelligence === */

    public void recordContext(String sessionId, ContextRequest req) {
        UserSession session = getOrCreateSession(sessionId);
        ContextInfo info = new ContextInfo();
        info.setSection(req.getSection());
        info.setProjectId(req.getProjectId());
        info.setMetadata(req.getUserContext());
        info.setTimestamp(Instant.now().toString());
        session.getVisitedSections().add(info);
    }

    public AgentInfo getAgentInfo(AgentType type) {
        return switch (type) {
            case PROJECT_GUIDE -> new AgentInfo(type, "Project Guide", "\uD83D\uDCC2", "Entusiasta técnico, explica arquitecturas");
            case TECH_EXPERT -> new AgentInfo(type, "Tech Expert", "\uD83D\uDCBB", "Formal y con jerga avanzada");
            case PORTFOLIO_HOST -> new AgentInfo(type, "Portfolio Host", "\uD83C\uDF99", "Amigable y comercial");
            case CONTACT_ASSISTANT -> new AgentInfo(type, "Contact Assistant", "\u2709", "Profesional y directo");
        };
    }

    public String generateContextAwareResponse(String message, String sessionId, ContextInfo context, AgentType agent) {
        UserSession session = getOrCreateSession(sessionId);
        session.setCurrentAgent(agent);
        session.getConversationHistory().add(message);
        String prompt = buildIntelligentPrompt(message, context, session);
        int tokens = calculateOptimalTokens(session, context);
        return sendPrompt(prompt + "\nMensaje actual: " + message, tokens);
    }

    public String generateRobustResponse(String message, ContextInfo context, String sessionId) {
        try {
            return generateContextAwareResponse(message, sessionId, context, AgentType.PORTFOLIO_HOST);
        } catch (Exception e) {
            log.warn("AI service error, using fallback", e);
            return generateFallbackResponse(message, context);
        }
    }

    private String generateFallbackResponse(String message, ContextInfo context) {
        return "Lo siento, no puedo responder en este momento.";
    }

    private String buildIntelligentPrompt(String userInput, ContextInfo context, UserSession session) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Act\u00FAa como ").append(getAgentPersonality(session.getCurrentAgent()));
        if (!session.getVisitedSections().isEmpty()) {
            prompt.append(" El usuario ha visitado: ");
            session.getVisitedSections().stream()
                    .map(ContextInfo::getSection)
                    .distinct()
                    .forEach(s -> prompt.append(s).append(" "));
        }
        if (context != null && context.getProjectId() != null) {
            prompt.append(" Contexto actual: proyecto ").append(context.getProjectId());
        }
        double engagement = calculateUserEngagement(session, context != null ? context.getSection() : null);
        if (engagement > 0.7) {
            prompt.append(" Usuario altamente interesado, profundizar en detalles t\u00E9cnicos.");
        } else if (engagement < 0.3) {
            prompt.append(" Usuario explorando superficialmente, mantener respuestas concisas y atractivas.");
        }
        return prompt.toString();
    }

    private double calculateUserEngagement(UserSession session, String currentSection) {
        return 0.5; // placeholder heuristic
    }

    private int calculateOptimalTokens(UserSession session, ContextInfo context) {
        int base = 200;
        if (context != null && "projects".equals(context.getSection())) base += 100;
        double engagement = calculateUserEngagement(session, context != null ? context.getSection() : null);
        if (engagement > 0.8) base += 150;
        if (session.getConversationHistory().size() > 5) base -= 50;
        return Math.max(150, Math.min(600, base));
    }

    private String getAgentPersonality(AgentType agent) {
        return switch (agent) {
            case PROJECT_GUIDE -> "Entusiasta t\u00E9cnico";
            case TECH_EXPERT -> "Especialista formal";
            case PORTFOLIO_HOST -> "Anfitri\u00F3n amigable";
            case CONTACT_ASSISTANT -> "Asistente profesional";
        };
    }

    private UserSession getOrCreateSession(String sessionId) {
        return userSessions.computeIfAbsent(sessionId, id -> {
            UserSession s = new UserSession();
            s.setVisitedSections(new ArrayList<>());
            s.setProjectInterestScores(new ConcurrentHashMap<>());
            s.setConversationHistory(new ArrayList<>());
            s.setSessionStartTime(System.currentTimeMillis());
            s.setCurrentAgent(AgentType.PORTFOLIO_HOST);
            return s;
        });
    }

    @Scheduled(fixedRate = 300000)
    public void analyzeUserPatterns() {
        userSessions.values().forEach(this::updateUserBehaviorModel);
    }

    private void updateUserBehaviorModel(UserSession session) {
        // placeholder for learning algorithm
    }

    public static class UserSession {
        private List<ContextInfo> visitedSections;
        private Map<String, Integer> projectInterestScores;
        private List<String> conversationHistory;
        private AgentType currentAgent;
        private long sessionStartTime;

        public List<ContextInfo> getVisitedSections() { return visitedSections; }
        public void setVisitedSections(List<ContextInfo> visitedSections) { this.visitedSections = visitedSections; }
        public Map<String, Integer> getProjectInterestScores() { return projectInterestScores; }
        public void setProjectInterestScores(Map<String, Integer> projectInterestScores) { this.projectInterestScores = projectInterestScores; }
        public List<String> getConversationHistory() { return conversationHistory; }
        public void setConversationHistory(List<String> conversationHistory) { this.conversationHistory = conversationHistory; }
        public AgentType getCurrentAgent() { return currentAgent; }
        public void setCurrentAgent(AgentType currentAgent) { this.currentAgent = currentAgent; }
        public long getSessionStartTime() { return sessionStartTime; }
        public void setSessionStartTime(long sessionStartTime) { this.sessionStartTime = sessionStartTime; }
    }
}
