package com.example.ajouevent_be_v2.repository.port.subscription;

import com.example.ajouevent_be_v2.domain.topic.Topic;
import com.example.ajouevent_be_v2.repository.adapter.subscription.TopicJpaRepositoryAdapter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TopicRepositoryPort {

    private final TopicJpaRepositoryAdapter topicJpaRepositoryAdapter;

    public Optional<Topic> findByDepartment(String department) {
        return topicJpaRepositoryAdapter.findByDepartment(department);
    }

    public List<Topic> findAll() {
        return topicJpaRepositoryAdapter.findAll();
    }
}
