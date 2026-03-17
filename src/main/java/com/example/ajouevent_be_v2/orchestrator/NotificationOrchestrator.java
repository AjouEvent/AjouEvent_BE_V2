package com.example.ajouevent_be_v2.orchestrator;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.ajouevent_be_v2.common.dto.SliceResponse;
import com.example.ajouevent_be_v2.common.dto.SliceResult;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.notification.PushNotification;
import com.example.ajouevent_be_v2.dto.notification.KeywordNotificationResponse;
import com.example.ajouevent_be_v2.dto.notification.TopicNotificationResponse;
import com.example.ajouevent_be_v2.dto.notification.UnreadNotificationCountResponse;
import com.example.ajouevent_be_v2.service.notification.NotificationCommandService;
import com.example.ajouevent_be_v2.service.notification.NotificationQueryService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationOrchestrator {

    private final NotificationQueryService notificationQueryService;
    private final NotificationCommandService notificationCommandService;

    @Transactional
    public SliceResponse<TopicNotificationResponse> getTopicNotifications(Member member, Pageable pageable) {
        SliceResult<PushNotification> sliceResult = notificationQueryService.getTopicNotifications(member, pageable);
        notificationCommandService.markPageNotificationsAsRead(sliceResult.result());
        return SliceResponse.from(sliceResult, TopicNotificationResponse::from);
    }

    @Transactional
    public SliceResponse<KeywordNotificationResponse> getKeywordNotifications(Member member, Pageable pageable) {
        SliceResult<PushNotification> sliceResult = notificationQueryService.getKeywordNotifications(member, pageable);
        notificationCommandService.markPageNotificationsAsRead(sliceResult.result());
        return SliceResponse.from(sliceResult, KeywordNotificationResponse::from);
    }

    @Transactional
    public void markNotificationAsRead(Long id, Member member) {
        PushNotification notification = notificationQueryService.getNotificationByMemberAndId(member, id);
        notificationCommandService.markAsRead(notification);
    }

    public UnreadNotificationCountResponse getUnreadCount(Member member) {
        long count = notificationQueryService.getUnreadCount(member);
        return new UnreadNotificationCountResponse(count);
    }

    @Transactional
    public void markAllNotificationsAsRead(Member member) {
        List<PushNotification> unreadNotifications = notificationQueryService.getUnreadNotifications(member);
        if (!unreadNotifications.isEmpty()) {
            notificationCommandService.markAllAsRead(unreadNotifications);
        }
    }
}
