package com.example.ajouevent_be_v2.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 정보 응답")
public record MemberInfoResponse(
    @Schema(description = "회원 이름", example = "홍길동")
    String name,

    @Schema(description = "아주대학교 Google 계정 이메일", example = "gildong@ajou.ac.kr")
    String email,

    @Schema(description = "전공", example = "소프트웨어학과")
    String major
) {
}
