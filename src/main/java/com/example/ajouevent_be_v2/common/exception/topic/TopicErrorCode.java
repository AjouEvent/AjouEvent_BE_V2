package com.example.ajouevent_be_v2.common.exception.topic;

import com.example.ajouevent_be_v2.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TopicErrorCode implements ErrorCode {

    TOPIC_NOT_FOUND(404, "AE-TOPIC-NOT-FOUND", "존재하지 않는 토픽입니다."),
    ALREADY_SUBSCRIBED(409, "AE-TOPIC-ALREADY-SUBSCRIBED", "이미 구독 중인 토픽입니다."),
    SUBSCRIPTION_NOT_FOUND(404, "AE-TOPIC-SUBSCRIPTION-NOT-FOUND", "구독 정보를 찾을 수 없습니다.");

    private final int status;
    private final String code;
    private final String message;
}
