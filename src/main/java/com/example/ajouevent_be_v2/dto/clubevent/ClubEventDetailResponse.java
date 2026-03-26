package com.example.ajouevent_be_v2.dto.clubevent;

import com.example.ajouevent_be_v2.domain.clubevent.ClubEvent;
import com.example.ajouevent_be_v2.domain.clubevent.ClubEventImage;
import java.time.LocalDateTime;
import java.util.List;

public record ClubEventDetailResponse(
    Long eventId,
    String title,
    String writer,
    List<String> imgUrl,
    LocalDateTime createdAt,
    Long likesCount,
    Long viewCount,
    boolean star, // 찜(like) 여부 — FE 호환을 위해 star 필드명 유지
    String subject,
    String type,
    String url,
    String content
) {
    public static ClubEventDetailResponse from(ClubEvent event, boolean star) {
        List<String> imgUrl = (event.getClubEventImageList() != null)
            ? event.getClubEventImageList().stream().map(ClubEventImage::getUrl).toList()
            : List.of();
        return new ClubEventDetailResponse(
            event.getEventId(),
            event.getTitle(),
            event.getWriter(),
            imgUrl,
            event.getCreatedAt(),
            event.getLikesCount(),
            event.getViewCount(),
            star,
            event.getSubject(),
            event.getType() != null ? event.getType().name() : null,
            event.getUrl(),
            event.getContent()
        );
    }
}
