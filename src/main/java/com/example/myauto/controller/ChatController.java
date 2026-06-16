package com.example.myauto.controller;

import com.example.myauto.entity.ChatMessage;
import com.example.myauto.repository.ListingRepository;
import com.example.myauto.service.ChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.Map;

@Controller
public class ChatController {

    private final ChatService chatService;
    private final ListingRepository listingRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatService chatService,
                          ListingRepository listingRepository,
                          SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.listingRepository = listingRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/chat/{listingId}")
    public String chatPage(@PathVariable Long listingId, Model model) {
        model.addAttribute("listing", listingRepository.findById(listingId).orElseThrow());
        model.addAttribute("messages", chatService.getMessagesForListing(listingId));
        return "chat";
    }

    @MessageMapping("/chat/{listingId}")
    public void sendMessage(@DestinationVariable Long listingId,
                            @Payload Map<String, String> payload,
                            Principal principal) {
        String content = payload.get("content");
        if (content == null || content.trim().isEmpty()) return;

        ChatMessage saved = chatService.saveMessage(listingId, principal.getName(), content.trim());

        Map<String, String> response = Map.of(
                "sender", saved.getSender().getUsername(),
                "content", saved.getContent(),
                "sentAt", saved.getSentAt().toString()
        );

        messagingTemplate.convertAndSend("/topic/chat/" + listingId, response);
    }
}