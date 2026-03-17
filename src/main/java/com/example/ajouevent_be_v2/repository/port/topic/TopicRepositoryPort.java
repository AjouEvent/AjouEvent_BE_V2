package com.example.ajouevent_be_v2.repository.port.topic;

import com.example.ajouevent_be_v2.domain.topic.Topic;
import com.example.ajouevent_be_v2.repository.adapter.topic.TopicJpaRepositoryAdapter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TopicRepositoryPort {

    private final TopicJpaRepositoryAdapter topicJpaRepositoryAdapter;

    public Optional<Topic> findByDepartment(String department) {
        return topicJpaRepositoryAdapter.findFirstByDepartment(department);
    }

    public Optional<Topic> findByKoreanTopic(String koreanTopic) {
        return topicJpaRepositoryAdapter.findByKoreanTopic(koreanTopic);
    }

    public List<Topic> findAll() {
        return topicJpaRepositoryAdapter.findAll();
    }
}
