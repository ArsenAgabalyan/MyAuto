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

    @GetMapping("/chat/{listingId}")
    public String chatPage(@PathVariable("listingId") Long listingId, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено: " + listingId));

        List<ChatMessage> history = chatService.getMessagesForListingAndMarkAsRead(listingId, principal.getName());

        model.addAttribute("listing", listing);
        model.addAttribute("history", history);
        model.addAttribute("currentUsername", principal.getName());

        return "chat";
    }

    @GetMapping("/chat/initiate/{listingId}")
    public String initiateChat(@PathVariable("listingId") Long listingId) {
        return "redirect:/chat/" + listingId;
    }

    @MessageMapping("/chat/{listingId}")
    public void handleChatMessage(@DestinationVariable("listingId") Long listingId,
                                  @Payload Map<String, String> payload,
                                  Principal principal) {
        if (principal == null) return;

        String text = payload.get("content");
        if (text == null || text.trim().isEmpty()) return;

        ChatMessage savedMessage = chatService.saveMessage(listingId, principal.getName(), text);
        String destination = "/topic/chat/" + listingId;

        Map<String, Object> messageJson = Map.of(
                "content", savedMessage.getContent(),
                "sender", savedMessage.getSender().getUsername(),
                "sentAt", savedMessage.getSentAt().toString()
        );

        messagingTemplate.convertAndSend(destination, (Object) messageJson);
    }

    @GetMapping("/chats")
    public String myChatsPage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        String username = principal.getName();
        List<ChatMessage> allMessages = chatService.getMessagesForUser(username);

        List<Map<String, Object>> chatListWithNotifications = allMessages.stream()
                .map(ChatMessage::getListing)
                .filter(Objects::nonNull)
                .distinct()
                .map(listing -> {
                    long unreadCount = chatService.getUnreadCount(listing.getId(), username);
                    return Map.of(
                            "listing", (Object) listing,
                            "hasUnread", (Object) (unreadCount > 0),
                            "unreadCount", (Object) unreadCount
                    );
                })
                .toList();

        model.addAttribute("chats", chatListWithNotifications);
        model.addAttribute("currentUsername", username);

        return "chats";
    }

    // НОВЫЙ REST ЭНДПОИНТ ДЛЯ ГЛАВНОЙ СТРАНИЦЫ SAYTA
    @ResponseBody
    @GetMapping("/api/chat/unread-count")
    public Map<String, Long> getTotalUnreadCount(Principal principal) {
        if (principal == null) {
            return Map.of("count", 0L);
        }
        long count = chatService.getTotalUnreadCount(principal.getName());
        return Map.of("count", count);
    }

    @ResponseBody
    @GetMapping("/api/chat/my-chats-summary")
    public List<Map<String, Object>> getMyChatsSummary(Principal principal) {
        if (principal == null) {
            return List.of();
        }

        String username = principal.getName();
        List<ChatMessage> allMessages = chatService.getMessagesForUser(username);

        return allMessages.stream()
                .map(ChatMessage::getListing)
                .filter(Objects::nonNull)
                .distinct()
                .map(listing -> {
                    long unreadCount = chatService.getUnreadCount(listing.getId(), username);
                    return Map.of(
                            "id", (Object) listing.getId(),
                            "title", (Object) listing.getTitle(),
                            "sellerName", (Object) listing.getUser().getUsername(),
                            "unreadCount", (Object) unreadCount
                    );
                })
                .toList();
    }
}