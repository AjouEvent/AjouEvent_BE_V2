package com.example.ajouevent_be_v2.dto.keyword;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "키워드 구독 취소 요청")
public record KeywordUnsubscribeRequest(
    @Schema(description = "취소할 키워드의 URL 인코딩 식별자 (URLEncode(koreanKeyword) + '_' + topicName)", example = "%EC%9E%A5%ED%95%99%EA%B8%88_Software")
    String encodedKeyword
) {}
