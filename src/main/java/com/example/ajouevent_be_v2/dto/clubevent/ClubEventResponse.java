package com.example.ajouevent_be_v2.dto.clubevent;

import com.example.ajouevent_be_v2.domain.event.ClubEvent;
import java.time.LocalDateTime;
import java.util.List;

public record ClubEventResponse(
    Long eventId,
    String title,
    String writer,
    String imgUrl,
    LocalDateTime createdAt,
    Long likesCount,
    Long viewCount,
    boolean star,
    String subject,
    String type,
    String url
) {
    public static ClubEventResponse from(ClubEvent event, boolean star) {
        List<?> images = event.getClubEventImageList();
        String imgUrl = (images != null && !images.isEmpty())
            ? event.getClubEventImageList().get(0).getUrl()
            : null;
        return new ClubEventResponse(
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
            event.getUrl()
        );
    }
}
