package com.example.ajouevent_be_v2.repository.port.keyword;

import com.example.ajouevent_be_v2.domain.keyword.Keyword;
import com.example.ajouevent_be_v2.domain.topic.Topic;
import com.example.ajouevent_be_v2.repository.adapter.keyword.KeywordJpaRepositoryAdapter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class KeywordRepositoryPort {

    private final KeywordJpaRepositoryAdapter keywordJpaRepositoryAdapter;

    public Keyword save(Keyword keyword) {
        return keywordJpaRepositoryAdapter.save(keyword);
    }

    public Optional<Keyword> findByEncodedKeyword(String encodedKeyword) {
        return keywordJpaRepositoryAdapter.findByEncodedKeyword(encodedKeyword);
    }

    public Optional<Keyword> findBySearchKeyword(String searchKeyword) {
        return keywordJpaRepositoryAdapter.findBySearchKeyword(searchKeyword);
    }

    public List<Keyword> findByTopic(Topic topic) {
        return keywordJpaRepositoryAdapter.findByTopic(topic);
    }
}
