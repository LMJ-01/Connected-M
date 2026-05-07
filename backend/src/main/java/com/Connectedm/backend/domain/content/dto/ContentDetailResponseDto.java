package com.Connectedm.backend.domain.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContentDetailResponseDto {
    private long id;
    private String title;
    private String overview;
    private String posterPath;
    private String ottLogos;
    private List<String> genres;
    @Schema(hidden = true)
    private List<TmdbMovieResponseDto.TmdbCastItem> castList;
    private String backdropPath;
    private Integer runtime;
    private String ageRating;

    // AI 분석 영역
    private String aiSummary;
    private Double positiveRatio;
    private List<String> topKeywords;

    // 리뷰 영역
    private List<ReviewResponseDto> expertReviews;
    private List<UserReviewResponseDto> userReviews;

    // 평점 통계 필드
    private Double userRatingAvg;    // 관람평 평균
    private Double expertRatingAvg;  // 전문가 평점 평균
    private Long expertReviewCount;  // 전문가 리뷰 건수

    // 트레일러 기능
    private String trailerKey;

    // 시맨틱 추천 영화 리스트 필드
    private List<ContentSummaryDto> recommendations;
}
