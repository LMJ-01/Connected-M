package com.Connectedm.backend.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class AdminReviewReportResponseDto {
    private Long reviewId;
    private String movieTitle;
    private String writerNickname;
    private String reviewComment;

    // 신고 내역 리스트
    private List<AdminReviewReportDetailDto> reportDetails;
}
