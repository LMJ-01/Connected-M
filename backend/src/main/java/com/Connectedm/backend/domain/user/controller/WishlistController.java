package com.Connectedm.backend.domain.user.controller;

import com.Connectedm.backend.domain.user.dto.WishlistResponse;
import com.Connectedm.backend.domain.user.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    /**
     * [찜 추가/해제 통합] ㅋㅋㅋㅋ
     * POST /api/members/wishlist/{contentId}
     */
    @PostMapping("/{contentId}")
    public ResponseEntity<String> toggleWishlist(
            // 🚀 [핵심 수정] CustomUserDetails 객체에서 userId 필드를 추출하도록 expression 추가
            @AuthenticationPrincipal(expression = "userId") Long userId,
//            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable Long contentId) {

        String result = wishlistService.toggleWishlist(userId, contentId);
        return ResponseEntity.ok(result);
    }

    /**
     * [내 위시리스트 조회] ㅋㅋㅋㅋ
     * GET /api/members/wishlist
     */
    @GetMapping
    public ResponseEntity<List<WishlistResponse>> getMyWishlist(
            @AuthenticationPrincipal(expression = "userId") Long userId) {

        List<WishlistResponse> responses = wishlistService.getMyWishlist(userId);
        return ResponseEntity.ok(responses);
    }
}