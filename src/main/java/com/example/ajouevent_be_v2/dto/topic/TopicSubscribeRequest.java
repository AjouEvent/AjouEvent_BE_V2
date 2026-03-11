package com.example.ajouevent_be_v2.dto.topic;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토픽 구독 요청")
public record TopicSubscribeRequest(
    @Schema(description = "구독할 토픽의 영문 식별자 (department)", example = "Software")
    String topic
) {
}
