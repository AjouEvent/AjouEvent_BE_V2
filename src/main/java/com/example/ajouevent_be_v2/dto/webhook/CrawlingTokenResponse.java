package com.example.ajouevent_be_v2.dto.webhook;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "크롤링 인증 토큰 발급 응답")
public record CrawlingTokenResponse(
    @Schema(description = "발급된 크롤링 인증 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    String crawlingToken
) {
}
