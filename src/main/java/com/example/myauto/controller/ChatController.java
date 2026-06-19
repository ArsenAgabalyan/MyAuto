package com.example.myauto.controller;

import com.example.myauto.entity.ChatMessage;
import com.example.myauto.entity.Listing;
import com.example.myauto.entity.User;
import com.example.myauto.repository.ChatMessageRepository;
import com.example.myauto.repository.ListingRepository;
import com.example.myauto.repository.UserRepository;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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

    private final ChatMessageRepository chatMessageRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatMessageRepository chatMessageRepository,
                          ListingRepository listingRepository,
                          UserRepository userRepository,
                          SimpMessagingTemplate messagingTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    @GetMapping("/chat/{listingId}")
    public String chatPage(@PathVariable("listingId") Long listingId, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено: " + listingId));

        String username = principal.getName();
        chatMessageRepository.markMessagesAsRead(listingId, username);
        List<ChatMessage> history = chatMessageRepository.findByListingIdOrderBySentAtAsc(listingId);

        model.addAttribute("listing", listing);
        model.addAttribute("history", history);
        model.addAttribute("currentUsername", username);

        return "chat";
    }

    @GetMapping("/chat/initiate/{listingId}")
    public String initiateChat(@PathVariable("listingId") Long listingId) {
        return "redirect:/chat/" + listingId;
    }

    @Transactional
    @MessageMapping("/chat/{listingId}")
    public void handleChatMessage(@DestinationVariable("listingId") Long listingId,
                                  @Payload Map<String, String> payload,
                                  Principal principal) {
        if (principal == null) return;

        String text = payload.get("content");
        if (text == null || text.trim().isEmpty()) return;

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено: " + listingId));
        User sender = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + principal.getName()));

        ChatMessage message = new ChatMessage();
        message.setListing(listing);
        message.setSender(sender);
        message.setContent(text);
        message.setRead(false);

        ChatMessage savedMessage = chatMessageRepository.save(message);

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
        List<ChatMessage> allMessages = chatMessageRepository.findAllMyMessages(username);

        List<Map<String, Object>> chatListWithNotifications = allMessages.stream()
                .map(ChatMessage::getListing)
                .filter(Objects::nonNull)
                .distinct()
                .map(listing -> {
                    long unreadCount = chatMessageRepository.countUnreadMessages(listing.getId(), username);
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

    @ResponseBody
    @GetMapping("/api/chat/unread-count")
    public Map<String, Long> getTotalUnreadCount(Principal principal) {
        if (principal == null) {
            return Map.of("count", 0L);
        }
        long count = chatMessageRepository.countTotalUnreadMessagesForUser(principal.getName());
        return Map.of("count", count);
    }

    @ResponseBody
    @GetMapping("/api/chat/my-chats-summary")
    public List<Map<String, Object>> getMyChatsSummary(Principal principal) {
        if (principal == null) {
            return List.of();
        }

        String username = principal.getName();
        List<ChatMessage> allMessages = chatMessageRepository.findAllMyMessages(username);

        return allMessages.stream()
                .map(ChatMessage::getListing)
                .filter(Objects::nonNull)
                .distinct()
                .map(listing -> {
                    long unreadCount = chatMessageRepository.countUnreadMessages(listing.getId(), username);
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