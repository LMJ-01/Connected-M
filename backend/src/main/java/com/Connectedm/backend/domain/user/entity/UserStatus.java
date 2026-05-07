package com.Connectedm.backend.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    ACTIVE("활동 중"),
    PENDING("활동 제한/경고"),
    BANNED("영구 정지"),
    WITHDRAWN("탈퇴한 계정");

    private final String description;
}
