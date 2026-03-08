package com.example.ajouevent_be_v2.common.exception;

/**
 * 에러코드 형식: AE-{DOMAIN}-{ERROR-NAME} (예: AE-MEMBER-USER-NOT-FOUND)
 * 모든 도메인 예외: XxxException extends AjouBaseException
 * GlobalExceptionHandler 에서 AjouBaseException 일괄 처리 — 도메인별 핸들러 별도 작성 금지
 */
public interface ErrorCode {
    int getStatus();

    String getCode();

    String getMessage();
}
