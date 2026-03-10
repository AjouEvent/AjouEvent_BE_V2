package com.example.ajouevent_be_v2.dto.auth;

public record LoginResponse(
    String accessToken,
    String name,
    String major,
    String email,
    Boolean isNewMember
) {
}
