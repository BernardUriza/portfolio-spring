package com.portfolio.service;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIServiceTest {

    @Mock
    private OpenAiService openAiService;

    @Test
    void generatesMessage() {
        ChatMessage msg = new ChatMessage();
        msg.setContent("Hello!");
        ChatCompletionChoice choice = new ChatCompletionChoice();
        choice.setMessage(msg);
        ChatCompletionResult result = new ChatCompletionResult();
        result.setChoices(List.of(choice));

        when(openAiService.createChatCompletion(any(ChatCompletionRequest.class))).thenReturn(result);

        AIService service = new AIService(openAiService);
        String response = service.generateDynamicMessage("Java");

        assertThat(response).isEqualTo("Hello!");
    }
}
