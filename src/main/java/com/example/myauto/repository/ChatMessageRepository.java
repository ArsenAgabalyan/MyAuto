package com.example.myauto.repository;

import com.example.myauto.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByListingIdOrderBySentAtAsc(Long listingId);

    @Query("SELECT m FROM ChatMessage m WHERE m.sender.username = :username OR m.listing.user.username = :username ORDER BY m.sentAt DESC")
    List<ChatMessage> findAllMyMessages(@Param("username") String username);

    // Счетчик для конкретного чата (Исключаем свои сообщения)
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.listing.id = :listingId AND m.sender.username != :currentUsername AND m.isRead = false")
    long countUnreadMessages(@Param("listingId") Long listingId, @Param("currentUsername") String currentUsername);

    // Общий счетчик для главной страницы по ВСЕМ чатам (Исключаем свои сообщения)
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE (m.listing.user.username = :username OR m.sender.username = :username) AND m.sender.username != :username AND m.isRead = false")
    long countTotalUnreadMessagesForUser(@Param("username") String username);

    // Пометка прочитанными при входе в конкретный чат
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.listing.id = :listingId AND m.sender.username != :currentUsername")
    void markMessagesAsRead(@Param("listingId") Long listingId, @Param("currentUsername") String currentUsername);
}