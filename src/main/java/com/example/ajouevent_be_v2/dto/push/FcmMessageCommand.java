package com.example.ajouevent_be_v2.dto.push;

public record FcmMessageCommand(
    String title,
    String body,
    String imageUrl,
    String clickUrl,
    String koreanTopic,
    String encodedKeyword
) {
}
