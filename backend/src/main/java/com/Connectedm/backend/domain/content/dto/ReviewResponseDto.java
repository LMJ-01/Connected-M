package com.Connectedm.backend.domain.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponseDto {
    private Long id;
    private String criticName;
    private String rating;
    private String comment;
    private String sourceName;  // "cine21" 등
    private String movieTitle;  // 응답시에는 Content 또는 Cine21Source에서 보냄
}
