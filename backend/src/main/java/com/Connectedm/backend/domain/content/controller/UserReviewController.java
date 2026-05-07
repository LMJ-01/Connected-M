package com.Connectedm.backend.domain.content.controller;

import com.Connectedm.backend.domain.content.dto.MyPageReviewResponseDto;
import com.Connectedm.backend.domain.content.dto.ReviewReportRequestDto;
import com.Connectedm.backend.domain.content.dto.UserReviewRequestDto;
import com.Connectedm.backend.domain.content.service.ReviewService;
import com.Connectedm.backend.global.common.ApiResponse;
import com.Connectedm.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api") // 명세서의 공통 경로
@Tag(name = "UserReview API", description = "유저 리뷰 전용 컨트롤러 (작성/수정/삭제/마이페이지)")
public class UserReviewController {

    private final ReviewService reviewService;

    /**
     * [유저 리뷰 등록] POST /api/contents/user-reviews
     * 명세: [중복방지] 적용 완료!
     */
    @PostMapping("/contents/user-reviews")
    @Operation(summary = "리뷰 작성 ", description = "영화 상세페이지에서 별점과 코멘트를 남깁니다.")
    public ApiResponse<String> saveUserReview(
            @RequestParam Long contentId,
            @AuthenticationPrincipal(expression = "userId") Long userId, // 🚀 expression 추가
            @RequestBody UserReviewRequestDto dto) {

        reviewService.saveUserReview(userId, contentId, dto);
        return ApiResponse.success("리뷰가 등록되었습니다!");
    }

    /**
     * [유저 리뷰 수정] PUT /api/contents/user-reviews/{id}
     * 명세: [본인검증] 작성자와 JWT 유저 대조 로직
     */
    @PutMapping("/contents/user-reviews/{id}")
    @Operation(summary = "리뷰 수정 ", description = "내가 작성한 리뷰를 수정합니다.")
    public ApiResponse<String> updateUserReview(
            @PathVariable("id") Long reviewId,
            @AuthenticationPrincipal(expression = "userId") Long userId, // 🚀 expression 추가
            @RequestBody UserReviewRequestDto dto) {

        reviewService.updateUserReview(userId, reviewId, dto);
        return ApiResponse.success("리뷰가 수정되었습니다!");
    }

    /**
     * [유저 리뷰 삭제] DELETE /api/contents/user-reviews/{id}
     * 명세: [권한분기] 본인 혹은 관리자
     */
    @DeleteMapping("/contents/user-reviews/{id}")
    @Operation(summary = "리뷰 삭제 ", description = "리뷰를 삭제합니다. ")
    public ApiResponse<String> deleteUserReview(
            @PathVariable("id") Long reviewId,
            @AuthenticationPrincipal(expression = "userId") Long userId) { // 🚀 expression 추가

        String role = "USER";
        reviewService.deleteUserReview(userId, role, reviewId);
        return ApiResponse.success("리뷰가 삭제되었습니다!");
    }

    /**
     * [내 리뷰 목록 조회] GET /api/user-reviews/me
     * 명세: [마이페이지] 내가 쓴 리뷰만
     */
    @GetMapping("/user-reviews/me")
    @Operation(summary = "내 리뷰 조회 ", description = "마이페이지에서 내가 쓴 리뷰 목록을 가져옵니다.")
    public ApiResponse<List<MyPageReviewResponseDto>> getMyReviews(
            @AuthenticationPrincipal(expression = "userId") Long userId) { // 🚀 expression 추가 (💡 진짜 유저 ID 주입 주석 보존)

        return ApiResponse.success(reviewService.getMyReviews(userId));
    }

    /**
     * [내 리뷰 전체 삭제]
     */
    @DeleteMapping("/user-reviews/me")
    @Operation(summary = "내 리뷰 전체 삭제", description = "마이페이지에서 내가 쓴 모든 리뷰를 삭제합니다.")
    public ApiResponse<String> deleteAllMyReviews(@AuthenticationPrincipal(expression = "userId") Long userId) { // 🚀 expression 추가
        reviewService.deleteAllUserReviews(userId);
        return ApiResponse.success("모든 리뷰가 삭제되었습니다!");
    }

    /**
     * [POST] 특정 리뷰 신고
     */
    @PostMapping("/contents/reviews/{id}/report")
    @Operation(summary = "리뷰 신고", description = "부적절한 리뷰를 신고합니다. 신고 사유와 상세 내용을 포함할 수 있습니다.")
    public ApiResponse<String> reportReview(
            @PathVariable("id") Long reviewId,
            @AuthenticationPrincipal(expression = "userId") Long userId, // 🚀 expression 추가 (💡 신고자 ID 소환! 주석 보존)
            @RequestBody ReviewReportRequestDto requestDto) {

        reviewService.reportReview(userId, reviewId, requestDto);

        return ApiResponse.success("신고가 정상적으로 접수되었습니다. "); // 공통 폼 적용
    }
}