package com.example.ajouevent_be_v2.dto.topic;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토픽 알림 수신 설정 변경 요청")
public record TopicNotificationUpdateRequest(
    @Schema(description = "설정을 변경할 토픽의 영문 식별자 (department)", example = "Software")
    String topic,

    @Schema(description = "알림 수신 여부", example = "false")
    boolean receiveNotification
) {
}
