package com.example.ajouevent_be_v2.dto.clubevent;

import com.example.ajouevent_be_v2.domain.clubevent.ClubEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "키워드 구독 게시글 목록 조회 응답")
public record ClubEventWithKeywordResponse(
    @Schema(description = "게시글 ID", example = "1")
    Long eventId,

    @Schema(description = "게시글 제목", example = "[소프트웨어학과] 2024학년도 1학기 장학금 공지")
    String title,

    @Schema(description = "게시글 본문 미리보기", example = "다음과 같이 장학금 신청을 안내드립니다...")
    String content,

    @Schema(description = "작성자", example = "소프트웨어학과")
    String writer,

    @Schema(description = "대표 이미지 URL", example = "https://www.ajou.ac.kr/_res/ajou/kr/img/intro/img-symbol.png")
    String imgUrl,

    @Schema(description = "게시글 등록 시각", example = "2024-03-01T10:00:00")
    LocalDateTime createdAt,

    @Schema(description = "찜(좋아요) 수", example = "42")
    Long likesCount,

    @Schema(description = "조회수", example = "128")
    Long viewCount,

    @Schema(description = "현재 사용자의 찜 여부", example = "false")
    boolean star,

    @Schema(description = "게시글 카테고리(토픽) 한국어 이름", example = "소프트웨어학과")
    String subject,

    @Schema(description = "게시글 타입 (ENUM 이름)", example = "AJOUNORMAL")
    String type,

    @Schema(description = "원문 URL", example = "https://www.ajou.ac.kr/kr/ajou/notice.do?mode=view&articleNo=12345")
    String url,

    @Schema(description = "매칭된 구독 키워드", example = "장학금")
    String keyword
) {
    public static ClubEventWithKeywordResponse from(ClubEvent event, String keyword, boolean star, List<String> imgUrls) {
        String imgUrl = (imgUrls != null && !imgUrls.isEmpty()) ? imgUrls.get(0) : null;
        return new ClubEventWithKeywordResponse(
            event.getEventId(),
            event.getTitle(),
            event.getContentPreview(),
            event.getWriter(),
            imgUrl,
            event.getCreatedAt(),
            event.getLikesCount(),
            event.getViewCount(),
            star,
            event.getSubject(),
            event.getType() != null ? event.getType().name() : null,
            event.getUrl(),
            keyword
        );
    }
}
