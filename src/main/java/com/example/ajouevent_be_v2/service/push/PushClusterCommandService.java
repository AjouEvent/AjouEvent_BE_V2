package com.example.ajouevent_be_v2.service.push;

import com.example.ajouevent_be_v2.config.properties.FcmProperties;
import com.example.ajouevent_be_v2.domain.clubevent.ClubEvent;
import com.example.ajouevent_be_v2.domain.clubevent.JobStatus;
import com.example.ajouevent_be_v2.domain.keyword.Keyword;
import com.example.ajouevent_be_v2.domain.keyword.KeywordMember;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.member.Token;
import com.example.ajouevent_be_v2.domain.push.PushCluster;
import com.example.ajouevent_be_v2.domain.push.PushClusterToken;
import com.example.ajouevent_be_v2.domain.topic.Topic;
import com.example.ajouevent_be_v2.domain.topic.TopicMember;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventCommand;
import com.example.ajouevent_be_v2.dto.push.FcmMessageCommand;
import com.example.ajouevent_be_v2.dto.push.PushClusterCreationResult;
import com.example.ajouevent_be_v2.dto.push.PushClusterSendRequest;
import com.example.ajouevent_be_v2.repository.port.keyword.KeywordMemberRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.push.PushClusterRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.push.PushClusterTokenRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.token.TokenRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.topic.TopicMemberRepositoryPort;
import java.time.LocalDateTime;
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
public class PushClusterCommandService {

    private final FcmProperties fcmProperties;
    private final TopicMemberRepositoryPort topicMemberRepositoryPort;
    private final KeywordMemberRepositoryPort keywordMemberRepositoryPort;
    private final TokenRepositoryPort tokenRepositoryPort;
    private final PushClusterRepositoryPort pushClusterRepositoryPort;
    private final PushClusterTokenRepositoryPort pushClusterTokenRepositoryPort;

    @Transactional
    public Optional<PushClusterCreationResult> createTopicPushCluster(
        ClubEvent clubEvent, Topic topic, ClubEventCommand command) {

        List<TopicMember> topicMembers = topicMemberRepositoryPort.findByTopicWithMemberAndReceiveNotificationTrue(topic);
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
        String clickUrl = resolveClickUrl(clubEvent.getEventId());

        PushCluster cluster = savePushCluster(clubEvent, title, body, imageUrl, clickUrl, activeTokens.size());
        savePushClusterTokens(cluster, activeTokens);

        log.info("Topic PushCluster 생성 - clusterId: {}, 대상 토큰 수: {}", cluster.getId(), activeTokens.size());
        FcmMessageCommand fcmCommand = new FcmMessageCommand(title, body, imageUrl, clickUrl, command.koreanTopic(), null);
        return Optional.of(new PushClusterCreationResult(cluster, members, new PushClusterSendRequest(cluster.getId(), fcmCommand)));
    }

    @Transactional
    public Optional<PushClusterCreationResult> createKeywordPushCluster(
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
        String clickUrl = resolveClickUrl(clubEvent.getEventId());

        PushCluster cluster = savePushCluster(clubEvent, title, body, imageUrl, clickUrl, activeTokens.size());
        savePushClusterTokens(cluster, activeTokens);

        log.info("Keyword PushCluster 생성 - clusterId: {}, keyword: {}, 대상 토큰 수: {}",
            cluster.getId(), keyword.getKoreanKeyword(), activeTokens.size());
        FcmMessageCommand fcmCommand = new FcmMessageCommand(title, body, imageUrl, clickUrl, null, keyword.getEncodedKeyword());
        return Optional.of(new PushClusterCreationResult(cluster, members, new PushClusterSendRequest(cluster.getId(), fcmCommand)));
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

    private String buildTopicTitle(ClubEventCommand command) {
        return String.format("[%s]", command.koreanTopic());
    }

    private String resolveImageUrl(ClubEventCommand command) {
        List<String> images = Optional.ofNullable(command.images())
            .filter(imgs -> !imgs.isEmpty())
            .orElseGet(() -> List.of(fcmProperties.getDefaultImageUrl()));
        return images.get(0);
    }

    private String resolveClickUrl(Long eventId) {
        return fcmProperties.getRedirectionUrlPrefix() + eventId;
    }
}
