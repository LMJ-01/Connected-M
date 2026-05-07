package com.Connectedm.backend.domain.ai.controller;

import com.Connectedm.backend.domain.ai.dto.ChatRequest;
import com.Connectedm.backend.domain.ai.dto.ChatResponse;
import com.Connectedm.backend.domain.ai.service.AiService;
import com.Connectedm.backend.domain.content.dto.AiAnalysisResponseDto;
import com.Connectedm.backend.domain.user.entity.User;
import com.Connectedm.backend.domain.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final UserRepository userRepository;

    /**
     * 1. 모든 영화의 분석 데이터 통합 조회
     * GET /api/ai/keywords
     */
    @GetMapping("/keywords")
    public ResponseEntity<List<AiAnalysisResponseDto>> getAllKeywords() {
        return ResponseEntity.ok(aiService.getAllAnalysisData());
    }

    /**
     * 2. 개별 영화의 분석 데이터 조회
     * GET /api/ai/analysis/{contentId}
     */
    @GetMapping("/analysis/{contentId}")
    public ResponseEntity<AiAnalysisResponseDto> getMovieAnalysis(@PathVariable Long contentId) {
        // 🚩 이제 aiService에 이 메서드가 있어서 빨간 줄이 사라집니다!
        return ResponseEntity.ok(aiService.getMovieAnalysis(contentId));
    }

    /**
     * 3. 챗봇 추천
     */
    @PostMapping("/recommend")
    public ResponseEntity<ChatResponse> recommendMovie(
            @RequestBody ChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(aiService.processChat(request, userId));
    }

    private Long resolveUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElse(null);
    }
}