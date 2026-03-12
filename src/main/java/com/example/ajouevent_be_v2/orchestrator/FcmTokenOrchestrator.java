package com.example.ajouevent_be_v2.orchestrator;

import com.example.ajouevent_be_v2.domain.keyword.KeywordToken;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.member.Token;
import com.example.ajouevent_be_v2.domain.topic.TopicToken;
import com.example.ajouevent_be_v2.dto.token.FcmTokenRequest;
import com.example.ajouevent_be_v2.repository.adapter.keyword.KeywordTokenBulkAdapter;
import com.example.ajouevent_be_v2.repository.adapter.topic.TopicTokenBulkAdapter;
import com.example.ajouevent_be_v2.service.keyword.KeywordQueryService;
import com.example.ajouevent_be_v2.service.token.FcmTokenCommandService;
import com.example.ajouevent_be_v2.service.topic.TopicQueryService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * V1 참고 파일: service/TopicService.java — saveFCMToken() 포팅
 *
 * V1과의 차이점:
 * - V1: FCMController → TopicService.saveFCMToken() (단일 서비스에서 Token + TopicToken + KeywordToken 처리)
 * - V2: FcmTokenController → FcmTokenOrchestrator
 *         → FcmTokenCommandService (Token 저장/갱신)
 *         → TopicQueryService + KeywordQueryService (구독 조회)
 *         → TopicTokenBulkAdapter + KeywordTokenBulkAdapter (매핑 저장)
 */
@Component
@RequiredArgsConstructor
public class FcmTokenOrchestrator {

    private final FcmTokenCommandService fcmTokenCommandService;
    private final TopicQueryService topicQueryService;
    private final KeywordQueryService keywordQueryService;
    private final TopicTokenBulkAdapter topicTokenBulkAdapter;
    private final KeywordTokenBulkAdapter keywordTokenBulkAdapter;

    @Transactional
    public void saveToken(FcmTokenRequest request, Member member) {
        Optional<Token> newToken = fcmTokenCommandService.registerToken(member, request.fcmToken());

        // 신규 토큰인 경우에만 기존 구독에 매핑
        if (newToken.isPresent()) {
            String tokenValue = newToken.get().getTokenValue();
            linkToSubscribedTopics(member, tokenValue);
            linkToSubscribedKeywords(member, tokenValue);
        }
    }

    private void linkToSubscribedTopics(Member member, String tokenValue) {
        List<TopicToken> topicTokens = topicQueryService.getSubscribedTopics(member).stream()
            .map(topicMember -> TopicToken.builder()
                .topic(topicMember.getTopic())
                .tokenValue(tokenValue)
                .build())
            .toList();

        if (!topicTokens.isEmpty()) {
            topicTokenBulkAdapter.saveAll(topicTokens);
        }
    }

    private void linkToSubscribedKeywords(Member member, String tokenValue) {
        List<KeywordToken> keywordTokens = keywordQueryService.getSubscribedKeywords(member).stream()
            .map(keywordMember -> KeywordToken.create(keywordMember.getKeyword(), tokenValue))
            .toList();

        if (!keywordTokens.isEmpty()) {
            keywordTokenBulkAdapter.saveAll(keywordTokens);
        }
    }
}
