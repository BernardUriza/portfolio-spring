package com.portfolio.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ProjectDTO {
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 100, message = "El título no puede superar 100 caracteres")
    private String title;

    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    private String description;

    @Size(max = 255, message = "El link no puede superar 255 caracteres")
    private String link;

    @NotBlank(message = "La fecha de creación es obligatoria")
    private String createdDate; // Formato esperado: yyyy-MM-dd
}
