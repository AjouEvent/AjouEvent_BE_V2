package com.example.ajouevent_be_v2.dto.auth;

public record AuthTokenResult(
        LoginResponse loginResponse,
        String refreshToken
) {
}
