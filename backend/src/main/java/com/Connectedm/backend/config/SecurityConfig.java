package com.Connectedm.backend.config;

import java.util.List;

import com.Connectedm.backend.domain.user.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import com.Connectedm.backend.global.auth.JwtTokenProvider;
import com.Connectedm.backend.global.auth.filter.JwtAuthenticationFilter;
import com.Connectedm.backend.global.auth.handler.OAuth2SuccessHandler;
import com.Connectedm.backend.global.auth.oauth.CustomOAuth2UserService;


import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;



    // 2. HTTP 보안 및 필터 체인 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (REST API 환경)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 설정을 보안 필터 체인에 직접 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 1. JWT 사용을 위한 세션 정책 추가 ✨ (필수!)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 기본 로그인 폼 및 HTTP Basic 인증 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 경로별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.web.cors.CorsUtils::isPreFlightRequest).permitAll()
                        .requestMatchers("/", "/error", "/favicon.ico").permitAll()
                        .requestMatchers("/api/user/me/withdraw").authenticated()
                        .requestMatchers("/api/user/**", "/api/users/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers("/api/users/recent/**").authenticated()
                        .requestMatchers("/api/ai/**").permitAll()
                        .requestMatchers("/register").permitAll()
                                .requestMatchers("/api/members/**").permitAll()

                        // 4. OAuth2 관련 엔드포인트 허용 추가
                        .requestMatchers("/login/oauth2/**", "/oauth2/**").permitAll()
//                        .requestMatchers("/api/contents/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/contents/**").permitAll()
                        .requestMatchers("/api/contents/user-reviews/**").authenticated()

                        .requestMatchers("/api/auth/update-extra-info").authenticated()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/api-docs/**","/api/main**").permitAll()

//                        .requestMatchers("/api/main/**", "/api/contents/**").permitAll()
                        .requestMatchers("/api/main/**").permitAll()
                        .requestMatchers("/api/admin/**").permitAll()

                        .anyRequest().authenticated()
                ) // 👈 여기서 세미콜론(;)을 지웠습니다!

                // 5. OAuth2 로그인 설정 추가 ✨ (위의 닫는 괄호 뒤에 바로 연결)
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            // 프론트엔드 주소/oauth2/redirect?error=BANNED_USER 형식으로 리다이렉트
                            response.sendRedirect("http://localhost:5173/oauth2/redirect?error=BANNED_USER");
                        })
                )
                // ✨ 핵심: JWT 필터를 UsernamePasswordAuthenticationFilter보다 먼저 실행하게 설정
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userRepository), // ✨ userRepository 전달
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 3. WebConfig의 기능을 흡수한 통합 CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용할 프론트엔드 주소 (React 기본 포트 3000, Vite 5173 모두 포함)
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
        // 허용할 HTTP 메서드
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // 허용할 헤더
        config.setAllowedHeaders(List.of("*"));
        // 쿠키 및 인증 정보(Credentials) 허용
        config.setAllowCredentials(true);
        // 응답 헤더 노출 설정
        config.setExposedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 경로(/**)에 대해 위 설정을 적용
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}