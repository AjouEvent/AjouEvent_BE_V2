package com.example.ajouevent_be_v2.orchestrator;

import com.example.ajouevent_be_v2.domain.clubevent.ClubEvent;
import com.example.ajouevent_be_v2.domain.notification.NotificationType;
import com.example.ajouevent_be_v2.domain.topic.Topic;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventCommand;
import com.example.ajouevent_be_v2.dto.push.PushClusterSendRequest;
import com.example.ajouevent_be_v2.service.keyword.KeywordQueryService;
import com.example.ajouevent_be_v2.service.notification.PushNotificationService;
import com.example.ajouevent_be_v2.service.push.PushClusterCommandService;
import com.example.ajouevent_be_v2.service.topic.TopicQueryService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushOrchestrator {

    private final PushClusterCommandService pushClusterCommandService;
    private final PushNotificationService pushNotificationService;
    private final TopicQueryService topicQueryService;
    private final KeywordQueryService keywordQueryService;

    public List<PushClusterSendRequest> createClusters(ClubEvent clubEvent, ClubEventCommand command) {
        List<PushClusterSendRequest> results = new ArrayList<>();

        Topic topic = topicQueryService.findByKoreanTopic(command.koreanTopic());

        pushClusterCommandService.createTopicPushCluster(clubEvent, topic, command)
                .ifPresent(result -> {
                            pushNotificationService.saveAll(result.cluster(), topic, null, result.members(), NotificationType.TOPIC);
                            results.add(result.sendRequest());
                        }
                );

        keywordQueryService.findMatchingByClubEvent(topic, command).forEach(keyword ->
                pushClusterCommandService.createKeywordPushCluster(clubEvent, keyword, command)
                        .ifPresent(result -> {
                            pushNotificationService.saveAll(result.cluster(), null, keyword, result.members(), NotificationType.KEYWORD);
                            results.add(result.sendRequest());
                        })
        );

        log.info("PushCluster 생성 완료 - ClubEventId: {}, 클러스터 수: {}", clubEvent.getEventId(), results.size());
        return results;
    }
}
