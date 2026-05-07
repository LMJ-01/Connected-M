package com.Connectedm.backend.domain.user.repository;

import com.Connectedm.backend.domain.user.entity.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {

    // 1. 특정 리뷰에 대한 모든 신고 내역 조회
    @Query("SELECT r FROM ReviewReport r " +
            "JOIN FETCH r.reporter " +
            "WHERE r.review.id = :reviewId " +
            "ORDER BY r.createdAt DESC")
    List<ReviewReport> findAllByReviewIdWithReporter(@Param("reviewId") Long reviewId);

    // 2. 특정 유저가 신고한 내역 확인 (중복 신고 방지용 ㅋ)
    boolean existsByReporterIdAndReviewId(Long reporterId, Long reviewId);
}
