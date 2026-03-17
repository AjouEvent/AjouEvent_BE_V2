package com.example.ajouevent_be_v2.service.topic;

import com.example.ajouevent_be_v2.common.exception.topic.TopicErrorCode;
import com.example.ajouevent_be_v2.common.exception.topic.TopicException;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.topic.Topic;
import com.example.ajouevent_be_v2.domain.topic.TopicMember;
import com.example.ajouevent_be_v2.repository.port.topic.TopicMemberRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.topic.TopicRepositoryPort;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicCommandService {

    private final TopicRepositoryPort topicRepositoryPort;
    private final TopicMemberRepositoryPort topicMemberRepositoryPort;

    @Transactional
    public Topic subscribeToTopic(String topicName, Member member) {
        Topic topic = topicRepositoryPort.findByDepartment(topicName)
            .orElseThrow(() -> new TopicException(TopicErrorCode.TOPIC_NOT_FOUND));

        if (topicMemberRepositoryPort.existsByTopicAndMember(topic, member)) {
            throw new TopicException(TopicErrorCode.ALREADY_SUBSCRIBED);
        }

        TopicMember topicMember = TopicMember.builder()
            .topic(topic)
            .member(member)
            .isRead(false)
            .lastReadAt(LocalDateTime.now())
            .receiveNotification(true)
            .build();
        topicMemberRepositoryPort.save(topicMember);

        return topic;
    }

    @Transactional
    public void deleteTopicMember(Topic topic, Member member) {
        topicMemberRepositoryPort.deleteByTopicAndMember(topic, member);
    }

    @Transactional
    public void deleteAllTopicMembers(Member member) {
        List<Long> topicMemberIds = topicMemberRepositoryPort.findByMember(member).stream()
            .map(TopicMember::getId)
            .toList();

        if (!topicMemberIds.isEmpty()) {
            topicMemberRepositoryPort.deleteAllByIds(topicMemberIds);
        }
    }

    @Transactional
    public void markTopicAsRead(Member member, String topicName) {
        topicRepositoryPort.findByDepartment(topicName).ifPresent(topic ->
            topicMemberRepositoryPort.findByMemberAndTopic(member, topic).ifPresent(TopicMember::markAsRead)
        );
    }

    @Transactional
    public void updateNotificationPreference(Member member, String topicName, boolean receiveNotification) {
        Topic topic = topicRepositoryPort.findByDepartment(topicName)
            .orElseThrow(() -> new TopicException(TopicErrorCode.TOPIC_NOT_FOUND));

        TopicMember topicMember = topicMemberRepositoryPort.findByMemberAndTopic(member, topic)
            .orElseThrow(() -> new TopicException(TopicErrorCode.SUBSCRIPTION_NOT_FOUND));

        topicMember.updateReceiveNotification(receiveNotification);
    }
}
