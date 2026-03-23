package com.example.ajouevent_be_v2.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "신규 회원 학과 등록 요청")
public record RegisterMemberInfoRequest(
    @Schema(description = "전공 (학과명)", example = "소프트웨어학과")
    String major
) {
}
