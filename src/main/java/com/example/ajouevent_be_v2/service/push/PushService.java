package com.example.ajouevent_be_v2.service.push;

import com.example.ajouevent_be_v2.config.properties.FcmProperties;
import com.example.ajouevent_be_v2.domain.event.ClubEvent;
import com.example.ajouevent_be_v2.domain.event.JobStatus;
import com.example.ajouevent_be_v2.domain.keyword.Keyword;
import com.example.ajouevent_be_v2.domain.keyword.KeywordMember;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.member.Token;
import com.example.ajouevent_be_v2.domain.notification.NotificationType;
import com.example.ajouevent_be_v2.domain.notification.PushNotification;
import com.example.ajouevent_be_v2.domain.push.PushCluster;
import com.example.ajouevent_be_v2.domain.push.PushClusterToken;
import com.example.ajouevent_be_v2.domain.topic.Topic;
import com.example.ajouevent_be_v2.domain.topic.TopicMember;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventCommand;
import com.example.ajouevent_be_v2.dto.push.FcmMessageCommand;
import com.example.ajouevent_be_v2.dto.push.PushClusterSendRequest;
import com.example.ajouevent_be_v2.repository.port.keyword.KeywordMemberRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.keyword.KeywordRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.notification.PushNotificationRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.push.PushClusterRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.push.PushClusterTokenRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.token.TokenRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.topic.TopicMemberRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.topic.TopicRepositoryPort;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushService {

    private final FcmProperties fcmProperties;
    private final TopicRepositoryPort topicRepositoryPort;
    private final TopicMemberRepositoryPort topicMemberRepositoryPort;
    private final KeywordRepositoryPort keywordRepositoryPort;
    private final KeywordMemberRepositoryPort keywordMemberRepositoryPort;
    private final TokenRepositoryPort tokenRepositoryPort;
    private final PushClusterRepositoryPort pushClusterRepositoryPort;
    private final PushClusterTokenRepositoryPort pushClusterTokenRepositoryPort;
    private final PushNotificationRepositoryPort pushNotificationRepositoryPort;

    /**
     * ClubEvent에 대한 Topic + Keyword 구독자 PushCluster를 생성하고,
     * 각 클러스터에 대한 FCM 전송 요청 목록을 반환한다.
     */
    @Transactional
    public List<PushClusterSendRequest> createPushClusters(ClubEvent clubEvent, ClubEventCommand command) {
        List<PushClusterSendRequest> results = new ArrayList<>();

        topicRepositoryPort.findByKoreanTopic(command.koreanTopic()).ifPresent(topic -> {
            createTopicPushCluster(clubEvent, topic, command).ifPresent(results::add);

            List<Keyword> matchingKeywords = findMatchingKeywords(topic, command);
            matchingKeywords.forEach(keyword ->
                createKeywordPushCluster(clubEvent, keyword, command).ifPresent(results::add));
        });

        log.info("PushCluster 생성 완료 - ClubEventId: {}, 생성된 클러스터 수: {}", clubEvent.getEventId(), results.size());
        return results;
    }

    /**
     * Topic 구독자 대상 PushCluster를 생성한다.
     * 알림 수신 설정(receiveNotification=true)인 TopicMember의 활성 토큰만 대상으로 한다.
     */
    private Optional<PushClusterSendRequest> createTopicPushCluster(
        ClubEvent clubEvent, Topic topic, ClubEventCommand command) {

        List<TopicMember> topicMembers =
            topicMemberRepositoryPort.findByTopicWithMemberAndReceiveNotificationTrue(topic);
        if (topicMembers.isEmpty()) {
            log.info("Topic 구독자 없음 - topic: {}", topic.getKoreanTopic());
            return Optional.empty();
        }

        List<Member> members = topicMembers.stream()
            .map(TopicMember::getMember)
            .collect(Collectors.toList());
        List<Token> activeTokens = tokenRepositoryPort.findActiveTokensByMembers(members);
        if (activeTokens.isEmpty()) {
            log.info("활성 토큰 없음 - topic: {}", topic.getKoreanTopic());
            return Optional.empty();
        }

        String title = buildTopicTitle(command);
        String body = command.title();
        String imageUrl = resolveImageUrl(command);
        String clickUrl = resolveClickUrl(command, clubEvent.getEventId());

        PushCluster cluster = savePushCluster(clubEvent, title, body, imageUrl, clickUrl, activeTokens.size());
        savePushClusterTokens(cluster, activeTokens);
        savePushNotifications(cluster, topic, null, members, NotificationType.TOPIC, title, body, imageUrl, clickUrl);

        log.info("Topic PushCluster 생성 - clusterId: {}, 대상 토큰 수: {}", cluster.getId(), activeTokens.size());
        FcmMessageCommand fcmCommand = new FcmMessageCommand(title, body, imageUrl, clickUrl, command.koreanTopic(), null);
        return Optional.of(new PushClusterSendRequest(cluster.getId(), fcmCommand));
    }

    /**
     * Keyword 구독자 대상 PushCluster를 생성한다.
     */
    private Optional<PushClusterSendRequest> createKeywordPushCluster(
        ClubEvent clubEvent, Keyword keyword, ClubEventCommand command) {

        List<KeywordMember> keywordMembers = keywordMemberRepositoryPort.findByKeywordWithMember(keyword);
        if (keywordMembers.isEmpty()) {
            return Optional.empty();
        }

        List<Member> members = keywordMembers.stream()
            .map(KeywordMember::getMember)
            .collect(Collectors.toList());
        List<Token> activeTokens = tokenRepositoryPort.findActiveTokensByMembers(members);
        if (activeTokens.isEmpty()) {
            return Optional.empty();
        }

        String title = keyword.getKoreanKeyword() + " - " + buildTopicTitle(command);
        String body = command.title();
        String imageUrl = resolveImageUrl(command);
        String clickUrl = resolveClickUrl(command, clubEvent.getEventId());

        PushCluster cluster = savePushCluster(clubEvent, title, body, imageUrl, clickUrl, activeTokens.size());
        savePushClusterTokens(cluster, activeTokens);
        savePushNotifications(cluster, null, keyword, members, NotificationType.KEYWORD, title, body, imageUrl, clickUrl);

        log.info("Keyword PushCluster 생성 - clusterId: {}, keyword: {}, 대상 토큰 수: {}",
            cluster.getId(), keyword.getKoreanKeyword(), activeTokens.size());
        FcmMessageCommand fcmCommand = new FcmMessageCommand(title, body, imageUrl, clickUrl, null, keyword.getEncodedKeyword());
        return Optional.of(new PushClusterSendRequest(cluster.getId(), fcmCommand));
    }

    /**
     * 이벤트 제목/내용에 키워드가 포함된 Keyword 목록을 반환한다.
     */
    private List<Keyword> findMatchingKeywords(Topic topic, ClubEventCommand command) {
        return keywordRepositoryPort.findByTopic(topic).stream()
            .filter(keyword -> matchesEvent(keyword, command))
            .collect(Collectors.toList());
    }

    private boolean matchesEvent(Keyword keyword, ClubEventCommand command) {
        String kw = keyword.getKoreanKeyword().toLowerCase();
        return (command.title() != null && command.title().toLowerCase().contains(kw))
            || (command.content() != null && command.content().toLowerCase().contains(kw));
    }

    private PushCluster savePushCluster(
        ClubEvent clubEvent, String title, String body,
        String imageUrl, String clickUrl, int totalCount) {
        return pushClusterRepositoryPort.save(PushCluster.builder()
            .clubEvent(clubEvent)
            .title(title)
            .body(body)
            .imageUrl(imageUrl)
            .clickUrl(clickUrl)
            .jobStatus(JobStatus.PENDING)
            .totalCount(totalCount)
            .successCount(0)
            .failCount(0)
            .registeredAt(LocalDateTime.now())
            .build());
    }

    private void savePushClusterTokens(PushCluster cluster, List<Token> activeTokens) {
        List<PushClusterToken> clusterTokens = activeTokens.stream()
            .map(token -> PushClusterToken.builder()
                .pushCluster(cluster)
                .member(token.getMember())
                .tokenValue(token.getTokenValue())
                .jobStatus(JobStatus.PENDING)
                .requestTime(LocalDateTime.now())
                .build())
            .collect(Collectors.toList());
        pushClusterTokenRepositoryPort.bulkSaveAll(clusterTokens);
    }

    private void savePushNotifications(
        PushCluster cluster, Topic topic, Keyword keyword,
        List<Member> members, NotificationType type,
        String title, String body, String imageUrl, String clickUrl) {

        List<PushNotification> notifications = members.stream()
            .map(member -> PushNotification.builder()
                .pushCluster(cluster)
                .topic(topic)
                .keyword(keyword)
                .member(member)
                .notificationType(type)
                .title(title)
                .body(body)
                .imageUrl(imageUrl)
                .clickUrl(clickUrl)
                .isRead(false)
                .notifiedAt(LocalDateTime.now())
                .build())
            .collect(Collectors.toList());
        pushNotificationRepositoryPort.saveAll(notifications);
    }

    private String buildTopicTitle(ClubEventCommand command) {
        return String.format("[%s]", command.koreanTopic());
    }

    private String resolveImageUrl(ClubEventCommand command) {
        List<String> images = Optional.ofNullable(command.images())
            .filter(imgs -> !imgs.isEmpty())
            .orElseGet(() -> List.of(fcmProperties.getDefaultImageUrl()));
        return images.get(0);
    }

    private String resolveClickUrl(ClubEventCommand command, Long eventId) {
        return Optional.ofNullable(command.url())
            .filter(u -> !u.isEmpty())
            .map(u -> fcmProperties.getRedirectionUrlPrefix() + eventId)
            .orElse(fcmProperties.getDefaultClickActionUrl());
    }
}
