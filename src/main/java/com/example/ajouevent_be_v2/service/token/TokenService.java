package com.example.ajouevent_be_v2.service.token;

import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.member.Token;
import com.example.ajouevent_be_v2.domain.topic.Topic;
import com.example.ajouevent_be_v2.domain.topic.TopicToken;
import com.example.ajouevent_be_v2.repository.adapter.subscription.TopicTokenBulkAdapter;
import com.example.ajouevent_be_v2.repository.port.subscription.TopicTokenRepositoryPort;
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

    @Transactional
    public void subscribeToTopic(Topic topic, Member member) {
        List<TopicToken> topicTokens = tokenRepositoryPort.findByMember(member).stream()
            .filter(t -> !t.isDeleted())
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
}
