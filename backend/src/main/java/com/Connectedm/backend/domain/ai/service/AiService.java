package com.Connectedm.backend.domain.ai.service;

import com.Connectedm.backend.domain.ai.dto.ChatRequest;
import com.Connectedm.backend.domain.ai.dto.ChatResponse;
import com.Connectedm.backend.domain.ai.entity.ChatMessage;
import com.Connectedm.backend.domain.ai.entity.ChatSession;
import com.Connectedm.backend.domain.ai.repository.ChatMessageRepository;
import com.Connectedm.backend.domain.ai.repository.ChatSessionRepository;
import com.Connectedm.backend.domain.content.dto.AiAnalysisResponseDto;
import com.Connectedm.backend.domain.content.entity.AnalysisCache;
import com.Connectedm.backend.domain.content.repository.AnalysisCacheRepository;
import com.Connectedm.backend.domain.user.entity.User;
import com.Connectedm.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final AnalysisCacheRepository analysisCacheRepository;

    @Value("${external-api.python-ai-url}")
    private String pythonAiUrl;

    /**
     * [신규] 모든 영화의 분석 데이터를 리스트로 반환 ( /keywords 용 )
     */
    @Transactional(readOnly = true)
    public List<AiAnalysisResponseDto> getAllAnalysisData() {
        return analysisCacheRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    /**
     * [신규] 특정 영화의 분석 데이터 조회 ( /analysis/{contentId} 용 )
     */
    @Transactional(readOnly = true)
    public AiAnalysisResponseDto getMovieAnalysis(Long contentId) {
        return analysisCacheRepository.findByContentId(contentId)
                .map(this::convertToDto)
                .orElseGet(() -> AiAnalysisResponseDto.builder()
                        .contentId(contentId)
                        .keywords(List.of("데이터 준비 중"))
                        .summary("분석 데이터가 없습니다.")
                        .positiveRatio(0)
                        .build());
    }

    /**
     * DTO 변환 헬퍼 메서드 (중복 코드 방지)
     * AnalysisCache 엔티티에서 연관된 Content 엔티티를 참조하여 포스터 정보를 가져옵니다.
     */
    private AiAnalysisResponseDto convertToDto(AnalysisCache cache) {
        // Content 엔티티가 존재할 경우 포스터 URL을 가져오고, 없으면 null 처리
        String posterUrl = (cache.getContent() != null) ? cache.getContent().getPosterPath() : null;

        return AiAnalysisResponseDto.builder()
                .contentId(cache.getContent() != null ? cache.getContent().getId() : null)
                .keywords(parseKeywords(cache))
                .summary(cache.getSummary())
                .positiveRatio((int) cache.getPositiveRatio())
                .posterPath(posterUrl) // <-- 빌더에 포스터 URL 추가
                .build();
    }

    private List<String> parseKeywords(AnalysisCache cache) {
        String keywords = cache.getTopKeywords();
        if (keywords == null || keywords.isBlank()) return Collections.emptyList();
        return Arrays.stream(keywords.split(","))
                .map(String::trim)
                .filter(word -> !word.isEmpty())
                .toList();
    }

    // --- 기존 챗봇 로직 (유지) ---
    @Transactional
    public ChatResponse processChat(ChatRequest request, Long userId) {
        ChatSession session = getOrCreateSession(request.getSessionId(), userId, request.getPrompt());
        String aiReply = callPythonAiServer(request.getPrompt(), session.getId(), userId);
        if (userId != null) saveChatMessage(session, request.getPrompt(), aiReply);
        return new ChatResponse(aiReply);
    }

    private String callPythonAiServer(String prompt, Long sessionId, Long userId) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> requestBody = Map.of("user_id", userId != null ? userId : 0, "session_id", sessionId, "prompt", prompt);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(pythonAiUrl, entity, Map.class);
            return (String) response.getBody().getOrDefault("answer", "응답 오류");
        } catch (Exception e) { return "서버 연결 실패"; }
    }

    private ChatSession getOrCreateSession(Long sessionId, Long userId, String prompt) {
        if (sessionId != null) return chatSessionRepository.findById(sessionId).orElseGet(() -> createNewSession(userId, prompt));
        return createNewSession(userId, prompt);
    }

    private ChatSession createNewSession(Long userId, String prompt) {
        User user = userRepository.findById(userId).orElseThrow();
        return chatSessionRepository.save(ChatSession.builder().user(user).sessionTitle(prompt.substring(0, Math.min(prompt.length(), 15))).build());
    }

    private void saveChatMessage(ChatSession session, String userMsg, String aiMsg) {
        chatMessageRepository.save(ChatMessage.builder().chatSession(session).role("user").message(userMsg).build());
        chatMessageRepository.save(ChatMessage.builder().chatSession(session).role("assistant").message(aiMsg).build());
    }
}