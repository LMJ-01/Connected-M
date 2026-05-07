package com.Connectedm.backend.domain.ai.repository;

import com.Connectedm.backend.domain.ai.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 특정 채팅방의 모든 메시지를 과거순(대화 흐름순)으로 조회
    List<ChatMessage> findByChatSessionIdOrderByCreatedAtAsc(Long sessionId);
}
