package com.Connectedm.backend.domain.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpertReviewCreateRequestDto {
    private Long contentId;         // 우리 시스템의 Content PK (필수!)
    private String externalMovieId; // 씨네21 고유 영화 ID (Source 테이블 저장용)
    private String rawTitle;        // 씨네21 원본 영화 제목 (검증용)

    private String criticName;      // 평론가 이름
    private String rating;          // 평점 (String 합의 완료!)
    private String comment;         // 리뷰 본문
}
