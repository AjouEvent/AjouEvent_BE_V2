package com.example.ajouevent_be_v2.dto.topic;

public record TopicSubscriptionResult(
    Long id,
    String koreanTopic,
    String englishTopic,
    String classification,
    boolean subscribed,
    boolean receiveNotification,
    Long koreanOrder
) {
}
