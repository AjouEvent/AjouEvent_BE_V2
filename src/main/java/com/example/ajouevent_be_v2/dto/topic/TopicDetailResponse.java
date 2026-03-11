package com.example.ajouevent_be_v2.dto.topic;

import com.example.ajouevent_be_v2.domain.topic.Topic;

public record TopicDetailResponse(
        String classification,
        Long koreanOrder,
        String koreanTopic
) {

    public static TopicDetailResponse from(Topic topic) {
        return new TopicDetailResponse(topic.getClassification(), topic.getKoreanOrder(), topic.getKoreanTopic());
    }
}
