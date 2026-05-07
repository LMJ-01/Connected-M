package com.Connectedm.backend.domain.content.dto;

import com.Connectedm.backend.domain.content.entity.UserReview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserReviewResponseDto {
    private Long id;
    private String nickname;
    private String rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserReviewResponseDto from(UserReview review) {
        return UserReviewResponseDto.builder()
                .id(review.getId())
                .nickname(review.getUser().getNickname())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
