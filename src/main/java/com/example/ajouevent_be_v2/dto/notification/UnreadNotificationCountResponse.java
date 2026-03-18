package com.example.ajouevent_be_v2.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "미읽음 알림 수 응답")
public record UnreadNotificationCountResponse(
    @Schema(description = "미읽음 알림 수", example = "5")
    long count
) {
}
