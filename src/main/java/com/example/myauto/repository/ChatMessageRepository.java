package com.example.myauto.repository;

import com.example.myauto.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Получить историю сообщений для конкретной машины, отсортированную по времени
    List<ChatMessage> findByListingIdOrderBySentAtAsc(Long listingId);

    // Найти все сообщения, где пользователь является либо отправителем, либо владельцем машины
    @Query("SELECT m FROM ChatMessage m WHERE m.sender.username = :username OR m.listing.user.username = :username ORDER BY m.sentAt DESC")
    List<ChatMessage> findAllMyMessages(@Param("username") String username);
}