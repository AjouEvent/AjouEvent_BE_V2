package com.example.ajouevent_be_v2.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 클릭(읽음 처리) 요청")
public record NotificationClickRequest(
    @Schema(description = "읽음 처리할 푸시 알림 ID", example = "1")
    Long pushNotificationId
) {
}
