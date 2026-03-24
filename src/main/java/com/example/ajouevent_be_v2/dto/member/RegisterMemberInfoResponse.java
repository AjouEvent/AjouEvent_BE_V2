package com.example.ajouevent_be_v2.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "신규 회원 학과 등록 응답")
public record RegisterMemberInfoResponse(
    @Schema(description = "아주대학교 Google 계정 이메일", example = "gildong@ajou.ac.kr")
    String email,

    @Schema(description = "회원 이름", example = "홍길동")
    String name
) {
}
