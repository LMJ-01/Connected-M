package com.Connectedm.backend.domain.content.entity; // content 패키지 추천!

import java.time.LocalDateTime; // user 엔티티 참조
import java.util.ArrayList;
import java.util.List;

import com.Connectedm.backend.domain.user.entity.ReviewReport;
import com.Connectedm.backend.domain.user.entity.User;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_review")
public class UserReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private Content content;

    @Column(nullable = false, length = 10)
    private String rating;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 리뷰별 신고 횟수
    @Column(nullable = false)
    @Builder.Default
    private int reportCount = 0;

    public void increaseReportCount() {
        this.reportCount++;
    }

    // NORMAL = default

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.NORMAL;

    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewReport> reports = new ArrayList<>();

    public void changeStatusByAdmin(ReviewStatus status) {
        this.status = status;
    }


    public void update(String rating, String comment) {
        this.rating = rating;
        this.comment = comment;
        this.updatedAt = LocalDateTime.now();
    }

    public void setMapping(User user, Content content) {
        this.user = user;
        this.content = content;

        if (user != null) user.getReviews().add(this);
        if (content != null) content.getReviews().add(this);
    }

}
