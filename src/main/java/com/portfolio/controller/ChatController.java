package com.portfolio.controller;

import com.portfolio.dto.*;
import com.portfolio.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ChatController {

    private final AIService aiService;

    @PostMapping("/context")
    public ResponseEntity<?> updateContext(@RequestBody ContextRequest request) {
        aiService.recordContext("default", request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/agent/{type}")
    public ResponseEntity<AgentInfo> getAgent(@PathVariable AgentType type) {
        return ResponseEntity.ok(aiService.getAgentInfo(type));
    }

    @PostMapping("/message")
    public ResponseEntity<MessageResponse> sendMessage(@RequestBody ChatMessageRequest req) {
        String reply = aiService.generateContextAwareResponse(req.getMessage(), "default", req.getContext(), req.getAgent());
        return ResponseEntity.ok(new MessageResponse(reply));
    }
}
