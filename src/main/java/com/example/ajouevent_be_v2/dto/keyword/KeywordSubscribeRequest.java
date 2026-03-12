package com.example.ajouevent_be_v2.dto.keyword;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "키워드 구독 요청")
public record KeywordSubscribeRequest(
    @Schema(description = "구독할 키워드 (한글)", example = "장학금")
    String koreanKeyword,

    @Schema(description = "키워드가 속한 토픽의 영문 식별자 (department)", example = "Software")
    String topicName
) {}
