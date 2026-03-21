package com.example.ajouevent_be_v2.controller;

import com.example.ajouevent_be_v2.dto.webhook.WebhookRequest;
import com.example.ajouevent_be_v2.dto.webhook.WebhookResponse;
import com.example.ajouevent_be_v2.orchestrator.WebhookOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Profile({"local", "test"})
public class TestWebhookController {

    private final WebhookOrchestrator webhookOrchestrator;

    @PostMapping("/api/v2/test/webhook/sync")
    public ResponseEntity<WebhookResponse> handleWebhookSync(
            @RequestHeader("crawling-token") String token,
            @RequestBody WebhookRequest webhookRequest) {
        return ResponseEntity.ok(webhookOrchestrator.processWebhookSync(token, webhookRequest));
    }
}
