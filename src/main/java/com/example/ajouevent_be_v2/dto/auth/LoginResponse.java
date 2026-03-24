package com.example.ajouevent_be_v2.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답 (리프레시 토큰은 HttpOnly 쿠키 'refreshToken'으로 전달)")
public record LoginResponse(
    @Schema(description = "회원 ID", example = "1")
    Long id,

    @Schema(description = "토큰 타입", example = "Authorization")
    String grantType,

    @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    String accessToken,

    @Schema(description = "회원 이름", example = "홍길동")
    String name,

    @Schema(description = "전공 (학과 정보가 없으면 null)", example = "소프트웨어학과")
    String major,

    @Schema(description = "아주대학교 Google 계정 이메일", example = "gildong@ajou.ac.kr")
    String email,

    @Schema(description = "최초 로그인 여부 (재발급 시 null)", example = "true")
    Boolean isNewMember
) {
}
