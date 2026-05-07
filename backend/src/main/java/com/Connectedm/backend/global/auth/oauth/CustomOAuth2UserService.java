package com.Connectedm.backend.global.auth.oauth;

import com.Connectedm.backend.domain.user.entity.User;
import com.Connectedm.backend.domain.user.entity.User.AuthProvider;
import com.Connectedm.backend.domain.user.entity.UserRole;
import com.Connectedm.backend.domain.user.entity.UserStatus;
import com.Connectedm.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 소셜에서 기본 유저 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. 어떤 소셜 로그인인지 확인 (kakao, google 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = null;
        AuthProvider authProvider = AuthProvider.LOCAL;

        if (registrationId.equals("kakao")) {
            oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
            authProvider = AuthProvider.KAKAO;
        } else if (registrationId.equals("google")) {
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
            authProvider = AuthProvider.GOOGLE;
        }

        if (oAuth2UserInfo == null) {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다.");
        }

        // 3. 소셜에서 제공하는 고유 식별 데이터 추출
        String providerId = oAuth2UserInfo.getProviderId();
        String email = oAuth2UserInfo.getEmail();

        // 4. 최초 가입 시 사용할 임시 이메일 생성
        if (email == null || email.isEmpty()) {
            email = registrationId + "_" + providerId + "@connectedm.temp";
        }

        final String finalEmail = email;
        final AuthProvider finalProvider = authProvider;
        final String socialName = oAuth2UserInfo.getName();
        final String realName = (socialName != null && !socialName.isEmpty()) ? socialName : "소셜유저";

        // 5. [개선된 로직] 지문(Provider+ID)으로 먼저 찾고, 없으면 이메일로 찾기
        User userEntity = userRepository.findByProviderAndProviderId(finalProvider, providerId)
                .or(() -> userRepository.findByEmail(finalEmail))
                .map(entity -> {
                    // ✨ [핵심 추가] 정지된 유저인지 체크
                    if (entity.getStatus() == UserStatus.BANNED) {
                        // 이 예외는 SecurityConfig에 설정된 FailureHandler나 인증 과정에서 처리됩니다.
                        throw new OAuth2AuthenticationException("BANNED_USER");
                    }

                    // 기존 유저 업데이트 로직
                    if (entity.getNickname() == null || entity.getNickname().isEmpty()) {
                        entity.setNickname(socialName != null && !userRepository.existsByNickname(socialName) ? socialName : "tmp_" + UUID.randomUUID().toString().substring(0,8));
                    }
                    entity.setRealName(realName);
                    if (entity.getProvider() == null || entity.getProvider() == AuthProvider.LOCAL) {
                        entity.setProvider(finalProvider);
                        entity.setProviderId(providerId);
                    }
                    return userRepository.save(entity);
                })
                .orElseGet(() -> {
                    // 신규 가입은 당연히 ACTIVE로 생성[cite: 4]
                    return userRepository.save(User.builder()
                            .email(finalEmail)
                            .nickname(generateUniqueNickname(socialName, finalProvider))
                            .realName(realName)
                            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                            .provider(finalProvider)
                            .providerId(providerId)
                            .role(UserRole.ROLE_USER)
                            .status(UserStatus.ACTIVE)
                            .reportedCount(0)
                            .build());
                });

        // ✨ [핵심 수정] DB에서 가져온 실제 권한(ROLE_ADMIN 등)을 시큐리티 객체에 주입합니다.
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(userEntity.getRole().name())),
                oAuth2User.getAttributes(),
                userRequest.getClientRegistration().getProviderDetails()
                        .getUserInfoEndpoint().getUserNameAttributeName()
        );
    }

    private String generateUniqueNickname(String baseNickname, AuthProvider provider) {
        if (baseNickname == null || baseNickname.isEmpty()) {
            baseNickname = "소셜유저";
        }

        String candidate = baseNickname;
        int counter = 1;

        // 중복되지 않을 때까지 숫자를 붙여서 시도
        while (userRepository.existsByNickname(candidate)) {
            candidate = baseNickname + counter;
            counter++;
            // 무한 루프 방지를 위해 최대 100번 시도
            if (counter > 100) {
                // 마지막 수단으로 UUID 일부 사용
                candidate = baseNickname + "_" + UUID.randomUUID().toString().substring(0, 8);
                break;
            }
        }

        return candidate;
    }
}
