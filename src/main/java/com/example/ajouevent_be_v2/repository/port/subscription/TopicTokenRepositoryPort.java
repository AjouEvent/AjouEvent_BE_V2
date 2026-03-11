package com.example.ajouevent_be_v2.repository.port.subscription;

import com.example.ajouevent_be_v2.domain.member.Token;
import com.example.ajouevent_be_v2.domain.topic.Topic;
import com.example.ajouevent_be_v2.domain.topic.TopicToken;
import com.example.ajouevent_be_v2.repository.adapter.subscription.TopicTokenJpaRepositoryAdapter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TopicTokenRepositoryPort {

    private final TopicTokenJpaRepositoryAdapter topicTokenJpaRepositoryAdapter;

    public void deleteByTopicAndTokens(Topic topic, List<Token> tokens) {
        List<String> tokenValues = tokens.stream().map(Token::getTokenValue).toList();
        topicTokenJpaRepositoryAdapter.deleteByTopicAndTokenValues(topic, tokenValues);
    }

    public List<TopicToken> findTopicTokensWithTopic(List<Token> tokens) {
        List<String> tokenValues = tokens.stream().map(Token::getTokenValue).toList();
        return topicTokenJpaRepositoryAdapter.findTopicTokensWithTopic(tokenValues);
    }

    public void deleteAllByTokenValues(List<String> tokenValues) {
        topicTokenJpaRepositoryAdapter.deleteAllByTokenValues(tokenValues);
    }
}
