package com.Connectedm.backend.domain.user.controller;

import com.Connectedm.backend.domain.user.dto.MyPageResponseDto;
import com.Connectedm.backend.domain.user.dto.UserResponse;
import com.Connectedm.backend.domain.user.entity.User;
import com.Connectedm.backend.domain.user.repository.UserRepository;
import com.Connectedm.backend.domain.user.service.MyPageService;
import com.Connectedm.backend.domain.user.service.UserService;
import com.Connectedm.backend.global.common.ApiResponse;
import com.Connectedm.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user") // 👈 주소를 "/api/user"로 바꿔서 충돌을 피합니다!
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final MyPageService myPageService;
    private final BCryptPasswordEncoder passwordEncoder;

    // 마이페이지 정보 조회
    @GetMapping("/{userId}")
    public ApiResponse<MyPageResponseDto> getMyPage(@PathVariable("userId") Long userId) {
        MyPageResponseDto response = myPageService.getMyPageInfo(userId);
        return ApiResponse.success(response);
    }

    // 추가 정보 업데이트
    @PutMapping("/update-extra-info")
    public ApiResponse<String> updateExtraInfo(@RequestBody Map<String, Object> request) {
        try {
            if (request.get("id") == null) return ApiResponse.error("ID가 누락되었습니다.");

            Long id = Long.valueOf(request.get("id").toString());
            String email = (String) request.get("email");
            String phoneNumber = (String) request.get("phoneNumber");
            String password = (String) request.get("password");
            String nickname = (String) request.get("nickname");

            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));

            String currentPhone = user.getPhoneNumber();
            String normalizedCurrentPhone = currentPhone == null ? null : currentPhone.replaceAll("[^0-9]", "");
            String normalizedPhoneNumber = phoneNumber == null ? null : phoneNumber.replaceAll("[^0-9]", "");

            // 전화번호 중복 체크. 본인 전화번호와 동일하면 허용, 다른 사용자에 의해 이미 등록된 번호이면 에러 처리.
            if (normalizedPhoneNumber != null && !normalizedPhoneNumber.isEmpty()) {
                if (!normalizedPhoneNumber.equals(normalizedCurrentPhone) && userRepository.existsByPhoneNumber(phoneNumber)) {
                    return ApiResponse.error("이미 등록된 전화번호입니다. 다른 번호를 입력해 주세요.");
                }
            }

            if (nickname != null && !nickname.isEmpty() && userRepository.existsByNickname(nickname) && !nickname.equals(user.getNickname())) {
                return ApiResponse.error("이미 사용 중인 닉네임입니다.");
            }

            if (nickname != null && !nickname.isEmpty()) {
                user.setNickname(nickname);
            }
            user.setEmail(email);
            user.setPhoneNumber(phoneNumber);
            if (password != null && !password.isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }

            userRepository.save(user);
            return ApiResponse.success("정보 업데이트 성공!");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("서버 내부 에러: " + e.getMessage());
        }
    }

    // 닉네임 중복 확인 API
    @GetMapping("/check-nickname")
    public ApiResponse<Boolean> checkNickname(@RequestParam String nickname) {
        boolean exists = userRepository.existsByNickname(nickname);
        return ApiResponse.success(exists);
    }

    // 회원 탈퇴
    @PatchMapping("/me/withdraw")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 500 대신 401 반환
        }
        userService.withdraw(userDetails.getUserId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{userId}/withdraw")
    public ResponseEntity<Void> adminWithdrawUser(@PathVariable("userId") Long userId) {
        // 기존에 이미 잘 짜여진 withdraw 로직을 그대로 활용합니다!
        userService.withdraw(userId);
        return ResponseEntity.ok().build();
    }
}