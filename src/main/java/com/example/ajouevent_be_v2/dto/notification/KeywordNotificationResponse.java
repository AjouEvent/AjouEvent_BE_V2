package com.example.ajouevent_be_v2.dto.notification;

import java.time.LocalDateTime;

import com.example.ajouevent_be_v2.domain.notification.PushNotification;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "키워드 알림 응답")
public record KeywordNotificationResponse(
    @Schema(description = "푸시 알림 ID", example = "1")
    Long id,

    @Schema(description = "알림 제목", example = "[소프트웨어학과] 2024학년도 1학기 장학금 공지")
    String title,

    @Schema(description = "알림 이미지 URL", example = "https://www.ajou.ac.kr/_res/ajou/kr/img/intro/img-symbol.png")
    String imageUrl,

    @Schema(description = "클릭 시 이동할 URL", example = "https://www.ajouevent.com/event/1")
    String clickUrl,

    @Schema(description = "읽음 여부", example = "false")
    boolean isRead,

    @Schema(description = "알림 수신 시각", example = "2024-03-01T10:00:00")
    LocalDateTime notifiedAt,

    @Schema(description = "토픽 한국어 이름", example = "소프트웨어학과")
    String topicName,

    @Schema(description = "매칭된 키워드 이름", example = "장학금")
    String keywordName
) {
    public static KeywordNotificationResponse from(PushNotification pushNotification) {
        return new KeywordNotificationResponse(
            pushNotification.getId(),
            pushNotification.getBody(),
            pushNotification.getImageUrl(),
            pushNotification.getClickUrl(),
            pushNotification.isRead(),
            pushNotification.getNotifiedAt(),
            pushNotification.getTopic().getKoreanTopic(),
            pushNotification.getKeyword().getKoreanKeyword()
        );
    }
}
