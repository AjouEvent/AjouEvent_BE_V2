package com.example.ajouevent_be_v2.common.exception.common;

import com.example.ajouevent_be_v2.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    INTERNAL_FAILURE(500, "AE-COMMON-INTERNAL-FAILURE", "서버 내부 오류입니다."),
    VALIDATION_ERROR(400, "AE-COMMON-VALIDATION-ERROR", "입력값이 올바르지 않습니다."),
    THROTTLING(429, "AE-COMMON-THROTTLING", "요청이 너무 많습니다."),
    SERVICE_UNAVAILABLE(503, "AE-COMMON-SERVICE-UNAVAILABLE", "서비스를 사용할 수 없습니다.");

    private final int status;
    private final String code;
    private final String message;
}
