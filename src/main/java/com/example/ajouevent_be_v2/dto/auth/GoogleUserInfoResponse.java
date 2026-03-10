package com.example.ajouevent_be_v2.dto.auth;

public record GoogleUserInfoResponse(
        String email,
        String name,
        String department
) {
}
