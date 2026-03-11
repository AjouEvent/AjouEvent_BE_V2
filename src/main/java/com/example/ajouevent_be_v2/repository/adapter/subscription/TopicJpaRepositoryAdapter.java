package com.example.ajouevent_be_v2.repository.adapter.subscription;

import com.example.ajouevent_be_v2.domain.topic.Topic;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicJpaRepositoryAdapter extends JpaRepository<Topic, Long> {

    Optional<Topic> findByDepartment(String department);
}
