package com.example.ajouevent_be_v2.common.exception.webhook;

import com.example.ajouevent_be_v2.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WebhookErrorCode implements ErrorCode {

    INVALID_CRAWLING_TOKEN(401, "AE-WEBHOOK-INVALID-TOKEN", "유효하지 않은 크롤링 토큰입니다."),
    WEBHOOK_PROCESSING_FAILED(400, "AE-WEBHOOK-PROCESSING-FAILED", "Webhook 처리 중 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
