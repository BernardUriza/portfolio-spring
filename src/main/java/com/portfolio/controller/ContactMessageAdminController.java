/**
 * Creado por Bernard Orozco
 * Admin controller for contact message management
 */
package com.portfolio.controller;

import com.portfolio.model.ContactMessage;
import com.portfolio.model.ContactMessage.MessageStatus;
import com.portfolio.dto.ContactMessageUpdateRequest;
import com.portfolio.service.ContactMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.StringWriter;
import java.io.PrintWriter;

@RestController
@RequestMapping("/api/admin/contact-messages")
@CrossOrigin(origins = {"http://localhost:4200", "https://localhost:4200"})
public class ContactMessageAdminController {
    
    @Autowired
    private ContactMessageService contactMessageService;
    
    private final CopyOnWriteArrayList<SseEmitter> sseEmitters = new CopyOnWriteArrayList<>();
    
    @GetMapping
    public ResponseEntity<Page<ContactMessage>> getMessages(
            @RequestParam(required = false) MessageStatus status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) String label,
            Pageable pageable) {
        
        Page<ContactMessage> messages = contactMessageService.getMessages(status, q, dateFrom, dateTo, label, pageable);
        return ResponseEntity.ok(messages);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ContactMessage> getMessage(@PathVariable Long id) {
        return contactMessageService.getMessage(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<ContactMessage> updateMessage(
            @PathVariable Long id,
            @RequestBody ContactMessageUpdateRequest request) {
        
        try {
            ContactMessage updated = contactMessageService.updateMessage(id, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        contactMessageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/export.csv")
    public ResponseEntity<String> exportCsv(
            @RequestParam(required = false) MessageStatus status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) String label,
            Pageable pageable) {
        
        Page<ContactMessage> messages = contactMessageService.getMessages(status, q, dateFrom, dateTo, label, pageable);
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        
        // CSV Header
        writer.println("ID,Created At,Name,Email,Company,Subject,Status,Labels,Source Path,Session ID");
        
        // CSV Data
        messages.getContent().forEach(message -> {
            writer.printf("%d,%s,\"%s\",\"%s\",\"%s\",\"%s\",%s,\"%s\",\"%s\",\"%s\"%n",
                message.getId(),
                message.getCreatedAt(),
                escapeCSV(message.getName()),
                escapeCSV(message.getEmail()),
                escapeCSV(message.getCompany()),
                escapeCSV(message.getSubject()),
                message.getStatus(),
                String.join(";", message.getLabels()),
                escapeCSV(message.getSourcePath()),
                escapeCSV(message.getSessionId())
            );
        });
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "contact_messages.csv");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(stringWriter.toString());
    }
    
    @GetMapping("/stream")
    public SseEmitter subscribeToNewMessages() {
        SseEmitter emitter = new SseEmitter(300_000L); // 5 minutes timeout
        
        sseEmitters.add(emitter);
        
        emitter.onCompletion(() -> sseEmitters.remove(emitter));
        emitter.onTimeout(() -> sseEmitters.remove(emitter));
        emitter.onError((ex) -> sseEmitters.remove(emitter));
        
        return emitter;
    }
    
    public void notifyNewMessage(ContactMessage message) {
        sseEmitters.removeIf(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("new-message")
                    .data(message));
                return false;
            } catch (Exception e) {
                return true; // Remove dead emitter
            }
        });
    }
    
    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
