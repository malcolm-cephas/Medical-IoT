package com.malcolm.mcpclient.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private final ChatClient chatClient;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("${GROQ_API_KEY}")) {
            logger.error("GROQ_API_KEY is NOT loaded! Check your .env file.");
        } else {
            logger.info("GROQ_API_KEY loaded successfully (length: {})", apiKey.length());
        }
    }

    public ChatService(ChatClient.Builder chatClientBuilder, List<ToolCallbackProvider> toolCallbackProviders) {
        String systemPrompt = """
                You are a helpful medical assistant for the Medical IoT System.
                You can check which doctors exist and check their availability using the tools provided.
                Always format availability nicely for the user.
                """;

        logger.info("Initializing ChatClient with {} ToolCallbackProviders", toolCallbackProviders.size());

        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt)
                .defaultToolCallbacks(toolCallbackProviders.toArray(new ToolCallbackProvider[0]))
                .build();
    }

    public String chat(String promptText) {
        try {
            return chatClient.prompt()
                    .user(promptText)
                    .call()
                    .content();
        } catch (Exception e) {
            logger.error("Error calling chat model", e);
            return "Sorry, I am having trouble connecting to the AI service. " + e.getMessage();
        }
    }
}
