package com.example.ajouevent_be_v2.dto.push;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "푸시 클러스터 발송 요청")
public record PushClusterSendRequest(
    @Schema(description = "발송할 푸시 클러스터 ID", example = "1")
    Long pushClusterId,

    @Schema(description = "FCM 메시지 발송 명령")
    FcmMessageCommand fcmMessageCommand
) {
}
