package com.Connectedm.backend.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class AdminReviewReportDetailDto {
    private Long reviewId;
    private String reporterNickname; // 신고자 닉네임
    private String reason;           // 신고 사유 (Enum description)
    private String detailReason;     // 상세 신고 내용
    private LocalDateTime reportedAt; // 신고 일시
}
