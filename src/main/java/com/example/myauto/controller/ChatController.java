package com.example.myauto.controller;

import com.example.myauto.entity.ChatMessage;
import com.example.myauto.entity.Listing;
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
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    // 1. Открыть страницу конкретного чата по объявлению
    @GetMapping("/chat/{listingId}")
    public String chatPage(@PathVariable Long listingId, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено: " + listingId));

        model.addAttribute("listing", listing);
        model.addAttribute("car", listing);
        model.addAttribute("messages", chatService.getMessagesForListing(listingId));
        model.addAttribute("currentUsername", principal.getName());
        return "chat";
    }

    // 2. Обработка отправки мгновенных сообщений через WebSocket
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
                "sentAt", saved.getSentAt() != null ? saved.getSentAt().toString() : ""
        );

        messagingTemplate.convertAndSend("/topic/chat/" + listingId, response);
    }

    // 3. Страница «Мои сообщения» — список всех активных чатов пользователя
    @GetMapping("/chat/my-chats")
    public String myChatsPage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        List<ChatMessage> allMessages = chatService.getMessagesForUser(principal.getName());

        List<Listing> activeListings = allMessages.stream()
                .map(ChatMessage::getListing)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        model.addAttribute("listings", activeListings);
        model.addAttribute("currentUsername", principal.getName());

        return "chats";
    }

    // 4. API Эндпоинт для плавающего виджета чатов (возвращает чистый JSON без перезагрузки)
    @ResponseBody
    @GetMapping("/api/chat/my-chats-summary")
    public List<Map<String, Object>> getMyChatsSummary(Principal principal) {
        if (principal == null) {
            return List.of(); // Если не авторизован, отдаем пустой список
        }

        List<ChatMessage> allMessages = chatService.getMessagesForUser(principal.getName());

        return allMessages.stream()
                .map(ChatMessage::getListing)
                .filter(Objects::nonNull)
                .distinct()
                .map(listing -> Map.of(
                        "id", (Object) listing.getId(),
                        "title", (Object) listing.getTitle(),
                        "sellerName", (Object) listing.getUser().getUsername()
                ))
                .toList();
    }
}