package com.Connectedm.backend.domain.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeywordPageResponseDto {
    private Long id;            // content_id와 매칭
    private String title;       // 영화 제목
    private String posterPath;  // 포스터 경로
    private String summary;     // AI 줄거리 요약
    private String topKeywords; // AI 키워드
}