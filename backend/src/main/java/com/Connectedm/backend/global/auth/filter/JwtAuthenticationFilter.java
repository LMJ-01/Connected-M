package com.Connectedm.backend.global.auth.filter;

import com.Connectedm.backend.domain.user.entity.User;
import com.Connectedm.backend.domain.user.entity.UserStatus;
import com.Connectedm.backend.domain.user.repository.UserRepository;
import com.Connectedm.backend.global.auth.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);
        // 1. [위치 변경] 토큰 검사 밖으로 빼서 모든 요청의 경로를 확인합니다.
        String path = request.getRequestURI();

        // 이 로그가 콘솔에 찍혀야 합니다. /api/user/me/withdraw가 보이는지 꼭 확인하세요!
        System.out.println("🚩 [JWT Filter] 들어온 요청 경로: " + path);

        // 2. 탈퇴 API는 토큰이 유효하기만 하면 유저 상태 체크 없이 "무조건" 통과시킵니다.
        if (path != null && path.contains("/withdraw")) {
            if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
                Authentication authentication = tokenProvider.getAuthentication(token);

                System.out.println("🧐 권한 정보가 있는가? " + authentication.getAuthorities());
                System.out.println("🧐 인증된 상태인가? " + authentication.isAuthenticated());

                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("✅ [JWT Filter] 탈퇴 경로 인증 성공");
                filterChain.doFilter(request, response);
                return;
            } else {
                System.out.println("❌ [JWT Filter] 탈퇴 요청인데 토큰이 없거나 무효함");
            }
        }

        // 3. 그 외 일반 요청 로직
        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            Authentication authentication = tokenProvider.getAuthentication(token);
            String email = authentication.getName();

            // 일반 요청에서만 유저 조회를 수행하여 불필요한 로그를 줄입니다.
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null || user.getStatus() == UserStatus.WITHDRAWN) {
                System.out.println("🚫 [JWT Filter] 존재하지 않거나 탈퇴한 유저의 접근");
                filterChain.doFilter(request, response);
                return;
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}