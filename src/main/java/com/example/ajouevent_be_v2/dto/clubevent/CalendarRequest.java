package com.example.ajouevent_be_v2.dto.clubevent;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Google Calendar 일정 추가 요청")
public record CalendarRequest(
    @Schema(description = "일정 제목", example = "[소프트웨어학과] 2024학년도 1학기 장학금 신청")
    String summary,

    @Schema(description = "일정 설명 (공지 본문 요약)", example = "장학금 신청 기간: 3월 1일 ~ 3월 15일")
    String description,

    @Schema(description = "일정 시작 날짜 (yyyy-MM-dd)", example = "2024-03-01")
    String startDate,

    @Schema(description = "일정 종료 날짜 (yyyy-MM-dd)", example = "2024-03-15")
    String endDate
) {
}
