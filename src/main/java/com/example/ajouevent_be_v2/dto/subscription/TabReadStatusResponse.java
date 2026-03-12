package com.example.ajouevent_be_v2.dto.subscription;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "구독 탭 읽음 상태 응답")
public record TabReadStatusResponse(
    @Schema(description = "토픽/키워드 구독 탭의 모든 공지를 읽었는지 여부. true = 모두 읽음, false = 읽지 않은 공지 존재", example = "false")
    boolean isSubscribedTabRead
) {}
