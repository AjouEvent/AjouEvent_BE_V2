package com.example.ajouevent_be_v2.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ajouevent_be_v2.common.auth.AuthUser;
import com.example.ajouevent_be_v2.common.dto.SliceResponse;
import com.example.ajouevent_be_v2.controller.docs.NotificationControllerDocs;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.notification.KeywordNotificationResponse;
import com.example.ajouevent_be_v2.dto.notification.TopicNotificationResponse;
import com.example.ajouevent_be_v2.dto.notification.UnreadNotificationCountResponse;
import com.example.ajouevent_be_v2.orchestrator.NotificationOrchestrator;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationControllerDocs {

    private final NotificationOrchestrator notificationOrchestrator;

    @Override
    @GetMapping("/api/v2/notification/topic")
    public ResponseEntity<SliceResponse<TopicNotificationResponse>> getTopicNotifications(
        @AuthUser Member member,
        @PageableDefault(size = 10, sort = "notifiedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(notificationOrchestrator.getTopicNotifications(member, pageable));
    }

    @Override
    @GetMapping("/api/v2/notification/keyword")
    public ResponseEntity<SliceResponse<KeywordNotificationResponse>> getKeywordNotifications(
        @AuthUser Member member,
        @PageableDefault(size = 10, sort = "notifiedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(notificationOrchestrator.getKeywordNotifications(member, pageable));
    }

    @Override
    @PostMapping("/api/v2/notification/{id}")
    public ResponseEntity<Void> markNotificationAsRead(
        @PathVariable Long id, @AuthUser Member member) {
        notificationOrchestrator.markNotificationAsRead(id, member);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/api/v2/notification/unread-count")
    public ResponseEntity<UnreadNotificationCountResponse> getUnreadCount(@AuthUser Member member) {
        return ResponseEntity.ok(notificationOrchestrator.getUnreadCount(member));
    }

    @Override
    @PutMapping("/api/v2/notification/readAll")
    public ResponseEntity<Void> markAllNotificationsAsRead(@AuthUser Member member) {
        notificationOrchestrator.markAllNotificationsAsRead(member);
        return ResponseEntity.noContent().build();
    }
}
