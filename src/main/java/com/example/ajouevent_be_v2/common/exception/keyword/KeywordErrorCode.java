package com.example.ajouevent_be_v2.common.exception.keyword;

import com.example.ajouevent_be_v2.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KeywordErrorCode implements ErrorCode {

    KEYWORD_NOT_FOUND(404, "AE-KEYWORD-NOT-FOUND", "존재하지 않는 키워드입니다."),
    ALREADY_SUBSCRIBED(409, "AE-KEYWORD-ALREADY-SUBSCRIBED", "이미 구독 중인 키워드입니다."),
    MAX_SUBSCRIPTION_LIMIT_EXCEEDED(400, "AE-KEYWORD-MAX-SUBSCRIPTION-LIMIT", "키워드 구독은 최대 10개까지 가능합니다.");

    private final int status;
    private final String code;
    private final String message;
}
