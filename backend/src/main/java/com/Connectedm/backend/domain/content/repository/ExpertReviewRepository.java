package com.Connectedm.backend.domain.content.repository;

import com.Connectedm.backend.domain.content.entity.ExpertReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpertReviewRepository extends JpaRepository<ExpertReview, Long> {
    // 특정 영화의 전문가 리뷰들만 긁어올 때
    List<ExpertReview> findByContentId(Long contentId);

    // 영화 ID로 평균 평점 계산 (String -> Double 변환 후 평균)
    @Query("SELECT AVG(CAST(e.rating AS double)) FROM ExpertReview e WHERE e.content.id = :contentId")
    Double getAverageRatingByContentId(@Param("contentId") Long contentId);

    long countByContentId(Long contentId);
}
