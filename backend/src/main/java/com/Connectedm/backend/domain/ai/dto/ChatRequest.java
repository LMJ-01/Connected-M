package com.Connectedm.backend.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private String prompt; // 사용자가 입력한 질문
    private Long sessionId;
}