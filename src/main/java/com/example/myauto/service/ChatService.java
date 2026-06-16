package com.example.myauto.service;

import com.example.myauto.entity.ChatMessage;
import com.example.myauto.entity.Listing;
import com.example.myauto.entity.User;
import com.example.myauto.repository.ChatMessageRepository;
import com.example.myauto.repository.ListingRepository;
import com.example.myauto.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;

    public ChatService(ChatMessageRepository chatMessageRepository,
                       UserRepository userRepository,
                       ListingRepository listingRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
    }

    public List<ChatMessage> getMessagesForListing(Long listingId) {
        return chatMessageRepository.findByListingIdOrderBySentAtAsc(listingId);
    }

    public ChatMessage saveMessage(Long listingId, String username, String content) {
        User sender = userRepository.findByUsername(username).orElseThrow();
        Listing listing = listingRepository.findById(listingId).orElseThrow();

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setListing(listing);
        message.setContent(content);
        return chatMessageRepository.save(message);
    }
}