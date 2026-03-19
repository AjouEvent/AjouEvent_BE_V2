package com.example.ajouevent_be_v2.controller;

import com.example.ajouevent_be_v2.dto.auth.LoginResponse;
import com.example.ajouevent_be_v2.dto.auth.TestLoginRequest;
import com.example.ajouevent_be_v2.dto.auth.TestLoginResponse;
import com.example.ajouevent_be_v2.dto.auth.TestLoginWithFcmRequest;
import com.example.ajouevent_be_v2.orchestrator.AuthOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Test Auth", description = "[LOCAL 전용] 테스트용 인증 API")
@Profile("local")
@RestController
@RequiredArgsConstructor
public class TestAuthController {

    private final AuthOrchestrator authOrchestrator;

    @Operation(
        summary = "[LOCAL 전용] 테스트 로그인",
        description = "이메일로 즉시 JWT Access Token을 발급합니다. FCM 토큰이 이미 DB에 등록된 경우 사용하세요."
    )
    @PostMapping("/api/v2/auth/test-login")
    public ResponseEntity<LoginResponse> testLogin(@RequestBody TestLoginRequest request) {
        return ResponseEntity.ok(authOrchestrator.testLogin(request.email()));
    }

    @Operation(
        summary = "[LOCAL 전용] FCM 토큰 포함 테스트 로그인",
        description = "이메일과 FCM 토큰으로 즉시 JWT Access Token을 발급하고 FCM 토큰을 등록합니다. 최초 1회 또는 토큰 갱신 시 사용하세요."
    )
    @PostMapping("/api/v2/auth/test-login/fcm")
    public ResponseEntity<TestLoginResponse> testLoginWithFcm(@RequestBody TestLoginWithFcmRequest request) {
        return ResponseEntity.ok(authOrchestrator.testLoginWithFcm(request.email(), request.fcmToken()));
    }
}
