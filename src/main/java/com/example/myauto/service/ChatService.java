package com.example.myauto.service;

import com.example.myauto.entity.ChatMessage;
import com.example.myauto.entity.Listing;
import com.example.myauto.entity.User;
import com.example.myauto.repository.ChatMessageRepository;
import com.example.myauto.repository.ListingRepository;
import com.example.myauto.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public ChatService(ChatMessageRepository chatMessageRepository,
                       ListingRepository listingRepository,
                       UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    // 1. Получить историю сообщений для конкретного объявления
    public List<ChatMessage> getMessagesForListing(Long listingId) {
        return chatMessageRepository.findByListingIdOrderBySentAtAsc(listingId);
    }

    // 2. Сохранить новое сообщение из WebSocket
    @Transactional
    public ChatMessage saveMessage(Long listingId, String username, String content) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено: " + listingId));
        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + username));

        ChatMessage message = new ChatMessage();
        message.setListing(listing);
        message.setSender(sender);
        message.setContent(content);

        return chatMessageRepository.save(message);
    }

    // 3. Получить все сообщения пользователя для общего списка диалогов
    public List<ChatMessage> getMessagesForUser(String username) {
        return chatMessageRepository.findAllMyMessages(username);
    }
}