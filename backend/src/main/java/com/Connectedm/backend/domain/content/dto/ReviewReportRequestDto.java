package com.Connectedm.backend.domain.content.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReportRequestDto {
    private String reason; // 신고 사유

    private String detailReason;
}
