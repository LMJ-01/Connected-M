package com.Connectedm.backend.domain.content.dto;

import com.Connectedm.backend.domain.content.entity.Content;
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
public class ContentCreateRequestDto {
    private String tmdbId;      // TMDB 고유 ID
    private String cine21Id;    // 씨네21 고유 ID
    private String title;       // 영화 제목
    private String overview;    // 영화 줄거리
    private String posterPath;  // 포스터 이미지 경로
    private String ottLogos;    // OTT 플랫폼 로고들 (쉼표 구분 등)
    private String cine21MovieId; // 씨네21 영화 고유 ID

    private List<String> genres; // ["액션", "코미디"] 형태

    // AI 분석 데이터 (AnalysisCache용)
    private String summary;       // AI 요약 줄거리
    private Double positiveRatio; // AI 평점 (예: 8.5)

    // DTO를 엔티티로 변환해주는 메서드
    @Schema(hidden = true)
    public Content toEntity() {
        return Content.builder()
                .tmdbId(this.tmdbId)
                .cine21Id(this.cine21Id)
                .title(this.title)
                .overview(this.overview)
                .posterPath(this.posterPath)
                .ottLogos(this.ottLogos)
                .build();
    }
}
