package com.example.ajouevent_be_v2.service.subscription;

import com.example.ajouevent_be_v2.common.exception.subscription.SubscriptionErrorCode;
import com.example.ajouevent_be_v2.common.exception.subscription.SubscriptionException;
import com.example.ajouevent_be_v2.domain.keyword.Keyword;
import com.example.ajouevent_be_v2.domain.keyword.KeywordMember;
import com.example.ajouevent_be_v2.domain.keyword.KeywordToken;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.member.Token;
import com.example.ajouevent_be_v2.dto.keyword.KeywordSubscribeCommand;
import com.example.ajouevent_be_v2.dto.keyword.KeywordUnsubscribeCommand;
import com.example.ajouevent_be_v2.repository.port.keyword.KeywordMemberRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.keyword.KeywordRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.keyword.KeywordTokenRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.token.TokenRepositoryPort;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// V1 참고: service/KeywordService.java
@Service
@Slf4j
@RequiredArgsConstructor
public class KeywordCommandService {

    private final KeywordRepositoryPort keywordRepositoryPort;
    private final KeywordMemberRepositoryPort keywordMemberRepositoryPort;
    private final KeywordTokenRepositoryPort keywordTokenRepositoryPort;
    private final TokenRepositoryPort tokenRepositoryPort;

    // V1 KeywordService.subscribeToKeyword() 포팅
    // - Topic 조회 및 신규 Keyword 생성 로직은 #26 TopicRepository 구현 완료 후 연동 필요
    // - 현재는 DB에 존재하는 Keyword만 구독 가능
    @Transactional
    public void subscribeToKeyword(KeywordSubscribeCommand command) {
        Member member = command.member();
        String koreanKeyword = command.koreanKeyword();
        String topicName = command.topicName();

        String encodedKeyword = URLEncoder.encode(koreanKeyword, StandardCharsets.UTF_8).replace("+", "%20");
        String formattedKeyword = encodedKeyword + "_" + topicName;

        // TODO(#26): Topic 조회 후 Keyword 없으면 신규 생성 로직 추가
        //   Topic topic = topicRepositoryPort.findByDepartment(topicName)
        //       .orElseThrow(() -> new SubscriptionException(SubscriptionErrorCode.TOPIC_NOT_FOUND));
        //   Keyword keyword = keywordRepositoryPort.findByEncodedKeyword(formattedKeyword)
        //       .orElseGet(() -> keywordRepositoryPort.save(Keyword.builder()
        //           .encodedKeyword(formattedKeyword)
        //           .koreanKeyword(koreanKeyword)
        //           .searchKeyword(koreanKeyword + "_" + topicName)
        //           .topic(topic)
        //           .build()));
        Keyword keyword = keywordRepositoryPort.findByEncodedKeyword(formattedKeyword)
            .orElseThrow(() -> new SubscriptionException(SubscriptionErrorCode.TOPIC_NOT_FOUND));

        if (keywordMemberRepositoryPort.existsByKeywordAndMember(keyword, member)) {
            throw new SubscriptionException(SubscriptionErrorCode.ALREADY_SUBSCRIBED_KEYWORD);
        }

        long subscribedKeywordCount = keywordMemberRepositoryPort.countByMember(member);
        if (subscribedKeywordCount >= 10) {
            throw new SubscriptionException(SubscriptionErrorCode.MAX_KEYWORD_LIMIT_EXCEEDED);
        }

        keywordMemberRepositoryPort.save(KeywordMember.builder()
            .keyword(keyword)
            .member(member)
            .isRead(false)
            .lastReadAt(LocalDateTime.now())
            .build());

        List<Token> memberTokens = tokenRepositoryPort.findByMember(member);
        List<KeywordToken> keywordTokens = memberTokens.stream()
            .map(token -> KeywordToken.builder()
                .keyword(keyword)
                .tokenValue(token.getTokenValue())
                .build())
            .toList();
        keywordTokenRepositoryPort.saveAll(keywordTokens);

        log.info("키워드 구독 - member: {}, keyword: {}", member.getEmail(), keyword.getKoreanKeyword());
    }

    // V1 KeywordService.unsubscribeFromKeyword() 포팅
    @Transactional
    public void unsubscribeFromKeyword(KeywordUnsubscribeCommand command) {
        Member member = command.member();

        Keyword keyword = keywordRepositoryPort.findByEncodedKeyword(command.encodedKeyword())
            .orElseThrow(() -> new SubscriptionException(SubscriptionErrorCode.KEYWORD_NOT_FOUND));

        keywordMemberRepositoryPort.deleteByKeywordAndMember(keyword, member);

        List<Token> memberTokens = tokenRepositoryPort.findByMember(member);
        keywordTokenRepositoryPort.deleteByKeywordAndTokens(keyword, memberTokens);

        log.info("키워드 구독 취소 - member: {}, keyword: {}", member.getEmail(), keyword.getKoreanKeyword());
    }

    // V1 KeywordService.resetAllSubscriptions() 포팅
    @Transactional
    public void resetAllKeywordSubscriptions(Member member) {
        List<KeywordMember> keywordMembers = keywordMemberRepositoryPort.findByMemberWithKeyword(member);
        List<Token> tokens = tokenRepositoryPort.findByMember(member);

        List<Long> tokenIds = tokens.stream().map(Token::getId).toList();
        keywordTokenRepositoryPort.deleteAllByTokenIds(tokenIds);

        List<Long> keywordMemberIds = keywordMembers.stream().map(KeywordMember::getId).toList();
        keywordMemberRepositoryPort.deleteAllByIds(keywordMemberIds);

        log.info("키워드 구독 전체 초기화 - member: {}", member.getEmail());
    }
}
