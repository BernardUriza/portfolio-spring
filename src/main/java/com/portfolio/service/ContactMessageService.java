/**
 * Creado por Bernard Orozco
 * Service for managing contact messages
 */
package com.portfolio.service;

import com.portfolio.model.ContactMessage;
import com.portfolio.model.ContactMessage.MessageStatus;
import com.portfolio.repository.ContactMessageRepository;
import com.portfolio.dto.ContactMessageRequest;
import com.portfolio.dto.ContactMessageResponse;
import com.portfolio.dto.ContactMessageUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ContactMessageService {
    
    private static final Logger logger = LoggerFactory.getLogger(ContactMessageService.class);
    
    @Autowired
    private ContactMessageRepository repository;
    
    @Autowired(required = false)
    private NarrationMetricsService metricsService;
    
    public ContactMessageResponse createMessage(ContactMessageRequest request, String userAgent, String clientIp) {
        // Honeypot validation
        if (request.getHoneypot() != null && !request.getHoneypot().trim().isEmpty()) {
            logger.warn("Honeypot triggered from IP: {}", clientIp);
            throw new IllegalArgumentException("Invalid request");
        }
        
        // Rate limiting check
        String ipHash = hashIp(clientIp);
        if (isRateLimited(ipHash)) {
            logger.warn("Rate limit exceeded for IP: {}", clientIp);
            throw new IllegalStateException("Rate limit exceeded");
        }
        
        ContactMessage message = new ContactMessage();
        message.setName(request.getName());
        message.setEmail(request.getEmail());
        message.setCompany(request.getCompany());
        message.setSubject(request.getSubject());
        message.setMessage(request.getMessage());
        message.setSourcePath(request.getSourcePath());
        message.setSessionId(request.getSessionId());
        message.setUserAgent(userAgent);
        message.setIpHash(ipHash);
        message.setStatus(MessageStatus.NEW);
        
        ContactMessage saved = repository.save(message);
        
        if (metricsService != null) {
            metricsService.recordContactMessageCreated();
        }
        
        logger.info("Contact message created with ID: {}", saved.getId());
        return new ContactMessageResponse(saved.getId(), saved.getCreatedAt());
    }
    
    public Page<ContactMessage> getMessages(MessageStatus status, String query, 
                                          LocalDateTime dateFrom, LocalDateTime dateTo, 
                                          String label, Pageable pageable) {
        return repository.findWithFilters(status, query, dateFrom, dateTo, label, pageable);
    }
    
    public Optional<ContactMessage> getMessage(Long id) {
        return repository.findById(id);
    }
    
    public ContactMessage updateMessage(Long id, ContactMessageUpdateRequest request) {
        ContactMessage message = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        
        if (request.getStatus() != null) {
            message.setStatus(request.getStatus());
        }
        
        if (request.getLabels() != null) {
            message.setLabels(request.getLabels());
        }
        
        if (request.getNotes() != null) {
            message.setNotes(request.getNotes());
        }
        
        ContactMessage updated = repository.save(message);
        logger.info("Contact message {} updated", id);
        return updated;
    }
    
    public void deleteMessage(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            logger.info("Contact message {} deleted", id);
        }
    }
    
    private boolean isRateLimited(String ipHash) {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        
        int recentMessages = repository.countByIpHashSince(ipHash, oneMinuteAgo);
        int dailyMessages = repository.countByIpHashSince(ipHash, oneDayAgo);
        
        return recentMessages >= 3 || dailyMessages >= 30;
    }
    
    private String hashIp(String ip) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(ip.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString().substring(0, 64);
        } catch (Exception e) {
            logger.error("Error hashing IP", e);
            return "unknown";
        }
    }
}