package com.example.ajouevent_be_v2.common.exception.auth;

import com.example.ajouevent_be_v2.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    UNAUTHORIZED(401, "AE-AUTH-UNAUTHORIZED", "인증이 필요합니다."),
    INVALID_TOKEN(401, "AE-AUTH-INVALID-TOKEN", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "AE-AUTH-EXPIRED-TOKEN", "토큰이 만료되었습니다."),
    UNSUPPORTED_TOKEN(401, "AE-AUTH-UNSUPPORTED-TOKEN", "지원하지 않는 토큰입니다."),
    ILLEGAL_ARGUMENT_TOKEN(401, "AE-AUTH-ILLEGAL-ARGUMENT-TOKEN", "토큰 값이 올바르지 않습니다."),

    INVALID_AUTHORIZATION_CODE(400, "AE-AUTH-INVALID-AUTHORIZATION-CODE", "잘못된 인가 코드입니다."),

    FORBIDDEN(403, "AE-AUTH-FORBIDDEN", "접근 권한이 없습니다.");

    private final int status;
    private final String code;
    private final String message;
}
