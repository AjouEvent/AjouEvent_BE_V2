package com.example.ajouevent_be_v2.dto.topic;

public record TopicStatusResponse(
    Long id,
    String koreanTopic,
    String englishTopic,
    String classification,
    boolean subscribed,
    boolean receiveNotification,
    Long koreanOrder
) {

    public static TopicStatusResponse from(TopicSubscriptionResult result) {
        return new TopicStatusResponse(
            result.id(),
            result.koreanTopic(),
            result.englishTopic(),
            result.classification(),
            result.subscribed(),
            result.receiveNotification(),
            result.koreanOrder()
        );
    }
}
