package com.portfolio.config;

import io.github.cdimascio.dotenv.Dotenv;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {

    @Bean
    public OpenAiService openAiService() {
        String key = System.getenv("OPENAI_API_KEY"); // PRIMERO VARIABLES DE ENTORNO
        if (key == null || key.isEmpty()) {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load(); // SOLO SI NO ENCUENTRA VARIABLE DE ENTORNO
            key = dotenv.get("OPENAI_API_KEY");
        }
        if (key == null || key.isEmpty()) {
            throw new RuntimeException("OPENAI_API_KEY not set in environment variables or .env file");
        }
        return new OpenAiService(key);
    }
}
