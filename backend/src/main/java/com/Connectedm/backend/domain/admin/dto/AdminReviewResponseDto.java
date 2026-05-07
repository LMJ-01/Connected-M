package com.Connectedm.backend.domain.admin.dto;

import com.Connectedm.backend.domain.content.entity.ReviewStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminReviewResponseDto {
    private Long reviewId;
    private String writeNickname;
    private String movieTitle;
    private String comment;
    private int reportCount;
    private ReviewStatus status; // 현재 노출 상태
    private LocalDateTime createdAt;
}
