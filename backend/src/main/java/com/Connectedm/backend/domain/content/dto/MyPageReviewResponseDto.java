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
public class MyPageReviewResponseDto {
    private Long reviewId;
    private Long contentId;
    private String movieTitle;
    private String posterPath;
    private String rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MyPageReviewResponseDto from(UserReview review) {
        return MyPageReviewResponseDto.builder()
                .reviewId(review.getId())
                .contentId(review.getContent().getId())
                .movieTitle(review.getContent().getTitle())
                .posterPath(review.getContent().getPosterPath())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
