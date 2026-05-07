package com.Connectedm.backend.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportReason {
    SPAM("스팸/홍보"),
    INAPPROPRIATE_CONTENT("부적절한 내용"),
    ABUSIVE_LANGUAGE("욕설/비하 발언"),
    SPOILER("스포일러 포함"),
    OTHER("기타 사유");

    private final String description;
}
