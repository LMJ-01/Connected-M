package com.Connectedm.backend.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LoginLogResponseDto {
    private Long logId;
    private String userEmail;
    private String userNickname;
    private LocalDateTime loginAt;
    private String ipAddress;
    private String deviceInfo;
}
