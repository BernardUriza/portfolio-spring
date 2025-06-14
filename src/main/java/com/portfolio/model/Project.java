package com.portfolio.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String link;
    private LocalDate createdDate;

    // 🧠 Constructores
    public Project() {}

    public Project(String title, String description, String link, LocalDate createdDate) {
        this.title = title;
        this.description = description;
        this.link = link;
        this.createdDate = createdDate;
    }

    // 🧠 Getters y Setters (para que Spring sepa cómo leer y escribir los datos)
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
}
