package com.example.ajouevent_be_v2.controller;

import com.example.ajouevent_be_v2.dto.auth.AuthTokenResult;
import com.example.ajouevent_be_v2.dto.auth.LoginResponse;
import com.example.ajouevent_be_v2.dto.auth.TestLoginRequest;
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
        description = "이미 가입된 회원의 이메일로 즉시 JWT Access Token을 발급합니다. local 환경에서만 활성화됩니다."
    )
    @PostMapping("/api/v2/auth/test-login")
    public ResponseEntity<LoginResponse> testLogin(@RequestBody TestLoginRequest request) {
        AuthTokenResult result = authOrchestrator.testLogin(request.email());
        return ResponseEntity.ok(new LoginResponse(result.accessToken(), result.name(), result.major(), result.email(), result.isNewMember()));
    }
}
