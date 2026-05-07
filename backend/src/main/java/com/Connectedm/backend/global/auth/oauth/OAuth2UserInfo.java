package com.Connectedm.backend.global.auth.oauth;

import java.util.Map;

public interface OAuth2UserInfo {
    String getProviderId();    // 소셜 서비스의 고유 ID (숫자 등)
    String getProvider();      // 어느 서비스인지 (kakao, google)
    String getEmail();         // 사용자 이메일
    String getName();          // 사용자 닉네임
}