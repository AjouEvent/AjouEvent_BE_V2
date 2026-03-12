package com.example.ajouevent_be_v2.repository.adapter.topic;

import com.example.ajouevent_be_v2.domain.topic.Topic;
import com.example.ajouevent_be_v2.domain.topic.TopicToken;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TopicTokenJpaRepositoryAdapter extends JpaRepository<TopicToken, Long> {

    @Modifying
    @Query("DELETE FROM TopicToken tt WHERE tt.topic = :topic AND tt.tokenValue IN :tokenValues")
    void deleteByTopicAndTokenValues(@Param("topic") Topic topic, @Param("tokenValues") List<String> tokenValues);

    @Modifying
    @Query("DELETE FROM TopicToken tt WHERE tt.tokenValue IN :tokenValues")
    void deleteAllByTokenValues(@Param("tokenValues") List<String> tokenValues);
}
