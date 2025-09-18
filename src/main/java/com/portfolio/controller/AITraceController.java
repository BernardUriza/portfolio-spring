package com.portfolio.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

/**
 * Creado por Bernard Orozco
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = {"http://localhost:4200", "https://bernardowiredu.com"})
public class AITraceController {

    private static final Logger log = LoggerFactory.getLogger(AITraceController.class);

    @PostMapping("/trace")
    public ResponseEntity<Map<String, String>> trace(@RequestBody Map<String, String> payload) {
        String info = payload.get("info");
        
        if (info != null) {
            log.debug("AI Trace: {}", info);
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Trace recorded");
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    }
}