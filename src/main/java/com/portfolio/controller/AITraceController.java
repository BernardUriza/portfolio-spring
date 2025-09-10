package com.portfolio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = {"http://localhost:4200", "https://bernardowiredu.com"})
@Slf4j
public class AITraceController {

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