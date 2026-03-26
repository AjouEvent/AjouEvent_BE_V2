package com.example.ajouevent_be_v2.controller;

import com.example.ajouevent_be_v2.common.auth.AuthUser;
import com.example.ajouevent_be_v2.controller.docs.TopicControllerDocs;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.topic.TopicDetailResponse;
import com.example.ajouevent_be_v2.dto.topic.TopicNotificationUpdateRequest;
import com.example.ajouevent_be_v2.dto.topic.TopicResponse;
import com.example.ajouevent_be_v2.dto.topic.TopicStatusResponse;
import com.example.ajouevent_be_v2.dto.topic.TopicSubscribeRequest;
import com.example.ajouevent_be_v2.dto.topic.TopicUnsubscribeRequest;
import com.example.ajouevent_be_v2.orchestrator.TopicOrchestrator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TopicController implements TopicControllerDocs {

    private final TopicOrchestrator topicOrchestrator;

    @Override
    @GetMapping("/api/topic/all")
    public ResponseEntity<List<TopicDetailResponse>> getAllTopics() {
        return ResponseEntity.ok(topicOrchestrator.getAllTopics());
    }

    @Override
    @GetMapping("/api/topic/subscriptions")
    public ResponseEntity<List<TopicResponse>> getSubscribedTopics(@AuthUser Member member) {
        return ResponseEntity.ok(topicOrchestrator.getSubscribedTopics(member));
    }

    @Override
    @PostMapping("/api/topic/subscribe")
    public ResponseEntity<Void> subscribeToTopic(@RequestBody TopicSubscribeRequest request, @AuthUser Member member) {
        topicOrchestrator.subscribeToTopic(request, member);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/api/topic/unsubscribe")
    public ResponseEntity<Void> unsubscribeFromTopic(@RequestBody TopicUnsubscribeRequest request, @AuthUser Member member) {
        topicOrchestrator.unsubscribeFromTopic(request, member);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/api/topic/subscriptions/reset")
    public ResponseEntity<Void> resetAllTopicSubscriptions(@AuthUser Member member) {
        topicOrchestrator.resetAllTopicSubscriptions(member);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/api/topic/subscriptionsStatus")
    public ResponseEntity<List<TopicStatusResponse>> getTopicsWithSubscriptionStatus(@AuthUser Member member) {
        return ResponseEntity.ok(topicOrchestrator.getTopicsWithSubscriptionStatus(member));
    }

    @Override
    @PatchMapping("/api/topic/subscriptions/notification")
    public ResponseEntity<Void> updateNotificationPreference(
        @RequestBody TopicNotificationUpdateRequest request, @AuthUser Member member) {
        topicOrchestrator.updateNotificationPreference(request, member);
        return ResponseEntity.noContent().build();
    }
}
