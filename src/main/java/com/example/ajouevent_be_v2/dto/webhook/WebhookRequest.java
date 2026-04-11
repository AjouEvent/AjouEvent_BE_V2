package com.example.ajouevent_be_v2.dto.webhook;

import com.example.ajouevent_be_v2.dto.clubevent.ClubEventCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "크롤러에서 전달하는 공지 등록 웹훅 요청")
public record WebhookRequest(
    @Schema(description = "공지 제목", example = "[소프트웨어학과] 2024학년도 1학기 장학금 공지")
    String title,

    @Schema(description = "공지 본문 내용", example = "다음과 같이 장학금 신청을 안내드립니다.")
    String content,

    @Schema(description = "공지 카테고리", example = "scholarship")
    String category,

    @Schema(description = "작성 학과 영문 식별자", example = "Software")
    String department,

    @Schema(description = "토픽 영문 식별자", example = "AJOUNORMAL")
    String englishTopic,

    @Schema(description = "토픽 한국어 이름", example = "소프트웨어학과")
    String koreanTopic,

    @Schema(description = "원문 URL", example = "https://www.ajou.ac.kr/kr/ajou/notice.do?mode=view&articleNo=12345")
    String url,

    @Schema(description = "첨부 이미지 URL 목록")
    List<String> images,

    @Schema(description = "공지 등록 시각", example = "2024-03-01T10:00:00")
    LocalDateTime date
) {

    public ClubEventCommand toClubEventCommand() {
        return new ClubEventCommand(title, content, category, department, englishTopic, koreanTopic, url, images, date);
    }
}
