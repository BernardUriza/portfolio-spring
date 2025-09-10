/**
 * Creado por Bernard Orozco
 * DTO for contact message creation responses
 */
package com.portfolio.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ContactMessageResponse {
    private Long id;
    private LocalDateTime createdAt;
}