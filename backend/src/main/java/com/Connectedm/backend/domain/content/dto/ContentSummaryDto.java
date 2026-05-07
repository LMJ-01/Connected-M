package com.Connectedm.backend.domain.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class ContentSummaryDto {
    private Long id;
    private String title;
    private String posterPath;
    private Double positiveRatio;
    private String overview;
    private List<String> genres;
}
