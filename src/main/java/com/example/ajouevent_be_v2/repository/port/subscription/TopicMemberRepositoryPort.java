package com.example.ajouevent_be_v2.repository.port.subscription;

import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.topic.Topic;
import com.example.ajouevent_be_v2.domain.topic.TopicMember;
import com.example.ajouevent_be_v2.repository.adapter.subscription.TopicMemberJpaRepositoryAdapter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TopicMemberRepositoryPort {

    private final TopicMemberJpaRepositoryAdapter topicMemberJpaRepositoryAdapter;

    public boolean existsByTopicAndMember(Topic topic, Member member) {
        return topicMemberJpaRepositoryAdapter.existsByTopicAndMember(topic, member);
    }

    public Optional<TopicMember> findByMemberAndTopic(Member member, Topic topic) {
        return topicMemberJpaRepositoryAdapter.findByMemberAndTopic(member, topic);
    }

    public List<TopicMember> findByMemberWithTopic(Member member) {
        return topicMemberJpaRepositoryAdapter.findByMemberWithTopic(member);
    }

    public List<TopicMember> findByMember(Member member) {
        return topicMemberJpaRepositoryAdapter.findByMember(member);
    }

    public void deleteByTopicAndMember(Topic topic, Member member) {
        topicMemberJpaRepositoryAdapter.deleteByTopicAndMember(topic, member);
    }

    public void deleteAllByIds(List<Long> ids) {
        topicMemberJpaRepositoryAdapter.deleteAllByIds(ids);
    }

    public boolean existsByMemberAndIsReadFalse(Member member) {
        return topicMemberJpaRepositoryAdapter.existsByMemberAndIsReadFalse(member);
    }

    public TopicMember save(TopicMember topicMember) {
        return topicMemberJpaRepositoryAdapter.save(topicMember);
    }
}
