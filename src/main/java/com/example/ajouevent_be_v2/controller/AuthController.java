package com.example.ajouevent_be_v2.controller;

import com.example.ajouevent_be_v2.controller.docs.AuthControllerDocs;
import com.example.ajouevent_be_v2.dto.auth.LoginResponse;
import com.example.ajouevent_be_v2.dto.auth.OauthRequest;
import com.example.ajouevent_be_v2.dto.auth.ReissueTokenRequest;
import com.example.ajouevent_be_v2.orchestrator.auth.AuthOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final AuthOrchestrator authOrchestrator;

    @PostMapping("/api/v2/auth/login")
    public ResponseEntity<LoginResponse> login(@RequestBody OauthRequest request) {
        LoginResponse loginResponse = authOrchestrator.socialLogin(request);
        return ResponseEntity.ok(loginResponse);
    }

    @PatchMapping("/api/v2/auth/reissue-token")
    public ResponseEntity<LoginResponse> reissueAccessToken(@RequestBody ReissueTokenRequest request) {
        LoginResponse token = authOrchestrator.reissueAccessToken(request);
        return ResponseEntity.ok(token);
    }
}
