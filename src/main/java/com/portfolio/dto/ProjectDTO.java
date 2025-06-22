package com.portfolio.dto;

import lombok.Value;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;

@Value
@Builder
public class ProjectDTO {
    Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    String title;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description;

    @Size(max = 255, message = "Link must not exceed 255 characters")
    @Pattern(regexp = "^(https?|ftp)://.*$", message = "Link must be a valid URL")
    String link;

    @Size(max = 255, message = "Repo must not exceed 255 characters")
    @Pattern(regexp = "^[\\w-]+/[\\w-]+$", message = "Repo must be in the format user/repo")
    String githubRepo;

    @NotNull(message = "Created date is required")
    java.time.LocalDate createdDate;

    @NotBlank(message = "Stack is required")
    @Size(max = 100, message = "Stack must not exceed 100 characters")
    String stack;
}
