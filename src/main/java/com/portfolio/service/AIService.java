package com.portfolio.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AIService {

    private final OpenAiService openAiService;

    public String generateDynamicMessage(String stack) {
        String prompt = buildOptimizedPrompt(stack);
        ChatMessage userMessage = new ChatMessage("user", prompt);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(userMessage))
                .maxTokens(600)
                .build();

        ChatCompletionResult result = openAiService.createChatCompletion(request);
        return result.getChoices().getFirst().getMessage().getContent().trim();
    }

    String buildOptimizedPrompt(String stack) {
        String[] parts = stack.split(",");
        String primaryStack = parts.length > 0 ? parts[0].trim() : stack;
        return "Generate a personalized professional message for a software developer's portfolio. " +
                "Context: User is viewing a project which uses " + stack + ".\n" +
                "Primary technology: " + primaryStack + "\n" +
                "Requirements:\n" +
                "- Professional tone, 2-3 sentences max\n" +
                "- Highlight expertise in " + primaryStack + "\n" +
                "- Include relevant technical keywords\n" +
                "- Available in English and Spanish\n" +
                "- JSON format: {\"en\": \"message\", \"es\": \"mensaje\"}\n" +
                "Focus on demonstrating deep knowledge and practical experience with the technology stack.";
    }
}
