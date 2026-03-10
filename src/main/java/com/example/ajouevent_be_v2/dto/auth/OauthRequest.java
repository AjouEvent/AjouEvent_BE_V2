package com.example.ajouevent_be_v2.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Google OAuth2 로그인 요청")
public record OauthRequest(
    @Schema(description = "Google OAuth2 인가 코드", example = "4/0AX4XfWh...")
    String authorizationCode,

    @Schema(description = "FCM 디바이스 토큰 (푸시 알림 수신용)", example = "fME9z4k3...")
    String fcmToken,

    @Schema(description = "OAuth2 리다이렉트 URI (Google Cloud Console에 등록된 URI와 일치해야 함)", example = "https://ajouevent.com/oauth/callback")
    String redirectUri
) {
}
