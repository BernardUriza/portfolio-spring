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
        ChatMessage userMessage = new ChatMessage("user", "Describe a project using: " + stack);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(userMessage))
                .maxTokens(60)
                .build();

        ChatCompletionResult result = openAiService.createChatCompletion(request);
        return result.getChoices().getFirst().getMessage().getContent().trim();
    }
}
