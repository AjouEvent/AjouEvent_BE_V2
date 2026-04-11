package com.example.ajouevent_be_v2.dto.clubevent;

import com.example.ajouevent_be_v2.domain.clubevent.ClubEvent;
import java.time.LocalDateTime;
import java.util.List;

public record ClubEventWithKeywordResponse(
    Long eventId,
    String title,
    String content,
    String writer,
    String imgUrl,
    LocalDateTime createdAt,
    Long likesCount,
    Long viewCount,
    boolean star, // 찜(like) 여부 — FE 호환을 위해 star 필드명 유지
    String subject,
    String type,
    String url,
    String keyword
) {
    public static ClubEventWithKeywordResponse from(ClubEvent event, String keyword, boolean star, List<String> imgUrls) {
        String imgUrl = (imgUrls != null && !imgUrls.isEmpty()) ? imgUrls.get(0) : null;
        return new ClubEventWithKeywordResponse(
            event.getEventId(),
            event.getTitle(),
            event.getContent(),
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
