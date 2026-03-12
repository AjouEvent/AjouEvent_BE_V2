package com.example.ajouevent_be_v2.service.subscription;

import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.repository.port.keyword.KeywordMemberRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.topic.TopicMemberRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// V1 참고: service/SubscriptionService.java — isSubscribedTabRead() 포팅
@Service
@RequiredArgsConstructor
public class SubscriptionQueryService {

    private final TopicMemberRepositoryPort topicMemberRepositoryPort;
    private final KeywordMemberRepositoryPort keywordMemberRepositoryPort;

    public boolean isSubscribedTabRead(Member member) {
        boolean hasUnreadTopics = topicMemberRepositoryPort.existsByMemberAndIsReadFalse(member);
        boolean hasUnreadKeywords = keywordMemberRepositoryPort.existsByMemberAndIsReadFalse(member);
        return !(hasUnreadTopics || hasUnreadKeywords);
    }
}
