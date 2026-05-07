package com.Connectedm.backend.domain.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "유저 리뷰 작성 요청")
public class UserReviewRequestDto {

    @NotBlank(message = "평점은 필수")
    @Schema(description = "벌점에서 변환된 점수", example = "4.5")
    private String rating;

    @NotBlank(message = "리뷰 내용은 필수")
    @Schema(description = "리뷰 본문", example = "재밌었어요")
    private String comment;
}
