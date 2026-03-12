package com.example.ajouevent_be_v2.service.keyword;

import com.example.ajouevent_be_v2.common.exception.keyword.KeywordErrorCode;
import com.example.ajouevent_be_v2.common.exception.keyword.KeywordException;
import com.example.ajouevent_be_v2.domain.keyword.Keyword;
import com.example.ajouevent_be_v2.domain.keyword.KeywordMember;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.topic.Topic;
import com.example.ajouevent_be_v2.repository.port.keyword.KeywordMemberRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.keyword.KeywordRepositoryPort;
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

    private static final int MAX_KEYWORD_SUBSCRIPTION_COUNT = 10;

    private final KeywordRepositoryPort keywordRepositoryPort;
    private final KeywordMemberRepositoryPort keywordMemberRepositoryPort;

    // TODO: 동시 요청 시 동일 Keyword 중복 생성 가능성 존재 — 추후 Redis 분산 락 도입 예정
    @Transactional
    public Keyword findOrCreate(Topic topic, String koreanKeyword, String topicName) {
        String formattedKeyword = Keyword.buildFormattedKeyword(koreanKeyword, topicName);
        return keywordRepositoryPort.findByEncodedKeyword(formattedKeyword)
            .orElseGet(() -> keywordRepositoryPort.save(Keyword.create(topic, koreanKeyword, topicName)));
    }

    public Keyword findByEncodedKeyword(String encodedKeyword) {
        return keywordRepositoryPort.findByEncodedKeyword(encodedKeyword)
            .orElseThrow(() -> new KeywordException(KeywordErrorCode.KEYWORD_NOT_FOUND));
    }

    @Transactional
    public void subscribeKeywordMember(Keyword keyword, Member member) {
        validateNotAlreadySubscribed(keyword, member);
        validateSubscriptionLimit(member);
        keywordMemberRepositoryPort.save(KeywordMember.create(keyword, member));
        log.info("키워드 구독 - member: {}, keyword: {}", member.getEmail(), keyword.getKoreanKeyword());
    }

    @Transactional
    public void unsubscribeKeywordMember(Keyword keyword, Member member) {
        keywordMemberRepositoryPort.deleteByKeywordAndMember(keyword, member);
        log.info("키워드 구독 취소 - member: {}, keyword: {}", member.getEmail(), keyword.getKoreanKeyword());
    }

    @Transactional
    public void deleteAllKeywordMembers(Member member) {
        List<Long> keywordMemberIds = keywordMemberRepositoryPort.findByMemberWithKeyword(member).stream()
            .map(KeywordMember::getId)
            .toList();

        if (!keywordMemberIds.isEmpty()) {
            keywordMemberRepositoryPort.deleteAllByIds(keywordMemberIds);
        }

        log.info("키워드 구독 전체 초기화 - member: {}", member.getEmail());
    }

    private void validateNotAlreadySubscribed(Keyword keyword, Member member) {
        if (keywordMemberRepositoryPort.existsByKeywordAndMember(keyword, member)) {
            throw new KeywordException(KeywordErrorCode.ALREADY_SUBSCRIBED);
        }
    }

    private void validateSubscriptionLimit(Member member) {
        if (keywordMemberRepositoryPort.countByMember(member) >= MAX_KEYWORD_SUBSCRIPTION_COUNT) {
            throw new KeywordException(KeywordErrorCode.MAX_SUBSCRIPTION_LIMIT_EXCEEDED);
        }
    }
}
