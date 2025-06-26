package com.portfolio.controller;

import com.portfolio.dto.AIMessageRequest;
import com.portfolio.dto.MessageResponse;
import com.portfolio.dto.TraceRequest;
import com.portfolio.service.AIService;
import com.portfolio.service.TraceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    private final AIService aiService;
    private final TraceService traceService;
    public AIController(AIService aiService, TraceService traceService) {
        this.aiService = aiService;
        this.traceService = traceService;
    }

    @PostMapping("/message")
    public ResponseEntity<MessageResponse> getMessage(@RequestBody AIMessageRequest body) {
        if (body.getStack() != null && !body.getStack().isEmpty()) {
            String msg = aiService.generateMessageFromStack(body.getStack());
            return ResponseEntity.ok(new MessageResponse(msg));
        } else if (body.getPrompt() != null && !body.getPrompt().isBlank()) {
            String msg = aiService.generateFromPrompt(body.getPrompt());
            return ResponseEntity.ok(new MessageResponse(msg));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/trace")
    public ResponseEntity<MessageResponse> trace(@RequestBody TraceRequest body) {
        if (body.getInfo() == null || body.getInfo().isBlank() || body.getInfo().length() > 255) {
            return ResponseEntity.badRequest().build();
        }
        traceService.record(body.getInfo());
        return ResponseEntity.ok(new MessageResponse("received"));
    }
}
