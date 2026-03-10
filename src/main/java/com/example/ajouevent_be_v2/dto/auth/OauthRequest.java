package com.example.ajouevent_be_v2.dto.auth;

public record OauthRequest(
        String authorizationCode,
        String fcmToken,
        String redirectUri
) {
}
