package com.Connectedm.backend.domain.user.entity;

import com.Connectedm.backend.domain.content.entity.Content;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)  // 시간 자동 기록
@Table(name = "recent_view")
public class RecentView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @CreatedDate
    @LastModifiedDate
    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    @Builder
    public RecentView(User user, Content content) {
        this.user = user;
        this.content = content;
        this.viewedAt = LocalDateTime.now();
    }

    public void updateViewedAt() {
        this.viewedAt = LocalDateTime.now();
    }


}
