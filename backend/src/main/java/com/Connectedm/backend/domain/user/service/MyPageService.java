package com.Connectedm.backend.domain.user.service;

import com.Connectedm.backend.domain.content.repository.UserReviewRepository;
import com.Connectedm.backend.domain.user.dto.MyPageResponseDto;
import com.Connectedm.backend.domain.user.entity.User;
import com.Connectedm.backend.domain.user.repository.UserRepository;
import com.Connectedm.backend.domain.user.repository.WishlistRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final UserReviewRepository userReviewRepository;
    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public MyPageResponseDto getMyPageInfo (Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("찾을 수 없는 사용자입니다."));

        // DTO의 정적 팩토리 메서드 'from' 사용
        return MyPageResponseDto.from(
                user,
                userReviewRepository.countByUserId(userId),
                wishlistRepository.countByUserId(userId)
        );
    }
}
