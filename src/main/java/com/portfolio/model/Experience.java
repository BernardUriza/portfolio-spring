package com.portfolio.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Experience {
    @Id
    @GeneratedValue
    private Long id;
    private String title;
    private String company;
    private String description;
}