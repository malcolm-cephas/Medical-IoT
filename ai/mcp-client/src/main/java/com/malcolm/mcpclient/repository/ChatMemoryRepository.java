package com.malcolm.mcpclient.repository;

import com.malcolm.mcpclient.model.Chat;
import com.malcolm.mcpclient.model.ChatMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class ChatMemoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public ChatMemoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String generateChatId(String userId, String description) {
        String chatId = UUID.randomUUID().toString();
        String sql = "INSERT INTO CHAT_MEMORY (conversation_id, user_id, description) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, chatId, userId, description);
        return chatId;
    }

    public boolean chatIdExists(String chatId) {
        String sql = "SELECT COUNT(*) FROM CHAT_MEMORY WHERE conversation_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, chatId);
        return count != null && count == 1;
    }

    public List<Chat> getAllChatsForUser(String userId) {
        String sql = "SELECT conversation_id, description FROM CHAT_MEMORY WHERE user_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Chat(rs.getString("conversation_id"), rs.getString("description")), userId);
    }

    public List<ChatMessage> getChatMessages(String chatId) {
        // This reads from Spring AI's managed table: spring_ai_chat_memory
        String sql = "SELECT content, type FROM spring_ai_chat_memory WHERE conversation_id = ? ORDER BY timestamp ASC";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new ChatMessage(rs.getString("content"), rs.getString("type")), chatId);
    }
}
