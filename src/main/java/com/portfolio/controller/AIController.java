package com.portfolio.controller;

import com.portfolio.dto.StackRequest;
import com.portfolio.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/message")
    public ResponseEntity<String> getMessage(@RequestBody StackRequest body) {
        List<String> stackList = body.getStack();
        String stack = String.join(", ", stackList);
        String msg = aiService.generateDynamicMessage(stack);
        return ResponseEntity.ok(msg);
    }
}
