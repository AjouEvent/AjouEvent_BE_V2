package com.example.ajouevent_be_v2.repository.port.notification;

import java.util.List;

import com.example.ajouevent_be_v2.domain.notification.PushNotification;
import com.example.ajouevent_be_v2.dto.push.UnreadNotificationCountResult;
import com.example.ajouevent_be_v2.repository.adapter.notification.PushNotificationJpaRepositoryAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PushNotificationRepositoryPort {

    private final PushNotificationJpaRepositoryAdapter pushNotificationJpaRepositoryAdapter;

    public List<PushNotification> saveAll(List<PushNotification> notifications) {
        return pushNotificationJpaRepositoryAdapter.saveAll(notifications);
    }

    public List<UnreadNotificationCountResult> countUnreadNotificationsForTopic(String koreanTopic) {
        return pushNotificationJpaRepositoryAdapter.countUnreadNotificationsForTopic(koreanTopic);
    }

    public List<UnreadNotificationCountResult> countUnreadNotificationsForKeyword(String encodedKeyword) {
        return pushNotificationJpaRepositoryAdapter.countUnreadNotificationsForKeyword(encodedKeyword);
    }
}
