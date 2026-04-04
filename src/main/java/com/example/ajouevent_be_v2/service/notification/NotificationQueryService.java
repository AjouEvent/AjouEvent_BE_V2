package com.example.ajouevent_be_v2.service.notification;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.example.ajouevent_be_v2.common.dto.SliceResult;
import com.example.ajouevent_be_v2.common.exception.notification.NotificationErrorCode;
import com.example.ajouevent_be_v2.common.exception.notification.NotificationException;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.notification.PushNotification;
import com.example.ajouevent_be_v2.repository.port.notification.PushNotificationRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private final PushNotificationRepositoryPort pushNotificationRepositoryPort;

    public SliceResult<PushNotification> getTopicNotifications(Member member, Pageable pageable) {
        Slice<PushNotification> slice = pushNotificationRepositoryPort
            .findTopicNotificationsByMemberWithTopic(member, pageable);
        return SliceResult.from(slice, pageable);
    }

    public SliceResult<PushNotification> getKeywordNotifications(Member member, Pageable pageable) {
        Slice<PushNotification> slice = pushNotificationRepositoryPort
            .findKeywordNotificationsByMemberWithTopicAndKeyword(member, pageable);
        return SliceResult.from(slice, pageable);
    }

    public PushNotification getNotificationByMemberAndId(Member member, Long id) {
        return pushNotificationRepositoryPort.findByMemberAndId(member, id)
            .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));
    }

    public long getUnreadCount(Member member) {
        return pushNotificationRepositoryPort.countByMemberAndIsReadFalse(member);
    }

}
