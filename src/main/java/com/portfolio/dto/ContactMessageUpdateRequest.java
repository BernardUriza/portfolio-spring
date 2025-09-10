/**
 * Creado por Bernard Orozco
 * DTO for contact message admin updates
 */
package com.portfolio.dto;

import com.portfolio.model.ContactMessage.MessageStatus;
import lombok.Data;

import java.util.List;

@Data
public class ContactMessageUpdateRequest {
    private MessageStatus status;
    private List<String> labels;
    private String notes;
}