package com.example.ajouevent_be_v2.orchestrator.subscription;

import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.subscription.TabReadStatusResponse;
import com.example.ajouevent_be_v2.service.subscription.SubscriptionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * V1 참고 파일: controller/SubscriptionController.java, service/SubscriptionService.java
 *
 * V1과의 차이점:
 * - V1: SubscriptionController → SubscriptionService (SecurityContextHolder 직접 참조)
 * - V2: SubscriptionController → SubscriptionOrchestrator → SubscriptionQueryService
 *   컨트롤러에서 @AuthUser로 Member를 직접 주입받아 Orchestrator에 전달
 *
 * 구현된 V1 메서드 대응:
 * - isSubscribedTabRead ← SubscriptionService.isSubscribedTabRead()
 */
@Component
@RequiredArgsConstructor
public class SubscriptionOrchestrator {

    private final SubscriptionQueryService subscriptionQueryService;

    public TabReadStatusResponse isSubscribedTabRead(Member member) {
        return new TabReadStatusResponse(subscriptionQueryService.isSubscribedTabRead(member));
    }
}
