package com.example.ajouevent_be_v2.orchestrator;

import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.keyword.KeywordResponse;
import com.example.ajouevent_be_v2.dto.keyword.KeywordSubscribeCommand;
import com.example.ajouevent_be_v2.dto.keyword.KeywordSubscribeRequest;
import com.example.ajouevent_be_v2.dto.keyword.KeywordUnsubscribeCommand;
import com.example.ajouevent_be_v2.dto.keyword.KeywordUnsubscribeRequest;
import com.example.ajouevent_be_v2.service.subscription.KeywordCommandService;
import com.example.ajouevent_be_v2.service.subscription.KeywordQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * V1 참고 파일: controller/KeywordController.java, service/KeywordService.java
 *
 * V1과의 차이점:
 * - V1: KeywordController → KeywordService (SecurityContextHolder 직접 참조)
 * - V2: KeywordController → KeywordOrchestrator → KeywordCommandService / KeywordQueryService
 *   컨트롤러에서 @AuthUser로 Member를 직접 주입받아 Orchestrator에 전달,
 *   Service는 Member 객체만 수신하여 인증 로직에 무관하게 동작
 *
 * 구현된 V1 메서드 대응:
 * - subscribeToKeyword         ← KeywordService.subscribeToKeyword()
 * - unsubscribeFromKeyword     ← KeywordService.unsubscribeFromKeyword()
 * - getUserKeywords            ← KeywordService.getUserKeyword()
 * - resetAllKeywordSubscriptions ← KeywordService.resetAllSubscriptions()
 */
@Component
@RequiredArgsConstructor
public class KeywordOrchestrator {

    private final KeywordCommandService keywordCommandService;
    private final KeywordQueryService keywordQueryService;

    public void subscribeToKeyword(KeywordSubscribeRequest request, Member member) {
        keywordCommandService.subscribeToKeyword(new KeywordSubscribeCommand(
            member,
            request.koreanKeyword(),
            request.topicName()
        ));
    }

    public void unsubscribeFromKeyword(KeywordUnsubscribeRequest request, Member member) {
        keywordCommandService.unsubscribeFromKeyword(new KeywordUnsubscribeCommand(
            member,
            request.encodedKeyword()
        ));
    }

    public List<KeywordResponse> getUserKeywords(Member member) {
        return keywordQueryService.getUserKeywords(member).stream()
            .map(KeywordResponse::from)
            .toList();
    }

    public void resetAllKeywordSubscriptions(Member member) {
        keywordCommandService.resetAllKeywordSubscriptions(member);
    }
}
