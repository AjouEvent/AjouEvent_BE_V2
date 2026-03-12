package com.example.ajouevent_be_v2.orchestrator;

import com.example.ajouevent_be_v2.domain.keyword.Keyword;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.topic.Topic;
import com.example.ajouevent_be_v2.dto.keyword.KeywordResponse;
import com.example.ajouevent_be_v2.dto.keyword.KeywordSubscribeRequest;
import com.example.ajouevent_be_v2.dto.keyword.KeywordUnsubscribeRequest;
import com.example.ajouevent_be_v2.service.keyword.KeywordCommandService;
import com.example.ajouevent_be_v2.service.keyword.KeywordQueryService;
import com.example.ajouevent_be_v2.service.token.TokenService;
import com.example.ajouevent_be_v2.service.topic.TopicQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * V1 참고 파일: controller/KeywordController.java, service/KeywordService.java
 *
 * V1과의 차이점:
 * - V1: KeywordController → KeywordService (SecurityContextHolder 직접 참조)
 * - V2: KeywordController → KeywordOrchestrator → KeywordCommandService / KeywordQueryService / TokenService
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
    private final TokenService tokenService;
    private final TopicQueryService topicQueryService;

    @Transactional
    public void subscribeToKeyword(KeywordSubscribeRequest request, Member member) {
        Topic topic = topicQueryService.findByDepartment(request.topicName());
        Keyword keyword = keywordCommandService.findOrCreate(topic, request.koreanKeyword(), request.topicName());
        keywordCommandService.subscribeKeywordMember(keyword, member);
        tokenService.subscribeToKeyword(keyword, member);
    }

    @Transactional
    public void unsubscribeFromKeyword(KeywordUnsubscribeRequest request, Member member) {
        Keyword keyword = keywordCommandService.findByEncodedKeyword(request.encodedKeyword());
        tokenService.unsubscribeFromKeyword(keyword, member);
        keywordCommandService.unsubscribeKeywordMember(keyword, member);
    }

    public List<KeywordResponse> getUserKeywords(Member member) {
        return keywordQueryService.getUserKeywords(member).stream()
            .map(KeywordResponse::from)
            .toList();
    }

    @Transactional
    public void resetAllKeywordSubscriptions(Member member) {
        tokenService.deleteAllKeywordTokens(member);
        keywordCommandService.deleteAllKeywordMembers(member);
    }
}
