package com.example.ajouevent_be_v2.service.notification;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ajouevent_be_v2.domain.notification.PushNotification;
import com.example.ajouevent_be_v2.repository.port.notification.PushNotificationRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationCommandService {

    private final PushNotificationRepositoryPort pushNotificationRepositoryPort;

    @Transactional
    public void markAsRead(PushNotification pushNotification) {
        pushNotification.markAsRead();
        pushNotificationRepositoryPort.save(pushNotification);
    }

    @Transactional
    public void markAllAsRead(List<PushNotification> notifications) {
        List<Long> ids = notifications.stream()
            .map(PushNotification::getId)
            .toList();
        pushNotificationRepositoryPort.updateReadStatusByIds(ids, LocalDateTime.now());
    }

    @Transactional
    public void markPageNotificationsAsRead(List<PushNotification> pageNotifications) {
        List<Long> ids = pageNotifications.stream()
            .map(PushNotification::getId)
            .toList();
        pushNotificationRepositoryPort.updateReadStatusByIdsWhereUnread(ids, LocalDateTime.now());
    }
}
