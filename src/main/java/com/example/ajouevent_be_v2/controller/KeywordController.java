package com.example.ajouevent_be_v2.controller;

import com.example.ajouevent_be_v2.common.auth.AuthUser;
import com.example.ajouevent_be_v2.controller.docs.KeywordControllerDocs;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.keyword.KeywordResponse;
import com.example.ajouevent_be_v2.dto.keyword.KeywordSubscribeRequest;
import com.example.ajouevent_be_v2.dto.keyword.KeywordUnsubscribeRequest;
import com.example.ajouevent_be_v2.orchestrator.KeywordOrchestrator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KeywordController implements KeywordControllerDocs {

    private final KeywordOrchestrator keywordOrchestrator;

    @PostMapping("/api/keyword/subscribe")
    public ResponseEntity<Void> subscribeToKeyword(
        @AuthUser Member member,
        @RequestBody KeywordSubscribeRequest request
    ) {
        keywordOrchestrator.subscribeToKeyword(request, member);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/api/keyword/unsubscribe")
    public ResponseEntity<Void> unsubscribeFromKeyword(
        @AuthUser Member member,
        @RequestBody KeywordUnsubscribeRequest request
    ) {
        keywordOrchestrator.unsubscribeFromKeyword(request, member);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/keyword/userKeywords")
    public ResponseEntity<List<KeywordResponse>> getUserKeywords(@AuthUser Member member) {
        return ResponseEntity.ok(keywordOrchestrator.getUserKeywords(member));
    }

    @DeleteMapping("/api/keyword/subscriptions/reset")
    public ResponseEntity<Void> resetKeywordSubscriptions(@AuthUser Member member) {
        keywordOrchestrator.resetAllKeywordSubscriptions(member);
        return ResponseEntity.noContent().build();
    }
}
