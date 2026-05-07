package com.Connectedm.backend.domain.user.controller;

import com.Connectedm.backend.domain.content.entity.Content;
import com.Connectedm.backend.domain.content.service.ContentService;
import com.Connectedm.backend.domain.user.dto.RecentViewResponseDto;
import com.Connectedm.backend.domain.user.entity.User;
import com.Connectedm.backend.domain.user.repository.UserRepository;
import com.Connectedm.backend.domain.user.service.RecentViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/recent")
public class RecentViewController {
    private final RecentViewService recentViewService;
    private final ContentService contentService;
    private final UserRepository userRepository;

    @PostMapping("/{contentId}")
    public ResponseEntity<?> saveOrUpdateRecentView(
            @AuthenticationPrincipal Object principal, // User 대신 Object로 변경
            @PathVariable Long contentId) {

        try {
            // 1. 유저 ID 추출 (토큰 sub에 들어있던 값)
            Long userId = extractUserId(principal);
            if (userId == null) {
                log.error("인증 정보가 없거나 유효하지 않습니다.");
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }

            // 2. DB에서 실제 유저 조회
            User persistentUser = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("DB에 해당 유저가 없습니다. ID: " + userId));

            // 3. 영화 정보 조회
            Content content = contentService.findById(contentId);

            // 4. 서비스 호출 (기존 코드 그대로)
            recentViewService.saveOrUpdateRecentView(persistentUser, content);
            log.info("최근 본 목록 저장 성공! 유저ID: {}, 영화: {}", userId, content.getTitle());

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("저장 중 에러 발생: ", e);
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getRecentViews(@AuthenticationPrincipal Object principal) {
        try {
            Long userId = extractUserId(principal);
            if (userId == null) return ResponseEntity.status(401).build();

            User persistentUser = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            List<RecentViewResponseDto> response = recentViewService.getRecentViews(persistentUser).stream()
                    .map(RecentViewResponseDto::new).collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * principal에서 유저 ID를 안전하게 추출하는 헬퍼 메서드
     */
    private Long extractUserId(Object principal) {
        if (principal == null || principal.equals("anonymousUser")) return null;

        if (principal instanceof User) {
            return ((User) principal).getId();
        } else if (principal instanceof com.Connectedm.backend.global.security.CustomUserDetails) {
            // 🚀 [여기만 추가됨!] 새로 바뀐 인증 객체 처리
            return ((com.Connectedm.backend.global.security.CustomUserDetails) principal).getUserId();
        } else if (principal instanceof String) {
            // 토큰 sub의 "2" 같은 문자열 처리
            return Long.parseLong((String) principal);
        } else {
            // 숫자로 바로 넘어올 경우 처리
            return Long.valueOf(String.valueOf(principal));
        }
    }
}