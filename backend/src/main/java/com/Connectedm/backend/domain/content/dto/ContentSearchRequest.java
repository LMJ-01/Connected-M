package com.Connectedm.backend.domain.content.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContentSearchRequest {
    private String query;

    private double[] queryVector;
}
