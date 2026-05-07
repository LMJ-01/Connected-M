package com.Connectedm.backend.domain.user.entity;

import com.Connectedm.backend.domain.content.entity.UserReview;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "real_name", nullable = false, length = 50)
    private String realName;

    @Column(name = "phone_number", unique = true, length = 20)
    private String phoneNumber;

    @Column(name = "password_reset_token", unique = true, length = 255)
    private String passwordResetToken;

    @Column(name = "password_reset_token_expiry")
    private LocalDateTime passwordResetTokenExpiry;

    @Column(nullable = true, length = 50)
    private String nickname;

    @CreationTimestamp
    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 1. 리뷰 삭제 설정
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserReview> reviews = new ArrayList<>();

    // ✨ 2. 로그인 로그 삭제 설정 추가 (이게 빠져서 에러 났을 확률 높음)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LoginLog> loginLogs = new ArrayList<>();

    // ✨ 3. 내가 한 신고 내역 삭제 설정 추가[cite: 6, 8]
    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewReport> reports = new ArrayList<>();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private AuthProvider provider = AuthProvider.LOCAL;

    @Column(name = "provider_id")
    private String providerId;

    public enum AuthProvider {
        LOCAL, KAKAO, GOOGLE
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.ROLE_USER;

    @Column(nullable = false)
    @Builder.Default
    private int reportedCount = 0;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // 4. 위시리스트 삭제 설정[cite: 8]
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Wishlist> wishlists = new ArrayList<>();

    // 5. 최근 본 목록 삭제 설정[cite: 8]
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecentView> recentViews = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50) // 4:00  추가햇음
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @PrePersist
    @PreUpdate
    public void formatPhoneNumber() {
        if (this.phoneNumber != null) {
            String digits = this.phoneNumber.replaceAll("[^0-9]", "");
            if (digits.length() == 11) {
                this.phoneNumber = digits.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
            } else if (digits.length() == 10) {
                this.phoneNumber = digits.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "$1-$2-$3");
            }
        }
    }

    public void increaseReportedCount() {
        this.reportedCount++;
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }
}