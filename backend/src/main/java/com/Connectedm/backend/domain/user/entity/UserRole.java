package com.Connectedm.backend.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {

    // 스프링 시큐리티의 기본 관례인 "ROLE_" 접두사 포함
    ROLE_USER("일반 사용자"),
    ROLE_ADMIN("관리자");

    private final String description;   // 로그나 UI 출력용 설명

}
