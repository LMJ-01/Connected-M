package com.Connectedm.backend.domain.content.dto;

import lombok.*;

@Getter @Builder
@AllArgsConstructor @NoArgsConstructor
public class ReviewStatsResponseDto {
    private Double userRatingAvg;
    private Double expertRatingAvg;
    private Long expertReviewCount;
}