package com.Connectedm.backend.domain.user.repository;

import com.Connectedm.backend.domain.user.entity.User;
// User 엔티티 내부에 선언된 AuthProvider(LOCAL, KAKAO, GOOGLE)를 가져옵니다.
import com.Connectedm.backend.domain.user.entity.User.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.Connectedm.backend.domain.admin.dto.AdminUserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ==========================================================
    // 1. 회원가입 및 로그인용 (중복 체크)
    // ==========================================================

    // 이메일로 유저 찾기 (로그인 시 사용)
    Optional<User> findByEmail(String email);

    Optional<User> findByProviderId(String providerId);

    // 이메일 중복 확인
    boolean existsByEmail(String email);

    // 닉네임 중복 확인
    boolean existsByNickname(String nickname);

    // 전화번호 중복 확인 (회원가입 시 010-1234-1234 중복 방지용)
    boolean existsByPhoneNumber(String phoneNumber);


    // ==========================================================
    // 2. 비밀번호 찾기 (본인 인증용)
    // ==========================================================

    // 이메일 + 실명 + 전화번호가 모두 일치하는 유저 찾기 (3중 검증)
    Optional<User> findByEmailAndRealNameAndPhoneNumber(String email, String realName, String phoneNumber);

    // 비밀번호 재설정 토큰으로 유저 찾기 (토큰 검증용)
    Optional<User> findByPasswordResetToken(String resetToken);

    // ==========================================================
    // 3. 소셜 로그인용 (카카오, 구글)
    // ==========================================================

    // 소셜 서비스 공급자(provider)와 해당 서비스의 고유 ID(providerId)로 유저 찾기
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    List<User> findAllByReportedCountGreaterThanOrderByReportedCountDesc(int count);

    // ==========================================================
    // 4. 어드민 전용 (통계 및 관리용)
    // ==========================================================

    /**
     * 유저 목록 조회 시 찜 개수와 리뷰 개수를 서브쿼리로 한 번에 가져오는 쿼리
     */
    @Query("SELECT new com.Connectedm.backend.domain.admin.dto.AdminUserResponseDto(" +
            "u.id, u.email, u.nickname, u.realName, u.phoneNumber, u.role, u.status, u.reportedCount, u.createdAt, u.lastLoginAt, " +
            "(SELECT COUNT(w) FROM Wishlist w WHERE w.user.id = u.id), " +
            "(SELECT COUNT(r) FROM UserReview r WHERE r.user.id = u.id)) " + //
            "FROM User u")
    Page<AdminUserResponseDto> findAllUserStats(Pageable pageable);
}