package com.example.ajouevent_be_v2.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 정보 수정 요청 (null 필드는 변경하지 않음)")
public record MemberUpdateRequest(
    @Schema(description = "변경할 이름 (null이면 유지)", example = "홍길동")
    String name,

    @Schema(description = "변경할 전공 (null이면 유지)", example = "소프트웨어학과")
    String major
) {
}
