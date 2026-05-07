package com.Connectedm.backend.domain.content.repository;

import com.Connectedm.backend.domain.content.entity.ReviewStatus;
import com.Connectedm.backend.domain.content.entity.UserReview;
import com.Connectedm.backend.domain.user.entity.User; // 👈 이 줄을 꼭 추가해야 합니다!
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface UserReviewRepository extends JpaRepository<UserReview, Long> {

    // 특정 영화의 유저 리뷰들을 최신순으로 가져오기
    List<UserReview> findByContentId(Long contentId);

    // 특정 유저가 쓴 리뷰들만 모아보기 - 마이페이지용
    List<UserReview> findByUserId(Long userId);

    // 특정유저가 쓴 리뷰의 갯수
    Long countByUserId(Long userId);

    // [핵심 추가] 1인 1영화 1리뷰 검증용
    boolean existsByUserIdAndContentId(Long userId, Long contentId);

    // 내 리뷰 전체 삭제를 위한 메서드
    @Modifying
    @Transactional
    @Query("DELETE FROM UserReview ur WHERE ur.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    // 👈 새로 추가할 메서드 (손님이 부르는 이름으로 메뉴 추가!)
    @Modifying
    @Transactional
    void deleteByUser(User user);
    // 정상 노출 상태인 리뷰만 가져오기
    List<UserReview> findAllByContentIdAndStatus(Long contentId, ReviewStatus status);



    // 일반 관람객 평점 평균 계산
    @Query("SELECT AVG(CAST(r.rating AS double)) FROM UserReview r WHERE r.content.id = :contentId")
    Double getAverageRatingByContentId(@Param("contentId") Long contentId);

    // [관리자용] 신고 0회 초과인 리뷰만 긁어오기
    List<UserReview> findAllByReportCountGreaterThanOrderByReportCountDesc(int count);
}
