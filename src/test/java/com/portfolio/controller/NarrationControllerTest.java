/**
 * Creado por Bernard Orozco
 * Tests for narration controller gating functionality
 */
package com.portfolio.controller;

import com.portfolio.model.JourneySession;
import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaRepository;
import com.portfolio.service.JourneySessionService;
import com.portfolio.service.ClaudeNarrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NarrationControllerTest {

    @InjectMocks
    private NarrationController narrationController;

    @Mock
    private JourneySessionService journeySessionService;

    @Mock
    private ClaudeNarrationService narrationService;

    @Mock
    private PortfolioProjectJpaRepository portfolioProjectRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Test
    public void testStreamNarrationBlockedWhenPortfolioEmpty() {
        String sessionId = "test-session-123";
        when(portfolioProjectRepository.count()).thenReturn(0L);

        ResponseEntity<?> response = narrationController.streamNarration(sessionId, httpServletRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("portfolio-empty", body.get("code"));
        assertEquals("Portfolio is empty. Sync in progress. Try again soon.", body.get("message"));

        verify(portfolioProjectRepository).count();
        verifyNoInteractions(journeySessionService);
        verifyNoInteractions(narrationService);
    }

    @Test
    public void testStreamNarrationAllowedWhenPortfolioHasProjects() {
        String sessionId = "test-session-123";
        JourneySession mockSession = new JourneySession();
        
        when(portfolioProjectRepository.count()).thenReturn(5L);
        when(journeySessionService.getSession(sessionId)).thenReturn(mockSession);

        // Mock the actual narration streaming - this would normally set up SSE
        ResponseEntity<?> response = narrationController.streamNarration(sessionId, httpServletRequest);

        verify(portfolioProjectRepository).count();
        verify(journeySessionService).getSession(sessionId);
        // Note: Full SSE testing would require integration test setup
    }

    @Test
    public void testStreamNarrationBlockedWhenSessionExceedsQuotas() {
        String sessionId = "test-session-123";
        JourneySession mockSession = new JourneySession();
        
        // Set up session that exceeds quotas
        mockSession.setNarrationLinesUsed(30); // Over 25 line limit
        
        when(portfolioProjectRepository.count()).thenReturn(5L);
        when(journeySessionService.getSession(sessionId)).thenReturn(mockSession);

        ResponseEntity<?> response = narrationController.streamNarration(sessionId, httpServletRequest);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        verify(portfolioProjectRepository).count();
        verify(journeySessionService).getSession(sessionId);
    }

    @Test
    public void testStreamNarrationBlockedWhenSessionMuted() {
        String sessionId = "test-session-123";
        JourneySession mockSession = new JourneySession();
        mockSession.setMuted(true);
        
        when(portfolioProjectRepository.count()).thenReturn(5L);
        when(journeySessionService.getSession(sessionId)).thenReturn(mockSession);

        ResponseEntity<?> response = narrationController.streamNarration(sessionId, httpServletRequest);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(portfolioProjectRepository).count();
        verify(journeySessionService).getSession(sessionId);
    }

    @Test
    public void testStreamNarrationHandlesInvalidSession() {
        String sessionId = "invalid-session";
        
        when(portfolioProjectRepository.count()).thenReturn(5L);
        when(journeySessionService.getSession(sessionId)).thenReturn(null);

        ResponseEntity<?> response = narrationController.streamNarration(sessionId, httpServletRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(portfolioProjectRepository).count();
        verify(journeySessionService).getSession(sessionId);
    }

    @Test
    public void testPortfolioEmptyGatingHasPriority() {
        String sessionId = "test-session-123";
        
        // Portfolio empty should be checked first, before session validation
        when(portfolioProjectRepository.count()).thenReturn(0L);

        ResponseEntity<?> response = narrationController.streamNarration(sessionId, httpServletRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("portfolio-empty", body.get("code"));

        verify(portfolioProjectRepository).count();
        // Session service should NOT be called when portfolio is empty
        verifyNoInteractions(journeySessionService);
    }
}