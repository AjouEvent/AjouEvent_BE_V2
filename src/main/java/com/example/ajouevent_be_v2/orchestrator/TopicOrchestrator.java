package com.example.ajouevent_be_v2.orchestrator;

import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.topic.Topic;
import com.example.ajouevent_be_v2.dto.topic.TopicDetailResponse;
import com.example.ajouevent_be_v2.dto.topic.TopicNotificationUpdateRequest;
import com.example.ajouevent_be_v2.dto.topic.TopicResponse;
import com.example.ajouevent_be_v2.dto.topic.TopicStatusResponse;
import com.example.ajouevent_be_v2.dto.topic.TopicSubscribeRequest;
import com.example.ajouevent_be_v2.dto.topic.TopicUnsubscribeRequest;
import com.example.ajouevent_be_v2.service.token.TokenService;
import com.example.ajouevent_be_v2.service.topic.TopicCommandService;
import com.example.ajouevent_be_v2.service.topic.TopicQueryService;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TopicOrchestrator {

    private final TopicCommandService topicCommandService;
    private final TopicQueryService topicQueryService;
    private final TokenService tokenService;

    public List<TopicDetailResponse> getAllTopics() {
        return topicQueryService.getAllTopics().stream()
                .map(TopicDetailResponse::from)
                .sorted(Comparator.comparing(TopicDetailResponse::koreanOrder))
                .toList();
    }

    @Transactional
    public void subscribeToTopic(TopicSubscribeRequest request, Member member) {
        Topic topic = topicCommandService.subscribeToTopic(request.topic(), member);
        tokenService.subscribeToTopic(topic, member);
    }

    @Transactional
    public void unsubscribeFromTopic(TopicUnsubscribeRequest request, Member member) {
        Topic topic = topicQueryService.findByDepartment(request.topic());
        tokenService.unsubscribeFromTopic(topic, member);
        topicCommandService.deleteTopicMember(topic, member);
    }

    public List<TopicResponse> getSubscribedTopics(Member member) {
        return topicQueryService.getSubscribedTopics(member).stream()
                .map(TopicResponse::from)
                .sorted(Comparator.comparing(TopicResponse::id).reversed())
                .toList();
    }

    @Transactional
    public void resetAllTopicSubscriptions(Member member) {
        tokenService.deleteAllTopicTokens(member);
        topicCommandService.deleteAllTopicMembers(member);
    }

    public List<TopicStatusResponse> getTopicsWithSubscriptionStatus(Member member) {
        return topicQueryService.getTopicsWithSubscriptionStatus(member).stream()
            .map(TopicStatusResponse::from)
            .toList();
    }

    public void updateNotificationPreference(TopicNotificationUpdateRequest request, Member member) {
        topicCommandService.updateNotificationPreference(member, request.topic(), request.receiveNotification());
    }
}
