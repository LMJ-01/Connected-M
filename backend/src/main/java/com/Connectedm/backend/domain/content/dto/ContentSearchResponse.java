package com.Connectedm.backend.domain.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentSearchResponse {
    private Long contentId;
    private String title;
    private String posterPath;
    private double similaritySource;
    private String overview;
    private List<String> genres;
    private Double positiveRatio;
}
