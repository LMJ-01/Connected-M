package com.Connectedm.backend.domain.common.validator;

import com.Connectedm.backend.domain.content.entity.ReviewStatus;
import com.Connectedm.backend.domain.content.entity.UserReview;
import com.Connectedm.backend.domain.user.entity.User;
import com.Connectedm.backend.domain.user.entity.UserStatus;
import com.Connectedm.backend.global.error.CustomException;
import com.Connectedm.backend.global.error.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class StatusValidator {

    // 유저 활동 상태 체크
    public void validateUserStatus(User user) {

        if (user.getStatus() == UserStatus.WITHDRAWN) {
            throw new CustomException(ErrorCode.USER_WITHDRAWN);
        }

        if (user.getStatus() == UserStatus.BANNED) {
            throw new CustomException(ErrorCode.USER_BANNED);
        }
    }

    // 리뷰 노출 상태 체크
    public void validateReviewStatus(UserReview review) {
        if (review.getStatus() == ReviewStatus.HIDDEN) {
            throw new CustomException(ErrorCode.REVIEW_HIDDEN);
        }
    }
}
