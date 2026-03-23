package com.example.ajouevent_be_v2.dto.auth;

import com.example.ajouevent_be_v2.domain.member.Member;

public record AuthTokenResult(
        Long id,
        String accessToken,
        String refreshToken,
        String name,
        String major,
        String email,
        Boolean isNewMember
) {

    public static AuthTokenResult of(Member member, String accessToken, String refreshToken, Boolean isNewMember) {
        return new AuthTokenResult(
            member.getId(),
            accessToken,
            refreshToken,
            member.getName(),
            member.getMajor(),
            member.getEmail(),
            isNewMember
        );
    }
}
