package com.example.myauto.repository;

import com.example.myauto.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // ИСПРАВЛЕННЫЙ ИМПОРТ
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByListingIdOrderBySentAtAsc(Long listingId);

    @Query("SELECT m FROM ChatMessage m WHERE m.sender.username = :username OR m.listing.user.username = :username ORDER BY m.sentAt DESC")
    List<ChatMessage> findAllMyMessages(@Param("username") String username); // ИСПРАВЛЕННАЯ АННОТАЦИЯ
}