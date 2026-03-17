package com.example.ajouevent_be_v2.service.notification;

import com.example.ajouevent_be_v2.domain.keyword.Keyword;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.notification.NotificationType;
import com.example.ajouevent_be_v2.domain.notification.PushNotification;
import com.example.ajouevent_be_v2.domain.push.PushCluster;
import com.example.ajouevent_be_v2.domain.topic.Topic;
import com.example.ajouevent_be_v2.dto.push.FcmMessageCommand;
import com.example.ajouevent_be_v2.dto.push.UnreadNotificationCountResult;
import com.example.ajouevent_be_v2.repository.port.notification.PushNotificationRepositoryPort;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final PushNotificationRepositoryPort pushNotificationRepositoryPort;

    @Transactional
    public void saveAll(
        PushCluster cluster, Topic topic, Keyword keyword,
        List<Member> members, NotificationType type) {

        List<PushNotification> notifications = members.stream()
            .map(member -> PushNotification.builder()
                .pushCluster(cluster)
                .topic(topic)
                .keyword(keyword)
                .member(member)
                .notificationType(type)
                .title(cluster.getTitle())
                .body(cluster.getBody())
                .imageUrl(cluster.getImageUrl())
                .clickUrl(cluster.getClickUrl())
                .isRead(false)
                .notifiedAt(LocalDateTime.now())
                .build())
            .collect(Collectors.toList());

        pushNotificationRepositoryPort.saveAll(notifications);
    }

    public Map<Long, Long> countUnreadByCommand(FcmMessageCommand command) {
        return command.koreanTopic() != null
            ? countUnreadByTopic(command.koreanTopic())
            : countUnreadByKeyword(command.encodedKeyword());
    }

    public Map<Long, Long> countUnreadByTopic(String koreanTopic) {
        return toUnreadCountMap(
            pushNotificationRepositoryPort.countUnreadNotificationsForTopic(koreanTopic));
    }

    public Map<Long, Long> countUnreadByKeyword(String encodedKeyword) {
        return toUnreadCountMap(
            pushNotificationRepositoryPort.countUnreadNotificationsForKeyword(encodedKeyword));
    }

    private Map<Long, Long> toUnreadCountMap(List<UnreadNotificationCountResult> results) {
        return results.stream()
            .collect(Collectors.toMap(
                UnreadNotificationCountResult::memberId,
                UnreadNotificationCountResult::unreadNotificationCount));
    }
}
