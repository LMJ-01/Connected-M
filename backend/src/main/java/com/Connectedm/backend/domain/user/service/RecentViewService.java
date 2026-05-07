package com.Connectedm.backend.domain.user.service;

import com.Connectedm.backend.domain.content.entity.Content;
import com.Connectedm.backend.domain.user.entity.RecentView;
import com.Connectedm.backend.domain.user.entity.User;
import com.Connectedm.backend.domain.user.entity.UserStatus;
import com.Connectedm.backend.domain.user.repository.RecentViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecentViewService {

    private final RecentViewRepository recentViewRepository;
    private final int MAX_RECENT_COUNT = 10;    // 최근 본 영화 개수 제한

    /**
     * 최근 열람 기록 저장 및 업데이트(UPSERT)
     */
    @Transactional
    public void saveOrUpdateRecentView(User user, Content content) {

        if (user.getStatus() == UserStatus.BANNED) {
            // 정지된 유저는 기록을 남길 권한이 없습니다.
            return;
        }

        // 🚀 [지독한 수정 포인트 1] findByUserAndContent 대신 findTop1...을 사용하여 중복 데이터 에러를 방지합니다.
        // 이미 DB에 중복 데이터가 쌓여 있어도 에러 없이 가장 최근 기록 하나만 가져옵니다.
        Optional<RecentView> existingView = recentViewRepository.findTop1ByUserAndContentOrderByViewedAtDesc(user, content);

        if (existingView.isPresent()) {
            // 2. 있으면 시간만 갱신
            existingView.get().updateViewedAt();
        } else {
            // 3. 없으면 새로 생성해서 저장
            RecentView newView = RecentView.builder()
                    .user(user)
                    .content(content)
                    .build();
            recentViewRepository.save(newView);

            // 🚀 [지독한 수정 포인트 2] 기록이 새로 추가될 때만 개수 제한 로직을 호출합니다.
            cleanUpOldRecords(user);

            if (user.getRecentViews() != null) {
                user.getRecentViews().add(newView);
            }
        }
    }

    /**
     * 유저별 기록 개수 제한 로직 (기존 코드 유지 및 확실한 삭제 보장)
     */
    private void cleanUpOldRecords(User user) {
        Long count = recentViewRepository.countByUser(user);
        // 설정한 최대 개수(10개)를 초과하면 가장 오래된 것을 지웁니다.
        if (count > MAX_RECENT_COUNT) {
            recentViewRepository.findFirstByUserOrderByViewedAtAsc(user)
                    .ifPresent(oldest -> recentViewRepository.delete(oldest));
        }
    }

    /**
     * 마이페이지용 최근 열람 목록 조회
     */
    public List<RecentView> getRecentViews(User user) {
        return recentViewRepository.findByUserOrderByViewedAtDesc(user);
    }
}