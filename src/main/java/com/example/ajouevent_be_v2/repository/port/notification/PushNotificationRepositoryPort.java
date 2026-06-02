package com.example.ajouevent_be_v2.repository.port.notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.notification.NotificationType;
import com.example.ajouevent_be_v2.domain.notification.PushNotification;
import com.example.ajouevent_be_v2.dto.push.UnreadNotificationCountResult;
import com.example.ajouevent_be_v2.repository.adapter.notification.PushNotificationJpaRepositoryAdapter;

import lombok.RequiredArgsConstructor;

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


    public Slice<PushNotification> findByMemberAndNotificationType(
        Member member, NotificationType notificationType, Pageable pageable) {
        return pushNotificationJpaRepositoryAdapter
            .findByMemberAndNotificationType(member, notificationType, pageable);
    }

    public Slice<PushNotification> findTopicNotificationsByMemberWithTopic(Member member, Pageable pageable) {
        return pushNotificationJpaRepositoryAdapter.findTopicNotificationsByMemberWithTopic(member, pageable);
    }

    public Slice<PushNotification> findKeywordNotificationsByMemberWithTopicAndKeyword(Member member, Pageable pageable) {
        return pushNotificationJpaRepositoryAdapter
            .findKeywordNotificationsByMemberWithTopicAndKeyword(member, pageable);
    }

    public Optional<PushNotification> findByMemberAndId(Member member, Long id) {
        return pushNotificationJpaRepositoryAdapter.findByMemberAndId(member, id);
    }

    public long countByMemberAndIsReadFalse(Member member) {
        return pushNotificationJpaRepositoryAdapter.countByMemberAndIsReadFalse(member);
    }

    public void updateReadStatusByIds(List<Long> ids, LocalDateTime now) {
        pushNotificationJpaRepositoryAdapter.updateReadStatusByIds(ids, now);
    }

    public PushNotification save(PushNotification pushNotification) {
        return pushNotificationJpaRepositoryAdapter.save(pushNotification);
    }

    public void updateAllUnreadByMember(Member member, LocalDateTime now) {
        pushNotificationJpaRepositoryAdapter.updateAllUnreadByMember(member, now);
    }

    public Optional<PushNotification> findFirstByPushClusterId(Long pushClusterId) {
        return pushNotificationJpaRepositoryAdapter.findFirstByPushClusterId(pushClusterId);
    }
}
