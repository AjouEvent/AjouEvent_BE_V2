package com.example.ajouevent_be_v2.dto.topic;

import com.example.ajouevent_be_v2.domain.topic.TopicMember;
import java.time.LocalDateTime;

public record TopicResponse(
    Long id,
    String koreanTopic,
    String englishTopic,
    boolean isRead,
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
