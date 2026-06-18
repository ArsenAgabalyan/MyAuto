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

    @Transactional
    public List<ChatMessage> getMessagesForListingAndMarkAsRead(Long listingId, String currentUsername) {
        chatMessageRepository.markMessagesAsRead(listingId, currentUsername);
        return chatMessageRepository.findByListingIdOrderBySentAtAsc(listingId);
    }

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
        message.setRead(false);

        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getMessagesForUser(String username) {
        return chatMessageRepository.findAllMyMessages(username);
    }

    public long getUnreadCount(Long listingId, String currentUsername) {
        return chatMessageRepository.countUnreadMessages(listingId, currentUsername);
    }

    public long getTotalUnreadCount(String username) {
        return chatMessageRepository.countTotalUnreadMessagesForUser(username);
    }
}