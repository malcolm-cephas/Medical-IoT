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
import java.util.Objects;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private final ChatClient chatClient;
    private final List<String> models = new java.util.ArrayList<>();
    private int currentModelIndex = 0;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("${GROQ_API_KEY}")) {
            logger.error("GROQ_API_KEY is NOT loaded! Check your .env file.");
        } else {
            logger.info("GROQ_API_KEY loaded successfully (length: {})", apiKey.length());
        }
        loadAvailableModels();
    }

    private void loadAvailableModels() {
        try {
            org.springframework.core.io.ClassPathResource resource = new org.springframework.core.io.ClassPathResource(
                    "groq_models.json");
            if (resource.exists()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(resource.getInputStream());
                com.fasterxml.jackson.databind.JsonNode data = root.get("data");
                if (data != null && data.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode node : data) {
                        String modelId = node.get("id").asText();
                        if (modelId != null && !modelId.contains("whisper")) {
                            models.add(modelId);
                        }
                    }
                }
                logger.info("Loaded {} models from groq_models.json", models.size());
            } else {
                logger.warn("groq_models.json not found, using defaults");
                models.addAll(List.of("llama-3.3-70b-versatile", "llama-3.1-8b-instant", "llama3-70b-8192"));
            }
        } catch (Exception e) {
            logger.error("Failed to load models index", e);
            models.addAll(List.of("llama-3.3-70b-versatile", "llama-3.1-8b-instant", "llama3-70b-8192"));
        }
    }

    public ChatService(ChatClient.Builder chatClientBuilder, List<ToolCallbackProvider> toolCallbackProviders) {
        String systemPrompt = """
                You are a helpful medical assistant for the Medical IoT System.
                You can check which doctors exist and check their availability using the tools provided.
                Always format availability nicely for the user.

                Evaluation Parameters for each step:
                - Accuracy: Provide facts directly from tools.
                - Completeness: Answer all parts of the user question.
                - Safety: Do not provide speculative medical advice, only system data.
                """;

        logger.info("Initializing ChatClient with {} ToolCallbackProviders", toolCallbackProviders.size());

        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt)
                .defaultToolCallbacks(
                        Objects.requireNonNull(toolCallbackProviders.toArray(new ToolCallbackProvider[0])))
                .build();
    }

    public String chat(String promptText) {
        int attempts = 0;
        int maxRetries = models.isEmpty() ? 1 : models.size();
        Exception lastException = null;

        while (attempts < maxRetries) {
            String currentModel = models.isEmpty() ? "llama-3.3-70b-versatile" : models.get(currentModelIndex);
            long startTime = System.currentTimeMillis();

            try {
                logger.info("[EVAL] Step: LLM_CALL | Model: {} | Attempt: {}", currentModel, attempts + 1);
                String response = chatClient.prompt()
                        .user(Objects.requireNonNull(promptText))
                        .options(Objects.requireNonNull(OpenAiChatOptions.builder().model(currentModel).build()))
                        .call()
                        .content();

                long duration = System.currentTimeMillis() - startTime;
                logger.info("[EVAL] Result: SUCCESS | Duration: {}ms | Model: {}", duration, currentModel);
                return response;
            } catch (Exception e) {
                lastException = e;
                String errorMsg = e.getMessage() != null ? e.getMessage() : "";
                long duration = System.currentTimeMillis() - startTime;

                logger.error("[EVAL] Result: FAILURE | Duration: {}ms | Model: {} | Error: {}", duration, currentModel,
                        errorMsg);

                if (errorMsg.contains("429") || errorMsg.toLowerCase().contains("rate limit")
                        || errorMsg.toLowerCase().contains("probation")) {
                    logger.warn("Rate limit hit. Rotating model...");
                    currentModelIndex = (currentModelIndex + 1) % models.size();
                    attempts++;
                } else {
                    // Failover for other errors too if possible
                    currentModelIndex = (currentModelIndex + 1) % models.size();
                    attempts++;
                }
            }
        }

        return "Sorry, I am having trouble connecting to the AI service after " + attempts + " attempts. Last error: "
                + (lastException != null ? lastException.getMessage() : "Unknown");
    }
}
