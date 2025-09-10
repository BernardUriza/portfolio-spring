/**
 * Creado por Bernard Orozco
 * Controller for contact message management
 */
package com.portfolio.controller;

import com.portfolio.dto.ContactMessageRequest;
import com.portfolio.dto.ContactMessageResponse;
import com.portfolio.service.ContactMessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact-messages")
@CrossOrigin(origins = {"http://localhost:4200", "https://localhost:4200"})
public class ContactMessageController {
    
    @Autowired
    private ContactMessageService contactMessageService;
    
    @PostMapping
    public ResponseEntity<ContactMessageResponse> createMessage(
            @Valid @RequestBody ContactMessageRequest request,
            HttpServletRequest httpRequest) {
        
        String userAgent = httpRequest.getHeader("User-Agent");
        String clientIp = getClientIp(httpRequest);
        
        try {
            ContactMessageResponse response = contactMessageService.createMessage(request, userAgent, clientIp);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(429).build(); // Too Many Requests
        }
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}