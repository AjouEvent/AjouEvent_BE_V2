package com.example.ajouevent_be_v2.service.topic;

import com.example.ajouevent_be_v2.common.exception.topic.TopicErrorCode;
import com.example.ajouevent_be_v2.common.exception.topic.TopicException;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.topic.Topic;
import com.example.ajouevent_be_v2.domain.topic.TopicMember;
import com.example.ajouevent_be_v2.dto.topic.TopicSubscriptionResult;
import com.example.ajouevent_be_v2.repository.port.topic.TopicMemberRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.topic.TopicRepositoryPort;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicQueryService {

    private final TopicRepositoryPort topicRepositoryPort;
    private final TopicMemberRepositoryPort topicMemberRepositoryPort;

    public List<Topic> getAllTopics() {
        return topicRepositoryPort.findAll();
    }

    public Topic findByDepartment(String department) {
        return topicRepositoryPort.findByDepartment(department)
                .orElseThrow(() -> new TopicException(TopicErrorCode.TOPIC_NOT_FOUND));
    }

    public List<TopicMember> getSubscribedTopics(Member member) {
        return topicMemberRepositoryPort.findByMemberWithTopic(member);
    }

    public boolean hasUnreadTopics(Member member) {
        return topicMemberRepositoryPort.existsByMemberAndIsReadFalse(member);
    }

    public List<TopicSubscriptionResult> getTopicsWithSubscriptionStatus(Member member) {
        List<Topic> allTopics = topicRepositoryPort.findAll();
        Map<Long, Boolean> subscriptionMap = topicMemberRepositoryPort.findByMemberWithTopic(member)
            .stream()
            .collect(Collectors.toMap(
                tm -> tm.getTopic().getId(),
                TopicMember::isReceiveNotification
            ));

        return allTopics.stream()
            .map(topic -> new TopicSubscriptionResult(
                topic.getId(),
                topic.getKoreanTopic(),
                topic.getDepartment(),
                topic.getClassification(),
                subscriptionMap.containsKey(topic.getId()),
                subscriptionMap.getOrDefault(topic.getId(), false),
                topic.getKoreanOrder()
            ))
            .sorted(Comparator.comparingLong(TopicSubscriptionResult::koreanOrder))
            .toList();
    }
}
