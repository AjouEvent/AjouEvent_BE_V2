package com.example.ajouevent_be_v2.controller;

import com.example.ajouevent_be_v2.common.auth.AuthUser;
import com.example.ajouevent_be_v2.dto.topic.TopicDetailResponse;
import com.example.ajouevent_be_v2.dto.topic.TopicNotificationUpdateRequest;
import com.example.ajouevent_be_v2.dto.topic.TopicResponse;
import com.example.ajouevent_be_v2.dto.topic.TopicStatusResponse;
import com.example.ajouevent_be_v2.dto.topic.TopicSubscribeRequest;
import com.example.ajouevent_be_v2.dto.topic.TopicUnsubscribeRequest;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.orchestrator.TopicOrchestrator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TopicController {

    private final TopicOrchestrator topicOrchestrator;

    @GetMapping("/api/v2/topics")
    public ResponseEntity<List<TopicDetailResponse>> getAllTopics() {
        return ResponseEntity.ok(topicOrchestrator.getAllTopics());
    }

    @GetMapping("/api/v2/topics/subscriptions")
    public ResponseEntity<List<TopicResponse>> getSubscribedTopics(@AuthUser Member member) {
        return ResponseEntity.ok(topicOrchestrator.getSubscribedTopics(member));
    }

    @PostMapping("/api/v2/topics/subscriptions")
    public ResponseEntity<Void> subscribeToTopic(@RequestBody TopicSubscribeRequest request, @AuthUser Member member) {
        topicOrchestrator.subscribeToTopic(request, member);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/v2/topics/subscriptions")
    public ResponseEntity<Void> unsubscribeFromTopic(@RequestBody TopicUnsubscribeRequest request, @AuthUser Member member) {
        topicOrchestrator.unsubscribeFromTopic(request, member);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/v2/topics/subscriptions/reset")
    public ResponseEntity<Void> resetAllTopicSubscriptions(@AuthUser Member member) {
        topicOrchestrator.resetAllTopicSubscriptions(member);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v2/topics/subscriptions/status")
    public ResponseEntity<List<TopicStatusResponse>> getTopicsWithSubscriptionStatus(@AuthUser Member member) {
        return ResponseEntity.ok(topicOrchestrator.getTopicsWithSubscriptionStatus(member));
    }

    @PostMapping("/api/v2/topics/subscriptions/notification")
    public ResponseEntity<Void> updateNotificationPreference(@RequestBody TopicNotificationUpdateRequest request, @AuthUser Member member) {
        topicOrchestrator.updateNotificationPreference(request, member);
        return ResponseEntity.noContent().build();
    }
}
