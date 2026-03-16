package com.example.ajouevent_be_v2.dto.clubevent;

import com.example.ajouevent_be_v2.domain.event.ClubEvent;
import com.example.ajouevent_be_v2.domain.event.ClubEventImage;
import java.time.LocalDateTime;
import java.util.List;

public record ClubEventDetailResponse(
    Long eventId,
    String title,
    String writer,
    List<String> imgUrls,
    LocalDateTime createdAt,
    Long likesCount,
    Long viewCount,
    boolean star,
    String subject,
    String type,
    String url,
    String content
) {
    public static ClubEventDetailResponse from(ClubEvent event, boolean star) {
        List<String> imgUrls = (event.getClubEventImageList() != null)
            ? event.getClubEventImageList().stream().map(ClubEventImage::getUrl).toList()
            : List.of();
        return new ClubEventDetailResponse(
            event.getEventId(),
            event.getTitle(),
            event.getWriter(),
            imgUrls,
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
