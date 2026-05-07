package com.Connectedm.backend.domain.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MovieMainDto {
    private Long id;
    private String title;
    private String posterPath;
    private Double averageRating;
}
