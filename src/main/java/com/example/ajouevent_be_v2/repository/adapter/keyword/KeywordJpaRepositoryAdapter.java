package com.example.ajouevent_be_v2.repository.adapter.keyword;

import com.example.ajouevent_be_v2.domain.keyword.Keyword;
import com.example.ajouevent_be_v2.domain.topic.Topic;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordJpaRepositoryAdapter extends JpaRepository<Keyword, Long> {

    Optional<Keyword> findByEncodedKeyword(String encodedKeyword);

    Optional<Keyword> findBySearchKeyword(String searchKeyword);

    List<Keyword> findByTopic(Topic topic);
}
