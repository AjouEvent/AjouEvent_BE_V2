package com.example.ajouevent_be_v2.dto.clubevent;

import com.example.ajouevent_be_v2.domain.clubevent.Type;
import java.time.LocalDateTime;

/**
 * 목록 조회 전용 경량 프로젝션.
 * content(TEXT)를 제외하고 contentPreview(VARCHAR)만 포함해
 * 오프-페이지 I/O 없이 인라인으로 읽힌다.
 */
public record ClubEventSummaryResult(
    Long eventId,
    String title,
    String contentPreview,
    String writer,
    LocalDateTime createdAt,
    Long likesCount,
    Long viewCount,
    String subject,
    Type type,
    String url
) {}
