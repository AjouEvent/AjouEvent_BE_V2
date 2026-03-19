package com.example.ajouevent_be_v2.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "[LOCAL 전용] FCM 토큰 포함 테스트 로그인 요청")
public record TestLoginWithFcmRequest(
    @Schema(description = "로그인할 회원 이메일", example = "test@ajou.ac.kr")
    String email,
    @Schema(description = "FCM 디바이스 토큰")
    String fcmToken
) {
}
