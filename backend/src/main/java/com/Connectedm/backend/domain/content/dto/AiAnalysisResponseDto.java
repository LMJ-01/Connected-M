package com.Connectedm.backend.domain.content.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class AiAnalysisResponseDto {

    @JsonProperty("content_id")
    private Long contentId;

    private List<String> keywords;

    private String summary;

    @JsonProperty("positive_ratio")
    private Integer positiveRatio;

    private String posterPath;
}
