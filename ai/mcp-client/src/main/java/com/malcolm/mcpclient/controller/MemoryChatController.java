package com.malcolm.mcpclient.controller;

import com.malcolm.mcpclient.model.Chat;
import com.malcolm.mcpclient.model.ChatMessage;
import com.malcolm.mcpclient.model.ChatStartResponse;
import com.malcolm.mcpclient.service.MemoryChatService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat-memory")
@CrossOrigin(origins = "*")
public class MemoryChatController {

    private final MemoryChatService memoryChatService;

    public MemoryChatController(MemoryChatService memoryChatService) {
        this.memoryChatService = memoryChatService;
    }

    @GetMapping
    public List<Chat> getAllChats(Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "anonymous";
        return this.memoryChatService.getAllChats(userId);
    }

    @GetMapping("/{chatId}")
    public List<ChatMessage> getChatMessages(@PathVariable String chatId) {
        return this.memoryChatService.getChatMessages(chatId);
    }

    @PostMapping("/start")
    public ChatStartResponse startNewChat(@RequestBody Map<String, String> request, Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "anonymous";
        return this.memoryChatService.createChatWithResponse(request.get("message"), userId);
    }

    @PostMapping("/{chatId}")
    public ChatMessage chatMemory(@PathVariable String chatId, @RequestBody Map<String, String> request) {
        String response = this.memoryChatService.chat(chatId, request.get("message"));
        return new ChatMessage(response, "ASSISTANT");
    }
}
