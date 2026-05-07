package com.Connectedm.backend.domain.ai.repository;

import com.Connectedm.backend.domain.ai.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    // 특정 사용자의 모든 채팅방을 최신순으로 조회
    List<ChatSession> findByUserIdOrderByCreatedAtDesc(Long userId);
}
