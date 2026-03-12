package com.example.ajouevent_be_v2.controller;

import com.example.ajouevent_be_v2.common.auth.AuthUser;
import com.example.ajouevent_be_v2.controller.docs.SubscriptionControllerDocs;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.subscription.TabReadStatusResponse;
import com.example.ajouevent_be_v2.orchestrator.SubscriptionOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SubscriptionController implements SubscriptionControllerDocs {

    private final SubscriptionOrchestrator subscriptionOrchestrator;

    @Override
    @GetMapping("/api/v2/subscriptions/read-status")
    public ResponseEntity<TabReadStatusResponse> isSubscribedTabRead(@AuthUser Member member) {
        return ResponseEntity.ok(subscriptionOrchestrator.isSubscribedTabRead(member));
    }
}
