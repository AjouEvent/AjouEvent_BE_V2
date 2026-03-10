package com.example.ajouevent_be_v2.controller;

import com.example.ajouevent_be_v2.config.properties.JwtProperties;
import com.example.ajouevent_be_v2.controller.docs.AuthControllerDocs;
import com.example.ajouevent_be_v2.dto.auth.AuthTokenResult;
import com.example.ajouevent_be_v2.dto.auth.LoginResponse;
import com.example.ajouevent_be_v2.dto.auth.OauthRequest;
import com.example.ajouevent_be_v2.orchestrator.auth.AuthOrchestrator;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private final AuthOrchestrator authOrchestrator;
    private final JwtProperties jwtProperties;

    @PostMapping("/api/v2/auth/login")
    public ResponseEntity<LoginResponse> login(OauthRequest request) {
        AuthTokenResult result = authOrchestrator.socialLogin(request);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, refreshCookie(result.refreshToken()).toString())
            .body(toLoginResponse(result));
    }

    @PatchMapping("/api/v2/auth/reissue")
    public ResponseEntity<LoginResponse> reissueToken(String refreshToken) {
        AuthTokenResult result = authOrchestrator.reissueToken(refreshToken);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, refreshCookie(result.refreshToken()).toString())
            .body(toLoginResponse(result));
    }

    private LoginResponse toLoginResponse(AuthTokenResult result) {
        return new LoginResponse(result.accessToken(), result.name(), result.major(), result.email(), result.isNewMember());
    }

    private ResponseCookie refreshCookie(String value) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, value)
            .httpOnly(true)
            .path("/")
            .maxAge(Duration.ofMillis(jwtProperties.getRefreshTokenExpiration()))
            .sameSite("Lax")
            .build();
    }
}
