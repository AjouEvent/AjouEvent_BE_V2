package com.example.ajouevent_be_v2.dto.webhook;

public record WebhookResponse(
        String result,
        String topic,
        String title,
        Long eventId
) {
}
