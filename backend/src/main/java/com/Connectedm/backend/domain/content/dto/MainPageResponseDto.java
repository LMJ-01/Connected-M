package com.Connectedm.backend.domain.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MainPageResponseDto {

    // 랜덤 영화 포스터
    private String backgroundImage;

    // 오늘의 추천작 (평점 높은 순 10개)
    private List<ContentSummaryDto> todayRecommendations;

    // 카테고리별 영화 (장르 이름별로 리스트 묶음)
    // 예 : "ACTION" -> [영화1, 영화2...]
    private Map<String, List<ContentSummaryDto>> genreContents;
}
