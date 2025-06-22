package com.portfolio.controller;

import com.portfolio.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/message")
    public ResponseEntity<String> getMessage(@RequestBody Map<String, String> body) {
        String stack = body.get("stack");
        String msg = aiService.generateDynamicMessage(stack);
        return ResponseEntity.ok(msg);
    }
}
