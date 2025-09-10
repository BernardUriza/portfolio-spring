/**
 * Creado por Bernard Orozco
 * Controller for journey analytics and finalization
 */
package com.portfolio.controller;

import com.portfolio.model.VisitorInsight;
import com.portfolio.service.JourneyAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/journey")
@CrossOrigin(origins = {"http://localhost:4200", "https://localhost:4200"})
public class JourneyAnalyticsController {
    
    @Autowired
    private JourneyAnalyticsService analyticsService;
    
    @PostMapping("/finalize")
    public ResponseEntity<VisitorInsight> finalizeSession(@RequestParam String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        VisitorInsight insight = analyticsService.finalizeSession(sessionId);
        
        if (insight == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(insight);
    }
}