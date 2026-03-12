package com.example.ajouevent_be_v2.controller;

import com.example.ajouevent_be_v2.common.auth.AuthUser;
import com.example.ajouevent_be_v2.controller.docs.FcmTokenControllerDocs;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.token.FcmTokenRequest;
import com.example.ajouevent_be_v2.orchestrator.FcmTokenOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// V1 참고: controller/FCMController.java
@RestController
@RequiredArgsConstructor
public class FcmTokenController implements FcmTokenControllerDocs {

    private final FcmTokenOrchestrator fcmTokenOrchestrator;

    @PostMapping("/send/registration-token")
    public ResponseEntity<Void> saveToken(@AuthUser Member member, @RequestBody FcmTokenRequest request) {
        fcmTokenOrchestrator.saveToken(request, member);
        return ResponseEntity.ok().build();
    }
}
