package com.Connectedm.backend.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminContentStateResponseDto {
    private Long contentId;
    private String title;
    private int viewCount;
    private int wishCount;
    private String posterPath;
}
