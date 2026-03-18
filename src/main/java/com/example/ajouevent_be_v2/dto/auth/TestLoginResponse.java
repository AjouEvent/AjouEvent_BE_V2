package com.example.ajouevent_be_v2.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "[LOCAL 전용] 테스트 로그인 응답")
public record TestLoginResponse(
    @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    String accessToken,

    @Schema(description = "회원 이름", example = "테스트유저")
    String name,

    @Schema(description = "전공", example = "소프트웨어학과")
    String major,

    @Schema(description = "이메일", example = "test@ajou.ac.kr")
    String email,

    @Schema(description = "최초 로그인 여부", example = "false")
    Boolean isNewMember,

    @Schema(description = "등록된 FCM 토큰 (당일 만료)", example = "dkjfhaksdjfhaksjdfh...")
    String fcmToken
) {
}
