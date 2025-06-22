package com.portfolio.service;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AIServiceTest {
    @Test
    void generatesMessage() {
        AIService service = new AIService();
        assertThat(service.generateDynamicMessage("Angular, Spring Boot"))
                .isEqualTo("This project uses: Angular, Spring Boot");
    }
}
