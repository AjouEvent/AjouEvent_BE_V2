package com.example.ajouevent_be_v2.service.auth;

import com.example.ajouevent_be_v2.common.util.JwtUtil;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.auth.AuthTokenResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;

    public AuthTokenResult issueTokens(Member member, Boolean isNewMember) {
        String accessToken = jwtUtil.generateAccessToken(member.getId(), member.getEmail(), member.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(member.getId());
        return AuthTokenResult.of(member, accessToken, refreshToken, isNewMember);
    }

    public Long extractMemberId(String token) {
        jwtUtil.validateToken(token);
        return jwtUtil.getMemberIdFromToken(token);
    }
}
