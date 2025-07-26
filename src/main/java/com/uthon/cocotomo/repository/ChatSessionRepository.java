package com.uthon.cocotomo.repository;

import com.uthon.cocotomo.entity.ChatSession;
import com.uthon.cocotomo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    
    Optional<ChatSession> findBySessionId(String sessionId);
    
    List<ChatSession> findByUserOrderByCreatedAtDesc(User user);
    
    List<ChatSession> findByUserAndCreatedAtAfterOrderByCreatedAtDesc(User user, LocalDateTime dateTime);
    
    Optional<ChatSession> findByUserAndCurrentStageNot(User user, ChatSession.ChatStage stage);
}