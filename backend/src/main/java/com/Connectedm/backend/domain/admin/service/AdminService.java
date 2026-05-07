package com.Connectedm.backend.domain.admin.service;

import com.Connectedm.backend.domain.admin.dto.*;
import com.Connectedm.backend.domain.content.entity.ReviewStatus;
import com.Connectedm.backend.domain.content.entity.UserReview;
import com.Connectedm.backend.domain.content.repository.ContentRepository;
import com.Connectedm.backend.domain.content.repository.UserReviewRepository;
import com.Connectedm.backend.domain.content.service.ReviewService;
import com.Connectedm.backend.domain.user.entity.UserStatus;
import com.Connectedm.backend.domain.user.repository.LoginLogRepository;
import com.Connectedm.backend.domain.user.repository.ReviewReportRepository;
import com.Connectedm.backend.domain.user.repository.UserRepository;
import com.Connectedm.backend.domain.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminService {

    private final UserReviewRepository userReviewRepository;
    private final UserRepository userRepository;
    private final ReviewService reviewService;
    private final UserService userService;
    private final LoginLogRepository loginLogRepository;
    private final ContentRepository contentRepository;
    private final ReviewReportRepository reviewReportRepository;

    // ... (기존 조회 메서드들 생략 - 유지됨) ...

    public List<AdminReviewResponseDto> getReportedReviews() {
        return userReviewRepository.findAllByReportCountGreaterThanOrderByReportCountDesc(0)
                .stream()
                .map(review -> AdminReviewResponseDto.builder()
                        .reviewId(review.getId())
                        .writeNickname(review.getUser().getNickname())
                        .movieTitle(review.getContent().getTitle())
                        .comment(review.getComment())
                        .reportCount(review.getReportCount())
                        .status(review.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    public AdminReviewReportResponseDto getReviewReportDetails(Long reviewId) {
        UserReview review = userReviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));

        List<AdminReviewReportDetailDto> reportDetails = reviewReportRepository.findAllByReviewIdWithReporter(reviewId)
                .stream()
                .map(reviewReport -> AdminReviewReportDetailDto.builder()
                        .reporterNickname(reviewReport.getReporter().getNickname())
                        .reason(reviewReport.getReason().getDescription())
                        .detailReason(reviewReport.getDetailReason())
                        .reportedAt(reviewReport.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return AdminReviewReportResponseDto.builder()
                .reviewId(review.getId())
                .movieTitle(review.getContent().getTitle())
                .writerNickname(review.getUser().getNickname())
                .reviewComment(review.getComment())
                .reportDetails(reportDetails)
                .build();
    }

    public List<AdminUserResponseDto> getReportedUsers() {
        return userRepository.findAllByReportedCountGreaterThanOrderByReportedCountDesc(0)
                .stream()
                .map(user -> AdminUserResponseDto.builder()
                        .userId(user.getId())
                        .email(user.getEmail())
                        .nickname(user.getNickname())
                        .reportedCount(user.getReportedCount())
                        .status(user.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    public Page<AdminUserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAllUserStats(pageable);
    }

    public List<LoginLogResponseDto> getLoginLogs() {
        return loginLogRepository.findAll(Sort.by(Sort.Direction.DESC))
                .stream()
                .map(log -> LoginLogResponseDto.builder()
                        .logId(log.getId())
                        .userEmail(log.getUser().getEmail())
                        .userNickname(log.getUser().getNickname())
                        .loginAt(log.getLoginAt())
                        .ipAddress(log.getIpAddress())
                        .deviceInfo(log.getDeviceInfo())
                        .build())
                .collect(Collectors.toList());
    }

    public List<AdminContentStateResponseDto> getContentStats() {
        return contentRepository.findAll(Sort.by(Sort.Direction.DESC, "viewCount"))
                .stream()
                .map(content -> AdminContentStateResponseDto.builder()
                        .contentId(content.getId())
                        .title(content.getTitle())
                        .viewCount(content.getViewCount())
                        .wishCount(content.getWishCount())
                        .build())
                .collect(Collectors.toList());
    }

    // ==========================================================
    // 2. [변경/처분]
    // ==========================================================

    @Transactional
    public void updateReviewStatus(Long reviewId, ReviewStatus status) {
        UserReview review = userReviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰 없음"));
        review.changeStatusByAdmin(status);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        reviewService.deleteUserReview(0L, "ROLE_ADMIN", reviewId);
    }

    @Transactional
    public void updateUserStatus(Long userId, UserStatus status) {
        userService.updateUserStatus(userId,status);
    }

    // ✨ [DELETE] 유저 삭제 로직 (추가된 부분)
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("해당 유저를 찾을 수 없습니다. ID: " + userId);
        }
        // User 엔티티의 CascadeType.ALL 설정 덕분에
        // 리뷰, 위시리스트 등은 자동으로 함께 삭제됩니다.
        userRepository.deleteById(userId);
    }

    @Transactional
    public void withdrawUser(Long userId) {
        // AdminService 안에서도 UserService를 주입받아 사용하거나,
        // 여기서 직접 User의 상태를 변경하는 로직을 구현합니다.[cite: 5]
        userService.withdraw(userId);
    }
}