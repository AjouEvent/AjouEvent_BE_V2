package com.example.ajouevent_be_v2.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "[LOCAL 전용] 테스트 로그인 요청")
public record TestLoginRequest(
    @Schema(description = "로그인할 회원 이메일", example = "gildong@ajou.ac.kr")
    String email
) {
}
