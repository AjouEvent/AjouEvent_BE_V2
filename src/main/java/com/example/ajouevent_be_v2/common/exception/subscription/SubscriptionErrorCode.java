package com.example.ajouevent_be_v2.common.exception.subscription;

import com.example.ajouevent_be_v2.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubscriptionErrorCode implements ErrorCode {

    TOPIC_NOT_FOUND(404, "AE-SUBSCRIPTION-TOPIC-NOT-FOUND", "존재하지 않는 토픽입니다."),
    ALREADY_SUBSCRIBED_TOPIC(409, "AE-SUBSCRIPTION-ALREADY-SUBSCRIBED-TOPIC", "이미 구독 중인 토픽입니다."),
    ALREADY_SUBSCRIBED_KEYWORD(409, "AE-SUBSCRIPTION-KEYWORD-ALREADY-SUBSCRIBED", "이미 구독 중인 키워드입니다."),
    MAX_KEYWORD_LIMIT_EXCEEDED(400, "AE-SUBSCRIPTION-MAX-KEYWORD-LIMIT", "키워드 구독은 최대 10개까지 가능합니다."),
    KEYWORD_NOT_FOUND(404, "AE-SUBSCRIPTION-KEYWORD-NOT-FOUND", "존재하지 않는 키워드입니다.");

    private final int status;
    private final String code;
    private final String message;
}
