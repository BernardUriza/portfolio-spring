package com.portfolio.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Creado por Bernard Orozco
 */
@RestController
@ConditionalOnProperty(name = "app.ai.enabled", havingValue = "true")
@RequestMapping("/api/chat")
@CrossOrigin(origins = {"http://localhost:4200", "https://bernardowiredu.com"})
public class ChatContextController {

    private static final Logger log = LoggerFactory.getLogger(ChatContextController.class);

    public static class ContextRequest {
        private String section;
        private String projectId;
        private String action;
        private Object userContext;

        public ContextRequest() {}

        public String getSection() { return section; }
        public void setSection(String section) { this.section = section; }

        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public Object getUserContext() { return userContext; }
        public void setUserContext(Object userContext) { this.userContext = userContext; }
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
}
