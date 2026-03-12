package com.example.ajouevent_be_v2.orchestrator;

import com.example.ajouevent_be_v2.domain.keyword.Keyword;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.member.Token;
import com.example.ajouevent_be_v2.domain.topic.Topic;
import com.example.ajouevent_be_v2.dto.token.FcmTokenRequest;
import com.example.ajouevent_be_v2.service.keyword.KeywordQueryService;
import com.example.ajouevent_be_v2.service.token.FcmTokenCommandService;
import com.example.ajouevent_be_v2.service.token.TokenService;
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
 *         → TokenService (TopicToken/KeywordToken 매핑)
 */
@Component
@RequiredArgsConstructor
public class FcmTokenOrchestrator {

    private final FcmTokenCommandService fcmTokenCommandService;
    private final TopicQueryService topicQueryService;
    private final KeywordQueryService keywordQueryService;
    private final TokenService tokenService;

    @Transactional
    public void saveToken(FcmTokenRequest request, Member member) {
        Optional<Token> newToken = fcmTokenCommandService.registerToken(member, request.fcmToken());

        // 신규 토큰인 경우에만 기존 구독에 매핑
        if (newToken.isPresent()) {
            String tokenValue = newToken.get().getTokenValue();

            List<Topic> subscribedTopics = topicQueryService.getSubscribedTopics(member).stream()
                .map(topicMember -> topicMember.getTopic())
                .toList();

            List<Keyword> subscribedKeywords = keywordQueryService.getSubscribedKeywords(member).stream()
                .map(keywordMember -> keywordMember.getKeyword())
                .toList();

            tokenService.linkNewTokenToTopics(tokenValue, subscribedTopics);
            tokenService.linkNewTokenToKeywords(tokenValue, subscribedKeywords);
        }
    }
}
