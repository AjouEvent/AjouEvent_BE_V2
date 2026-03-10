package com.example.ajouevent_be_v2.service.auth;

import com.example.ajouevent_be_v2.common.util.JwtUtil;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.auth.AuthTokenResult;
import com.example.ajouevent_be_v2.dto.auth.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;

    public AuthTokenResult issueTokens(Member member, Boolean isNewMember) {
        String accessToken = jwtUtil.generateAccessToken(member.getEmail(), member.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(member.getEmail());
        LoginResponse loginResponse = new LoginResponse(
            accessToken, member.getName(), member.getMajor(), member.getEmail(), isNewMember
        );
        return new AuthTokenResult(loginResponse, refreshToken);
    }

    public String extractEmail(String token) {
        jwtUtil.validateToken(token);
        return jwtUtil.getEmailFromToken(token);
    }
}
