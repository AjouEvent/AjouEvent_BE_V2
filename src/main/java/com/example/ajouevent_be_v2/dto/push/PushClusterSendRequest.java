package com.example.ajouevent_be_v2.dto.push;

public record PushClusterSendRequest(
    Long pushClusterId,
    FcmMessageCommand fcmMessageCommand
) {
}
