/**
 * Creado por Bernard Orozco
 * Tests for narration controller gating functionality
 */
package com.portfolio.controller;

import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaRepository;
import com.portfolio.service.JourneySessionService;
import com.portfolio.service.ClaudeNarrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    @Mock
    private HttpServletResponse httpServletResponse;

    @Test
    public void testStreamNarrationBlockedWhenPortfolioEmpty() throws Exception {
        String sessionId = "test-session-123";
        when(portfolioProjectRepository.count()).thenReturn(0L);

        SseEmitter emitter = narrationController.streamNarration(sessionId, httpServletRequest, httpServletResponse);

        assertNotNull(emitter);
        // The method should return an error SseEmitter that will send portfolio-empty event
        // This is tested functionally by the portfolio empty gating logic

        verify(portfolioProjectRepository).count();
        verifyNoInteractions(narrationService);
    }

    @Test
    public void testStreamNarrationAllowedWhenPortfolioHasProjects() throws Exception {
        String sessionId = "test-session-123";
        SseEmitter mockEmitter = new SseEmitter();
        
        when(portfolioProjectRepository.count()).thenReturn(5L);
        when(narrationService.createNarrationStream(sessionId, "127.0.0.1")).thenReturn(mockEmitter);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        SseEmitter result = narrationController.streamNarration(sessionId, httpServletRequest, httpServletResponse);

        assertNotNull(result);
        assertEquals(mockEmitter, result);
        verify(portfolioProjectRepository).count();
        verify(narrationService).createNarrationStream(sessionId, "127.0.0.1");
    }

    @Test
    public void testStreamNarrationHandlesServiceReturningNull() throws Exception {
        String sessionId = "test-session-123";
        
        when(portfolioProjectRepository.count()).thenReturn(5L);
        when(narrationService.createNarrationStream(sessionId, "127.0.0.1")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        SseEmitter result = narrationController.streamNarration(sessionId, httpServletRequest, httpServletResponse);

        assertNotNull(result);
        // Should return error stream when service returns null
        verify(portfolioProjectRepository).count();
        verify(narrationService).createNarrationStream(sessionId, "127.0.0.1");
    }

    @Test
    public void testStreamNarrationHandlesInvalidSessionId() throws Exception {
        String sessionId = null;
        
        when(portfolioProjectRepository.count()).thenReturn(5L);

        SseEmitter result = narrationController.streamNarration(sessionId, httpServletRequest, httpServletResponse);

        assertNotNull(result);
        // Should return error stream for invalid session ID
        verify(portfolioProjectRepository).count();
        verifyNoInteractions(narrationService);
    }

    @Test
    public void testPortfolioEmptyGatingHasPriority() throws Exception {
        String sessionId = "test-session-123";
        
        // Portfolio empty should be checked first, before session validation
        when(portfolioProjectRepository.count()).thenReturn(0L);

        SseEmitter result = narrationController.streamNarration(sessionId, httpServletRequest, httpServletResponse);

        assertNotNull(result);
        // Should return error stream with portfolio-empty message

        verify(portfolioProjectRepository).count();
        // Narration service should NOT be called when portfolio is empty
        verifyNoInteractions(narrationService);
    }
}
