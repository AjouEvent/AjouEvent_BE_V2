package com.example.ajouevent_be_v2.dto.webhook;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공지 등록 웹훅 처리 결과 응답")
public record WebhookResponse(
    @Schema(description = "처리 결과 메시지", example = "success")
    String result,

    @Schema(description = "등록된 토픽 이름", example = "소프트웨어학과")
    String topic,

    @Schema(description = "등록된 공지 제목", example = "[소프트웨어학과] 2024학년도 1학기 장학금 공지")
    String title,

    @Schema(description = "생성된 게시글 ID", example = "1")
    Long eventId
) {
}
