package com.Connectedm.backend.domain.admin.controller;

import com.Connectedm.backend.domain.admin.dto.*;
import com.Connectedm.backend.domain.admin.service.AdminService;
import com.Connectedm.backend.domain.content.entity.ReviewStatus;
import com.Connectedm.backend.domain.user.entity.UserStatus;
import com.Connectedm.backend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    // ==========================================================
    // 1. 유저 관리 (User Management)
    // ==========================================================

    // [GET] 전체 유저 목록 조회 (최신순)
    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserResponseDto>> getAllUsers(
            @PageableDefault(size = 1000, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    // [GET] 상습 신고 유저 조회
    @GetMapping("/users/reported")
    public ResponseEntity<List<AdminUserResponseDto>> getReportedUsers() {
        return ResponseEntity.ok(adminService.getReportedUsers());
    }

    // [PATCH] 유저 상태 변경 (ACTIVE, BANNED 등)
    @PatchMapping("/users/{id}/status")
    public ResponseEntity<Void> updateUsersStatus(
            @PathVariable Long id,
            @RequestParam UserStatus status) {
        adminService.updateUserStatus(id, status);
        return ResponseEntity.ok().build();
    }

    // ✨ [DELETE] 유저 영구 탈퇴 처리 (추가된 부분)
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{userId}/withdraw")
    public ResponseEntity<Void> adminWithdrawUser(@PathVariable("userId") Long userId) {
        adminService.withdrawUser(userId); // 👈 AdminService의 메서드를 호출
        return ResponseEntity.ok().build();
    }


    // ==========================================================
    // 2. 리뷰 및 콘텐츠 관리 (Review & Stats)
    // ==========================================================

    @GetMapping("/reviews/reported")
    public ResponseEntity<List<AdminReviewResponseDto>> getReportedReviews() {
        return ResponseEntity.ok(adminService.getReportedReviews());
    }

    @PatchMapping("/reviews/{id}/status")
    public ResponseEntity<Void> updateReviewStatus(
            @PathVariable Long id,
            @RequestParam ReviewStatus status) {
        adminService.updateReviewStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        adminService.deleteReview(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats/contents")
    public ResponseEntity<List<AdminContentStateResponseDto>> getContentStats() {
        return ResponseEntity.ok(adminService.getContentStats());
    }

    @GetMapping("/reviews/reported/{id}")
    public ResponseEntity<AdminReviewReportResponseDto> getReportedReviewDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getReviewReportDetails(id));
    }

    // ==========================================================
    // 3. 보안 및 시스템 로그 (System Logs)
    // ==========================================================

    @GetMapping("/logs/login")
    public ResponseEntity<List<LoginLogResponseDto>> getLoginLogs() {
        return ResponseEntity.ok(adminService.getLoginLogs());
    }


}