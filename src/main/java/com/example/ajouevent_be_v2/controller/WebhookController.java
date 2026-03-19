package com.example.ajouevent_be_v2.controller;

import com.example.ajouevent_be_v2.controller.docs.WebhookControllerDocs;
import com.example.ajouevent_be_v2.dto.webhook.WebhookRequest;
import com.example.ajouevent_be_v2.dto.webhook.WebhookResponse;
import com.example.ajouevent_be_v2.orchestrator.WebhookOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WebhookController implements WebhookControllerDocs {

    private final WebhookOrchestrator webhookOrchestrator;

    @Override
    @PostMapping("/api/webhook/crawling")
    public ResponseEntity<WebhookResponse> handleWebhook(
            @RequestHeader("crawling-token") String token,
            @RequestBody WebhookRequest webhookRequest) {
        return ResponseEntity.ok(webhookOrchestrator.processWebhook(token, webhookRequest));
    }
}
