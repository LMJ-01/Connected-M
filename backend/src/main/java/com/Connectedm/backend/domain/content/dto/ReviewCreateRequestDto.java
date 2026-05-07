package com.Connectedm.backend.domain.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
// 리뷰 저장용
public class ReviewCreateRequestDto {
    private Long contentId;             // 우리 DB의 content PK
    private String externalMovieId;     // 씨네21 영화 ID (Source 테이블용)
    private String rawTitle;            // 씨네21 원본 영화 제목
    private String criticName;
    private String rating;
    private String comment;
}
