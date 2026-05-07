package com.Connectedm.backend.domain.user.service;

import com.Connectedm.backend.domain.content.repository.UserReviewRepository;
import com.Connectedm.backend.domain.user.dto.UserLoginRequest;
import com.Connectedm.backend.domain.user.dto.UserResponse;
import com.Connectedm.backend.domain.user.dto.UserSignupRequest;
import com.Connectedm.backend.domain.user.entity.LoginLog;
import com.Connectedm.backend.domain.user.entity.User;
import com.Connectedm.backend.domain.user.entity.UserRole;
import com.Connectedm.backend.domain.user.entity.UserStatus;
import com.Connectedm.backend.domain.user.repository.LoginLogRepository;
import com.Connectedm.backend.domain.user.repository.UserRepository;
import com.Connectedm.backend.domain.user.repository.WishlistRepository;
import com.Connectedm.backend.global.error.CustomException;
import com.Connectedm.backend.global.error.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final LoginLogRepository loginLogRepository;
    private final UserReviewRepository userReviewRepository;
    private final WishlistRepository wishlistRepository;


    @Transactional
    public Long signUp(UserSignupRequest request) {
        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.ALREADY_REGISTERED_EMAIL);
        }

        // 2. 닉네임 중복 체크
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.ALREADY_USED_NICKNAME);
        }

        // 3. 전화번호 중복 체크 (010-1234-1234 중복 방지)
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new CustomException(ErrorCode.ALREADY_REGISTERED_PHONE);
        }

        // 4. 암호화 및 빌더로 저장
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .realName(request.getRealName())
                .phoneNumber(request.getPhoneNumber())
                .provider(User.AuthProvider.LOCAL)
                .role(UserRole.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .reportedCount(0)
                .build();

        return userRepository.save(user).getId();
    }

    @Transactional
    public void verifyAndSendResetLink(String email, String realName, String phoneNumber) {
        // 전화번호를 DB 저장 형식(010-0000-0000)으로 통일
        String normalizedPhone = normalizePhoneNumber(phoneNumber);

        // DB에서 이메일+이름+전화번호가 일치하는지 확인
        User user = userRepository.findByEmailAndRealNameAndPhoneNumber(email, realName, normalizedPhone)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new CustomException(ErrorCode.USER_BANNED);
        }

        // 비밀번호 재설정 토큰을 생성하여 DB에 저장하고, 이메일로 링크 전송
        String resetToken = java.util.UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);
        mailService.sendResetLink(user.getEmail(), resetToken);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        // 이메일 링크의 토큰으로 유저 조회
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_PASSWORD_RESET_TOKEN));

        if (user.getPasswordResetTokenExpiry() == null || user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.EXPIRED_PASSWORD_RESET_TOKEN);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        String digits = phoneNumber.replaceAll("[^0-9]", "");
        if (digits.length() == 11) {
            return digits.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
        } else if (digits.length() == 10) {
            return digits.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "$1-$2-$3");
        }
        return phoneNumber;
    }
    @Transactional
    public UserResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new CustomException(ErrorCode.USER_BANNED);
        }
        // 3:08  [추가] 탈퇴한 계정인지 확인
        // 이 로직이 실행되면 탈퇴한 유저가 로그인 시도 시 즉시 에러를 던져 로그인을 막습니다.
        if (user.getStatus() == UserStatus.WITHDRAWN) {
            throw new CustomException(ErrorCode.USER_WITHDRAWN);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        user.updateLastLoginAt();

        LoginLog log = LoginLog.builder()
                .user(user)
                .loginAt(LocalDateTime.now())
                .build();
        loginLogRepository.save(log);

        return UserResponse.from(user);
    }

    // 3. 마이페이지 유저 정보 조회
    public UserResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new CustomException(ErrorCode.USER_BANNED);
        }
        if (user.getStatus() == UserStatus.WITHDRAWN) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return UserResponse.from(user);
    }

    @Transactional
    public void updateProfile(Long userId, String nickname, String phoneNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 1. 닉네임 중복 체크 (기존 내 닉네임이랑 다를 때만 체크)
        if (nickname != null && !nickname.equals(user.getNickname())) {
            if (userRepository.existsByNickname(nickname)) {
                // 🚀 중복이면 여기서 예외를 던져서 흐름을 끊어버립니다!
                throw new CustomException(ErrorCode.ALREADY_USED_NICKNAME);
            }
            user.setNickname(nickname);
        }

        // 2. 전화번호 중복 체크 (기존 내 번호랑 다를 때만 체크)
        if (phoneNumber != null && !phoneNumber.equals(user.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumber(phoneNumber)) {
                throw new CustomException(ErrorCode.ALREADY_REGISTERED_PHONE);
            }
            user.setPhoneNumber(phoneNumber);
        }
    }


    /**
     *  [관리자용] 유저 상태 변경(BANNED, PENDING)
     */
    @Transactional
    public void updateUserStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 엔티티에 만든 전용 Setter 이용
        user.setStatus(status);

        // @transactional 읽기 전용 무시하고 DB저장
        userRepository.saveAndFlush(user);
    }

    /**
     * [유저 전용] 자진 탈퇴 (수정 버전)
     */
    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.setStatus(UserStatus.WITHDRAWN);

        // UUID 8자리만 사용하여 전체 길이를 줄입니다 (DB 100자 제한 방어용)
        String maskedId = java.util.UUID.randomUUID().toString().substring(0, 8);

        user.setEmail("del_" + maskedId + "@deleted.com");
        user.setPhoneNumber("del_" + maskedId); // 👈 전화번호도 반드시 변조!
        user.setNickname("탈퇴유저_" + maskedId);

        // 순서 변경: DB 반영 후 인증 정보 삭제 (Auditing 에러 방지)
        userRepository.saveAndFlush(user);
        SecurityContextHolder.clearContext();
    }
}
