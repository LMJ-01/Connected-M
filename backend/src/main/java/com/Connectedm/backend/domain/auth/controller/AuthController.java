package com.Connectedm.backend.domain.auth.controller;

import com.Connectedm.backend.domain.user.dto.*;
import com.Connectedm.backend.domain.user.service.UserService;
import com.Connectedm.backend.global.auth.JwtTokenProvider;
import com.Connectedm.backend.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/auth") // 👈 "나 여기로 들어올래!" (인증 관련)
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest dto) {
        UserResponse userResponse = userService.login(dto);

        org.springframework.security.core.authority.SimpleGrantedAuthority authority =
                new org.springframework.security.core.authority.SimpleGrantedAuthority(userResponse.getRole().name());

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userResponse.getEmail(),
                null,
                java.util.Collections.singletonList(authority)
        );

        String token = jwtTokenProvider.createAccessToken(auth);
        return ResponseEntity.ok(ApiResponse.success(new LoginResponse(token, userResponse)));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserSignupRequest dto) {
        Long userId = userService.signUp(dto);
        UserResponse userResponse = userService.getUserInfo(userId);
        Authentication auth = new UsernamePasswordAuthenticationToken(userResponse.getEmail(), null, Collections.emptyList());
        String token = jwtTokenProvider.createAccessToken(auth);
        return ResponseEntity.ok(ApiResponse.success(new LoginResponse(token, userResponse)));
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<?> requestPasswordReset(
            @RequestParam String email,
            @RequestParam String realName,
            @RequestParam String phoneNumber
    ) {
        // 비밀번호 재설정 요청 처리: 이메일/이름/전화번호 확인 후 메일 발송
        userService.verifyAndSendResetLink(email, realName, phoneNumber);

        return ResponseEntity.ok(ApiResponse.success("입력하신 이메일로 비밀번호 재설정 링크를 발송했습니다."));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<?> confirmPasswordReset(@RequestBody PasswordResetConfirmRequest request) {
        // 이메일 링크 클릭 후 토큰과 새 비밀번호로 실제 변경 처리
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 변경되었습니다."));
    }
}