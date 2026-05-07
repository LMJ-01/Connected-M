package com.Connectedm.backend.domain.user.dto;

import com.Connectedm.backend.domain.user.entity.User;

public record MyPageResponseDto(
        Long id,
        String email,
        String realName,
        String nickname,
        String phoneNumber,
        Long reviewCount,   // 레포지토리에서 count
        Long wishlistCount  // 레포지토리에서 count
) {
    public static MyPageResponseDto from(User user, Long reviewCount, Long wishlistCount) {
        return new MyPageResponseDto(
                user.getId(),
                user.getEmail(),
                user.getRealName(),
                user.getNickname(),
                user.getPhoneNumber(),
                reviewCount,
                wishlistCount
        );
    }
}
