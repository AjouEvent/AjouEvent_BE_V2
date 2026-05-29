package com.example.ajouevent_be_v2.dto.auth;

public record GoogleOauthResult(
    GoogleUserInfoResult userInfo,
    String refreshToken
) {
}
