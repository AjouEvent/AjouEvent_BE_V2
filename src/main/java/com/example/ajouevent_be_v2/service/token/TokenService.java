package com.example.ajouevent_be_v2.service.token;

import com.example.ajouevent_be_v2.domain.keyword.Keyword;
import com.example.ajouevent_be_v2.domain.keyword.KeywordToken;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.member.Token;
import com.example.ajouevent_be_v2.domain.topic.Topic;
import com.example.ajouevent_be_v2.domain.topic.TopicToken;
import com.example.ajouevent_be_v2.repository.adapter.keyword.KeywordTokenBulkAdapter;
import com.example.ajouevent_be_v2.repository.adapter.topic.TopicTokenBulkAdapter;
import com.example.ajouevent_be_v2.repository.port.keyword.KeywordTokenRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.topic.TopicTokenRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.token.TokenRepositoryPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepositoryPort tokenRepositoryPort;
    private final TopicTokenBulkAdapter topicTokenBulkAdapter;
    private final TopicTokenRepositoryPort topicTokenRepositoryPort;
    private final KeywordTokenBulkAdapter keywordTokenBulkAdapter;
    private final KeywordTokenRepositoryPort keywordTokenRepositoryPort;

    @Transactional
    public void subscribeToTopic(Topic topic, Member member) {
        List<TopicToken> topicTokens = tokenRepositoryPort.findActiveTokensByMember(member).stream()
            .map(token -> TopicToken.builder()
                .topic(topic)
                .tokenValue(token.getTokenValue())
                .build())
            .toList();

        if (!topicTokens.isEmpty()) {
            topicTokenBulkAdapter.saveAll(topicTokens);
        }
    }

    @Transactional
    public void unsubscribeFromTopic(Topic topic, Member member) {
        List<Token> memberTokens = tokenRepositoryPort.findByMember(member);
        topicTokenRepositoryPort.deleteByTopicAndTokens(topic, memberTokens);
    }

    @Transactional
    public void deleteAllTopicTokens(Member member) {
        List<String> tokenValues = tokenRepositoryPort.findByMember(member).stream()
            .map(Token::getTokenValue)
            .toList();

        if (!tokenValues.isEmpty()) {
            topicTokenRepositoryPort.deleteAllByTokenValues(tokenValues);
        }
    }

    @Transactional
    public void subscribeToKeyword(Keyword keyword, Member member) {
        List<KeywordToken> keywordTokens = tokenRepositoryPort.findActiveTokensByMember(member).stream()
            .map(token -> KeywordToken.create(keyword, token.getTokenValue()))
            .toList();

        if (!keywordTokens.isEmpty()) {
            keywordTokenBulkAdapter.saveAll(keywordTokens);
        }
    }

    @Transactional
    public void unsubscribeFromKeyword(Keyword keyword, Member member) {
        List<Token> memberTokens = tokenRepositoryPort.findActiveTokensByMember(member);
        keywordTokenRepositoryPort.deleteByKeywordAndTokens(keyword, memberTokens);
    }

    @Transactional
    public void deleteAllKeywordTokens(Member member) {
        List<String> tokenValues = tokenRepositoryPort.findByMember(member).stream()
            .map(Token::getTokenValue)
            .toList();

        if (!tokenValues.isEmpty()) {
            keywordTokenRepositoryPort.deleteAllByTokenValues(tokenValues);
        }
    }

    @Transactional
    public void linkNewTokenToTopics(String tokenValue, List<Topic> topics) {
        List<TopicToken> topicTokens = topics.stream()
            .map(topic -> TopicToken.builder()
                .topic(topic)
                .tokenValue(tokenValue)
                .build())
            .toList();

        if (!topicTokens.isEmpty()) {
            topicTokenBulkAdapter.saveAll(topicTokens);
        }
    }

    @Transactional
    public void linkNewTokenToKeywords(String tokenValue, List<Keyword> keywords) {
        List<KeywordToken> keywordTokens = keywords.stream()
            .map(keyword -> KeywordToken.create(keyword, tokenValue))
            .toList();

        if (!keywordTokens.isEmpty()) {
            keywordTokenBulkAdapter.saveAll(keywordTokens);
        }
    }
}
