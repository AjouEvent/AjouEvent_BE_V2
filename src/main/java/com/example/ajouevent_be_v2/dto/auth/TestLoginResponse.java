package com.example.ajouevent_be_v2.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "[LOCAL 전용] 테스트 로그인 응답")
public record TestLoginResponse(
    @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    String accessToken,

    @Schema(description = "회원 이름", example = "홍길동")
    String name,

    @Schema(description = "전공", example = "소프트웨어학과")
    String major,

    @Schema(description = "아주대학교 Google 계정 이메일", example = "test@ajou.ac.kr")
    String email,

    @Schema(description = "최초 로그인 여부", example = "false")
    Boolean isNewMember,

    @Schema(description = "FCM 디바이스 토큰", example = "fME9z4k3...")
    String fcmToken
) {
}
