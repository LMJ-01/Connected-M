package com.Connectedm.backend.global.auth;

import com.Connectedm.backend.domain.user.repository.UserRepository;
import com.Connectedm.backend.domain.user.entity.User;
import com.Connectedm.backend.global.security.CustomUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpirationTime;
    private final UserRepository userRepository;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration}") long expirationTime,
            UserRepository userRepository) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationTime = expirationTime;
        this.userRepository = userRepository;
    }

    // 인증 정보 가져오기 (권한 로직 보강) ✨ 핵심 수정 부분
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String subject = claims.getSubject();

        // 1. 토큰의 주체(ID 또는 Email)로 DB에서 유저 정보를 가져옴
        User user;
        try {
            Long userId = Long.parseLong(subject);
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        } catch (NumberFormatException e) {
            user = userRepository.findByEmail(subject)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        }

        // 2. DB에 저장된 ROLE_ADMIN 값을 권한 객체로 생성
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().toString())
        );

        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        // 3. 인증 객체 생성 시 세 번째 인자로 권한(authorities)을 반드시 전달해야 함
        return new UsernamePasswordAuthenticationToken(customUserDetails, token, authorities);
    }

    public String createAccessToken(Authentication authentication) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationTime);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("JWT 검증 실패");
            return false;
        }
    }
}