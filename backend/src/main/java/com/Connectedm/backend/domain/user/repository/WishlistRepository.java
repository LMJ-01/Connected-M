package com.Connectedm.backend.domain.user.repository;

import com.Connectedm.backend.domain.content.entity.Content;
import com.Connectedm.backend.domain.user.entity.User;
import com.Connectedm.backend.domain.user.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;    // 👈 추가 1
import org.springframework.transaction.annotation.Transactional; // 👈 추가 2
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    // 👈 이 부분을 추가해야 UserService의 빨간 줄이 사라집니다!
    @Modifying
    @Transactional
    void deleteByUser(User user);
    // 유저아이디로 찜 갯수 확인
    long countByUserId(Long userId);

    // [추가] 토글 로직용: 유저와 콘텐츠로 특정 찜 기록 찾기
    Optional<Wishlist> findByUserAndContent(User user, Content content);

    // [추가] 조회 로직용: 유저 아이디로 전체 찜 목록 긁어오기
    List<Wishlist> findAllByUserId(Long userId);
}
