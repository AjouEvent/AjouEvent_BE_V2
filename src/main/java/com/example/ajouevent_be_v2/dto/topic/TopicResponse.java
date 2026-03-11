package com.example.ajouevent_be_v2.dto.topic;

import com.example.ajouevent_be_v2.domain.topic.TopicMember;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "구독 중인 토픽 응답")
public record TopicResponse(
    @Schema(description = "TopicMember ID", example = "42")
    Long id,

    @Schema(description = "토픽 한국어 이름", example = "소프트웨어학과")
    String koreanTopic,

    @Schema(description = "토픽 영문 식별자 (department)", example = "Software")
    String englishTopic,

    @Schema(description = "공지 읽음 여부", example = "false")
    boolean isRead,

    @Schema(description = "마지막 읽은 시각", example = "2025-03-01T12:00:00")
    LocalDateTime lastReadAt
) {

    public static TopicResponse from(TopicMember topicMember) {
        return new TopicResponse(
            topicMember.getId(),
            topicMember.getTopic().getKoreanTopic(),
            topicMember.getTopic().getDepartment(),
            topicMember.isRead(),
            topicMember.getLastReadAt()
        );
    }
}
