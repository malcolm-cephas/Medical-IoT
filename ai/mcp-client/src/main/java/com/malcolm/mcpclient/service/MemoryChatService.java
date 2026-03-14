package com.malcolm.mcpclient.service;

import com.malcolm.mcpclient.model.Chat;
import com.malcolm.mcpclient.model.ChatMessage;
import com.malcolm.mcpclient.model.ChatStartResponse;
import com.malcolm.mcpclient.repository.ChatMemoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Service to handle AI chat with persistent memory.
 * It manages chat sessions, history retrieval, and context-aware AI interactions.
 */
@Service
public class MemoryChatService {

    private final ChatClient chatClient;
    private final ChatMemoryRepository chatMemoryRepository;
    
    // Default user for testing purposes. In production, this would be retrieved from Spring Security context.
    private static final String DEFAULT_USER_ID = "patient_01";
    
    // Prompt used to generate short summaries/titles for chat sessions.
    private static final String DESCRIPTION_PROMPT = 
        "Generate a short chat description (max 30 symbols) based on this message: ";

    public MemoryChatService(ChatClient.Builder chatClientBuilder,
                             JdbcChatMemoryRepository jdbcChatMemoryRepository,
                             ChatMemoryRepository chatMemoryRepository,
                             List<ToolCallbackProvider> toolCallbackProviders) {

        this.chatMemoryRepository = chatMemoryRepository;

        // Configure Spring AI ChatMemory with a sliding window of 10 messages.
        // It uses JdbcChatMemoryRepository to persist messages in the database.
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(Objects.requireNonNull(jdbcChatMemoryRepository))
                .maxMessages(10)
                .build();

        // Build the ChatClient with a 'memory advisor' that automatically injects 
        // past messages into the prompt context based on a session ID.
        this.chatClient = chatClientBuilder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultToolCallbacks(Objects.requireNonNull(toolCallbackProviders.toArray(new ToolCallbackProvider[0])))
                .build();
    }

    /**
     * Starts a new chat session by generating a description and saving initial metadata.
     * @return A response containing the new chatId and the AI's first message.
     */
    public ChatStartResponse createChatWithResponse(String message, String userId) {
        String description = generateDescription(message);
        String chatId = this.chatMemoryRepository.generateChatId(userId != null ? userId : DEFAULT_USER_ID, description);
        String response = this.chat(chatId, message);
        return new ChatStartResponse(chatId, response, description);
    }

    /**
     * Retrieves all chat sessions associated with a specific user.
     */
    public List<Chat> getAllChats(String userId) {
        return this.chatMemoryRepository.getAllChatsForUser(userId != null ? userId : DEFAULT_USER_ID);
    }

    /**
     * Loads the full message history (user and assistant messages) for a specific session.
     */
    public List<ChatMessage> getChatMessages(String chatId) {
        return this.chatMemoryRepository.getChatMessages(chatId);
    }

    /**
     * Continues an existing chat session.
     * @param chatId The unique ID of the conversation session.
     * @param message The new user message.
     * @return The AI's response, which considers the past context of this chatId.
     */
    public String chat(String chatId, String message) {
        if (!this.chatMemoryRepository.chatIdExists(chatId)) {
            throw new IllegalArgumentException("Chat ID does not exist: " + chatId);
        }
        
        // The .advisors() call uses the 'chat_memory_conversation_id' parameter to 
        // tell the ChatMemoryAdvisor which session's history to retrieve from the DB.
        return this.chatClient.prompt()
                .user(java.util.Objects.requireNonNull(message))
                .advisors(a -> a.param("chat_memory_conversation_id", java.util.Objects.requireNonNull(chatId)))
                .options(java.util.Objects.requireNonNull(OpenAiChatOptions.builder().build())) 
                .call()
                .content();
    }

    /**
     * Internal helper to generate a title for the chat session using the LLM.
     */
    private String generateDescription(String message) {
        return this.chatClient.prompt()
                .user(DESCRIPTION_PROMPT + message)
                .call()
                .content();
    }
}
