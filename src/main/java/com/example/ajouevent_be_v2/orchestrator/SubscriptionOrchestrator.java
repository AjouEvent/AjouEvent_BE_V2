package com.example.ajouevent_be_v2.orchestrator;

import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.subscription.TabReadStatusResponse;
import com.example.ajouevent_be_v2.service.keyword.KeywordQueryService;
import com.example.ajouevent_be_v2.service.topic.TopicQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * V1 참고 파일: controller/SubscriptionController.java, service/SubscriptionService.java
 *
 * V1과의 차이점:
 * - V1: SubscriptionController → SubscriptionService (SecurityContextHolder 직접 참조)
 * - V2: SubscriptionController → SubscriptionOrchestrator → TopicQueryService + KeywordQueryService
 *   컨트롤러에서 @AuthUser로 Member를 직접 주입받아 Orchestrator에 전달
 *
 * 구현된 V1 메서드 대응:
 * - isSubscribedTabRead ← SubscriptionService.isSubscribedTabRead()
 */
@Component
@RequiredArgsConstructor
public class SubscriptionOrchestrator {

    private final TopicQueryService topicQueryService;
    private final KeywordQueryService keywordQueryService;

    public TabReadStatusResponse isSubscribedTabRead(Member member) {
        boolean hasUnread = topicQueryService.hasUnreadTopics(member)
            || keywordQueryService.hasUnreadKeywords(member);
        return new TabReadStatusResponse(!hasUnread);
    }
}
