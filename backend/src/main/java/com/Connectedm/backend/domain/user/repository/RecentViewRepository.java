package com.Connectedm.backend.domain.user.repository;

import com.Connectedm.backend.domain.content.entity.Content;
import com.Connectedm.backend.domain.user.entity.RecentView;
import com.Connectedm.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.module.Configuration;
import java.util.List;
import java.util.Optional;

public interface RecentViewRepository extends JpaRepository<RecentView, Long> {

    // 1. 특정 유저의 열람 기록을 최신순으로(DESC)
    List<RecentView> findByUserOrderByViewedAtDesc(User user);

    // 2. UPSERT 로직을 위해 특정 유저가 특정 콘텐츠를 열람한 적 있는지 확인
    Optional<RecentView> findTop1ByUserAndContentOrderByViewedAtDesc(User user, Content content);

    // 3. 유저별 기록 개수 제한할 때 필요한 카운트 쿼리
    Long countByUser(User user);

    // 4. 가장 오래된 기록 하나 찾기 (서비스에서 안전하게 지우기 위함)
    Optional<RecentView> findFirstByUserOrderByViewedAtAsc(User user);
}