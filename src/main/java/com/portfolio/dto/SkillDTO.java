package com.portfolio.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class SkillDTO {
    public SkillDTO() {    }
    public SkillDTO(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String name;

    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    private String description;
}
