package com.Connectedm.backend.domain.ai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
/**
 * 챗봇 세션 내에서 사용자와 AI가 주고받은 개별 메시지를 저장하는 엔티티입니다.
 */
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소속된 채팅방 (N:1 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession chatSession;

    @Column(nullable = false, length = 20)
    private String role; // "user" 또는 "assistant" (AI)

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message; // 실제 대화 내용

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
