package com.portfolio.service;

import org.springframework.stereotype.Service;

@Service
public class AIService {

    public String generateDynamicMessage(String stack) {
        // Lógica fake para MVP
        return "This project uses: " + stack;
    }
}
