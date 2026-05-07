package com.Connectedm.backend.domain.content.service;

import java.util.List;
import java.util.stream.Collectors;

import com.Connectedm.backend.domain.user.entity.ReportReason;
import com.Connectedm.backend.domain.user.entity.ReviewReport;
import com.Connectedm.backend.domain.user.repository.ReviewReportRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Connectedm.backend.domain.content.dto.*; // DTO들 소환 ㅋ
import com.Connectedm.backend.domain.content.entity.*;
import com.Connectedm.backend.domain.content.repository.*;
import com.Connectedm.backend.domain.user.entity.User;
import com.Connectedm.backend.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {

    private final ExpertReviewRepository expertReviewRepository;
    private final UserReviewRepository userReviewRepository;
    private final Cine21SourceRepository cine21SourceRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final ReviewReportRepository reviewReportRepository;

    // ==========================================================
    // 1. 전문가 리뷰 영역 (크롤링 데이터)
    // ==========================================================

    @Transactional
    public void saveExpertReview(ExpertReviewCreateRequestDto dto) {
        Cine21Source source = cine21SourceRepository.findByExternalMovieId(dto.getExternalMovieId())
                .orElseGet(() -> cine21SourceRepository.save(
                        Cine21Source.builder()
                                .externalMovieId(dto.getExternalMovieId())
                                .rawTitle(dto.getRawTitle())
                                .build()
                ));

        Content content = contentRepository.findById(dto.getContentId())
                .orElseThrow(() -> new RuntimeException("콘텐츠 없음 ID:" + dto.getContentId()));

        ExpertReview review = ExpertReview.builder()
                .content(content)
                .cine21Source(source)
                .criticName(dto.getCriticName())
                .rating(dto.getRating())
                .comment(dto.getComment())
                .build();

        expertReviewRepository.save(review);
    }

    public List<ReviewResponseDto> getExpertReviews(Long contentId) {
        return expertReviewRepository.findByContentId(contentId).stream()
                .map(review -> ReviewResponseDto.builder()
                        .id(review.getId())
                        .criticName(review.getCriticName())
                        .comment(review.getComment())
                        .rating(review.getRating())
                        .sourceName(review.getSource())
                        .movieTitle(review.getContent().getTitle())
                        .build())
                .collect(Collectors.toList());
    }

    // ==========================================================
    // 2. Connected-M 유저 리뷰 영역 (우리 유저 전용)
    // ==========================================================

    /**
     * [조회] 영화 상세페이지용 유저 리뷰 목록 ㅋ
     */
    public List<UserReviewResponseDto> getUserReviews(Long contentId) {
        return userReviewRepository.findByContentId(contentId).stream()
                .map(UserReviewResponseDto::from) // 👈 마스터의 from 메서드 '딸깍'! ㅋ
                .collect(Collectors.toList());
    }

    /**
     * [조회] 마이페이지용 '내가 쓴' 리뷰 목록 ㅋ
     */
    public List<MyPageReviewResponseDto> getMyReviews(Long userId) {
        return userReviewRepository.findByUserId(userId).stream()
                .map(MyPageReviewResponseDto::from) // 👈 여기도 from으로 통일! ㅋㅋㅋㅋ
                .collect(Collectors.toList());
    }

    /**
     * [저장] 유저 리뷰 저장 (상세페이지 -> DB)
     */
    @Transactional
    public void saveUserReview(Long userId, Long contentId, UserReviewRequestDto dto) {
        System.out.println("DEBUG: userId=" + userId + ", contentId=" + contentId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 컨텐츠입니다."));

        // [중복 방지] 1인 1영화 1리뷰 원칙!
        // Repository에 existsByUserIdAndContentId가 완성되면 아래 주석을 푸세요 ㅋ

        if (userReviewRepository.existsByUserIdAndContentId(userId, contentId)) {
            throw new IllegalStateException("이미 리뷰를 작성한 영화입니다!");
        }


        UserReview userReview = UserReview.builder()
                .user(user)
                .content(content)
                .rating(dto.getRating()) // 프론트 별점 애니메이션 점수 수신 ㅋ
                .comment(dto.getComment())
                .status(ReviewStatus.NORMAL)
                .build();

        userReviewRepository.save(userReview);
    }
    public List<ReviewResponseDto> getAllExpertReviews() {
        return expertReviewRepository.findAll().stream()
                .map(review -> ReviewResponseDto.builder()
                        .id(review.getId())
                        .criticName(review.getCriticName())
                        .rating(review.getRating())
                        .comment(review.getComment())
                        .sourceName(review.getSource()) // 씨네21 등 출처 ㅋ
                        .movieTitle(review.getContent().getTitle())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * [수정] 유저 리뷰 수정 (본인검증 ㅋ)
     */
    @Transactional
    public void updateUserReview(Long userId, Long reviewId, UserReviewRequestDto dto) {
        UserReview review = userReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        // [본인검증] 리뷰 작성자와 현재 로그인 유저가 같은지 확인!
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalStateException("본인이 작성한 리뷰만 수정할 수 있습니다!");
        }

        // 변경 감지(Dirty Check)로 업데이트
        // UserReview 엔티티에 update 메서드가 있으면 더 좋습니다
        // review.update(dto.getRating(), dto.getComment());
        review.update(dto.getRating(), dto.getComment());

        userReviewRepository.save(review);
    }

    /**
     * [삭제] 유저 리뷰 삭제 (권한분기: 본인 OR 관리자 ㅋ)
     */
    @Transactional
    public void deleteUserReview(Long userId, String userRole, Long reviewId) {
        UserReview review = userReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        // [권한분기] 1. 본인이거나 2. 관리자(ADMIN)거나
        boolean isOwner = review.getUser().getId().equals(userId);
        boolean isAdmin = "ADMIN".equals(userRole);

        if (!isOwner && !isAdmin) {
            throw new IllegalStateException("삭제 권한이 없습니다!");
        }

        userReviewRepository.delete(review);
    }

    @Transactional
    public void deleteAllUserReviews(Long userId) {
        // userId에 해당하는 리뷰들만 찾아서 싹 다 밀어버리기 ㅋ
        userReviewRepository.deleteAllByUserId(userId);
    }

    /**
     *  영화 상세페이지용 리뷰 통계 데이터 생성
     */
    public ReviewStatsResponseDto getReviewStats(Long contentId) {
        Double userAvg = userReviewRepository.getAverageRatingByContentId(contentId);
        Double expertAvg = expertReviewRepository.getAverageRatingByContentId(contentId);
        long expertCount = expertReviewRepository.countByContentId(contentId);

        return ReviewStatsResponseDto.builder()
                .userRatingAvg(userAvg != null ? Math.round(userAvg * 10) / 10.0 : 0.0)
                .expertRatingAvg(expertAvg != null ? Math.round(expertAvg * 10) / 10.0 : 0.0)
                .expertReviewCount(expertCount)
                .build();
    }

    @Transactional
    public void reportReview(Long userId, Long reviewId, ReviewReportRequestDto dto) {
        // 1. 중복 신고 방지 (사령관의 꼼꼼함! ㅋ)
        if (reviewReportRepository.existsByReporterIdAndReviewId(userId, reviewId)) {
            throw new IllegalStateException("이미 신고한 리뷰입니다! ㅋ");
        }

        // 2. 대상 소환 (리뷰, 신고자)
        UserReview userReview = userReviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰가 없습니다."));
        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 3. 기록 박제 (ReviewReport 저장)
        ReviewReport report = ReviewReport.builder()
                .reporter(reporter)
                .review(userReview)
                .reason(ReportReason.valueOf(dto.getReason())) // Enum 변환 ㅋ
                .detailReason(dto.getDetailReason()) // 필요시 추가 ㅋ
                .build();
        reviewReportRepository.save(report);

        // 4. 연쇄 카운트 업 (기존 로직 유지 ㅋ)
        userReview.increaseReportCount();
        User writer = userReview.getUser();
        if (writer != null) {
            writer.increaseReportedCount();
        }
    }
}