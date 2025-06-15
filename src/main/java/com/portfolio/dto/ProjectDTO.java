package com.portfolio.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class ProjectDTO {
    public ProjectDTO() {}
    public ProjectDTO(Long id, String title, String description, String link, String createdDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.link = link;
        this.createdDate = createdDate;
    }

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
