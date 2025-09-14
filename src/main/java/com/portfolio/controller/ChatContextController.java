package com.portfolio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

@RestController
@ConditionalOnProperty(name = "app.ai.enabled", havingValue = "true")
@RequestMapping("/api/chat")
@CrossOrigin(origins = {"http://localhost:4200", "https://bernardowiredu.com"})
@Slf4j
public class ChatContextController {

    @Data
    public static class ContextRequest {
        private String section;
        private String projectId;
        private String action;
        private Object userContext;
    }

    @PostMapping("/context")
    public ResponseEntity<Map<String, Object>> handleContext(@RequestBody ContextRequest request) {
        log.debug("Chat context received - Section: {}, Action: {}, ProjectId: {}", 
            request.getSection(), request.getAction(), request.getProjectId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Context processed successfully");
        response.put("timestamp", new Date());
        response.put("section", request.getSection());
        
        return ResponseEntity.ok(response);
    }
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
}
