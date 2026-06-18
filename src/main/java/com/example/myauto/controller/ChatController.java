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

    // 1. Страница чата по конкретному объявлению (Рабочий URL)
    @GetMapping("/chat/{listingId}")
    public String chatPage(@PathVariable("listingId") Long listingId, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено: " + listingId));

        List<ChatMessage> history = chatService.getMessagesForListing(listingId);

        model.addAttribute("listing", listing);
        model.addAttribute("history", history);
        model.addAttribute("currentUsername", principal.getName());

        return "chat";
    }

    // 2. ИСПРАВЛЕНИЕ ОШИБКИ 404: Перехватываем старый URL инициализации чата из detail.html
    // и мягко перенаправляем пользователя на рабочий URL /chat/{id}
    @GetMapping("/chat/initiate/{listingId}")
    public String initiateChat(@PathVariable("listingId") Long listingId) {
        return "redirect:/chat/" + listingId;
    }

    // 3. Обработка входящих сообщений через WebSocket
    @MessageMapping("/chat/{listingId}")
    public void handleChatMessage(@DestinationVariable("listingId") Long listingId,
                                  @Payload Map<String, String> payload,
                                  Principal principal) {
        if (principal == null) return;

        String text = payload.get("content");
        if (text == null || text.trim().isEmpty()) return;

        // Сохранение сообщения в базу данных через сервис
        ChatMessage savedMessage = chatService.saveMessage(listingId, principal.getName(), text);

        // Формирование топика назначения
        String destination = "/topic/chat/" + listingId;

        Map<String, Object> messageJson = Map.of(
                "content", savedMessage.getContent(),
                "sender", savedMessage.getSender().getUsername(),
                "sentAt", savedMessage.getSentAt().toString()
        );

        // Явное приведение к (Object) убирает неоднозначность методов convertAndSend в Spring
        messagingTemplate.convertAndSend(destination, (Object) messageJson);
    }

    // 4. Страница со списком всех активных диалогов текущего пользователя
    @GetMapping("/chats")
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

    // 5. REST API Эндпоинт для плавающего виджета истории (возвращает JSON)
    @ResponseBody
    @GetMapping("/api/chat/my-chats-summary")
    public List<Map<String, Object>> getMyChatsSummary(Principal principal) {
        if (principal == null) {
            return List.of();
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