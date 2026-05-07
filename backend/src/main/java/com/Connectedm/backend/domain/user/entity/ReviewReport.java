package com.Connectedm.backend.domain.user.entity;

import com.Connectedm.backend.domain.content.entity.UserReview;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "ReviewReport")
public class ReviewReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_review_id", nullable = false)
    private UserReview review;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private ReportReason reason;

    @Column(columnDefinition = "TEXT")
    private String detailReason;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ReviewReport(User reporter, UserReview review, ReportReason reason, String detailReason) {
        this.reporter = reporter;
        this.review = review;
        this.reason = reason;
        this.detailReason = detailReason;
    }

}
