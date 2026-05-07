package com.Connectedm.backend.domain.content.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewStatus {
    NORMAL("정상 노출"),
    HIDDEN("관리자 블라인드");

    private final String description;
}
