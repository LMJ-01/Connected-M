package com.Connectedm.backend.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // User 관련 에러(400번대)
    ALREADY_REGISTERED_EMAIL(HttpStatus.BAD_REQUEST, "U001", "이미 가입된 이메일입니다!"),
    ALREADY_USED_NICKNAME(HttpStatus.BAD_REQUEST, "U002", "이미 사용 중인 닉네임이에요!"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U003", "일치하는 사용자 정보를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "U004", "비밀번호가 일치하지 않습니다!"),
    ALREADY_REGISTERED_PHONE(HttpStatus.BAD_REQUEST, "U005", "이미 등록된 전화번호입니다!"),
    INVALID_PASSWORD_RESET_TOKEN(HttpStatus.BAD_REQUEST, "U006", "유효하지 않은 비밀번호 재설정 토큰입니다."),
    EXPIRED_PASSWORD_RESET_TOKEN(HttpStatus.BAD_REQUEST, "U007", "비밀번호 재설정 토큰이 만료되었습니다."),
    SOCIAL_USER_CANNOT_RESET_PASSWORD(HttpStatus.BAD_REQUEST, "U008", "소셜 로그인 유저는 해당 서비스에서 비밀번호를 변경해주세요!"),
    USER_BANNED(HttpStatus.FORBIDDEN, "U009", "해당 계정은 운영 원칙에 따라 정지된 상태입니다!"),
    USER_PENDING(HttpStatus.FORBIDDEN, "U010", "해당 계정은 현재 활동이 일시 제한되었습니다"),
    USER_WITHDRAWN(HttpStatus.FORBIDDEN, "U011", "해당 계정은 탈퇴한 계정입니다."),

    // --- Content 관련 에러 ---
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CO001", "해당 콘텐츠를 찾을 수 없습니다!"),

    // --- Content/Review 관련 에러 확장 (CO/R시리즈) ---
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "해당 리뷰를 찾을 수 없습니다! ㅋ"),
    REVIEW_HIDDEN(HttpStatus.FORBIDDEN, "R002", "관리자에 의해 블라인드 처리된 리뷰입니다! ㅋ"),
    ALREADY_REPORTED(HttpStatus.BAD_REQUEST, "R003", "이미 신고가 접수된 항목입니다! ㅋ"),

    // --- Admin/Role 관련 에러 ---
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A001", "해당 사령부(관리자 페이지)에 접근할 권한이 없습니다! ㅋ"),

    // --- Wishlist/RecentRecord 관련 에러 (W/RE시리즈) ---
    WISHLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "W001", "찜 목록에서 해당 정보를 찾을 수 없습니다! ㅋ"),

    // --- 공통 에러 ---
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "서버에 문제가 생겼어요!");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
