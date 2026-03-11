package com.example.ajouevent_be_v2.dto.topic;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "구독 상태 포함 토픽 응답")
public record TopicStatusResponse(
    @Schema(description = "Topic ID", example = "1")
    Long id,

    @Schema(description = "토픽 한국어 이름", example = "소프트웨어학과")
    String koreanTopic,

    @Schema(description = "토픽 영문 식별자 (department)", example = "Software")
    String englishTopic,

    @Schema(description = "토픽 분류 (예: 학과, 장학, 기숙사 등)", example = "학과")
    String classification,

    @Schema(description = "구독 여부", example = "true")
    boolean subscribed,

    @Schema(description = "알림 수신 여부 (구독 중이 아닌 경우 false)", example = "true")
    boolean receiveNotification,

    @Schema(description = "한국어 정렬 순서", example = "1")
    Long koreanOrder
) {

    public static TopicStatusResponse from(TopicSubscriptionResult result) {
        return new TopicStatusResponse(
            result.id(),
            result.koreanTopic(),
            result.englishTopic(),
            result.classification(),
            result.subscribed(),
            result.receiveNotification(),
            result.koreanOrder()
        );
    }
}
