package com.example.ajouevent_be_v2.repository.adapter.topic;

import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.topic.Topic;
import com.example.ajouevent_be_v2.domain.topic.TopicMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TopicMemberJpaRepositoryAdapter extends JpaRepository<TopicMember, Long> {

    boolean existsByTopicAndMember(Topic topic, Member member);

    Optional<TopicMember> findByMemberAndTopic(Member member, Topic topic);

    @Query("SELECT tm FROM TopicMember tm JOIN FETCH tm.topic WHERE tm.member = :member")
    List<TopicMember> findByMemberWithTopic(@Param("member") Member member);

    List<TopicMember> findByMember(Member member);

    void deleteByTopicAndMember(Topic topic, Member member);

    boolean existsByMemberAndIsReadFalse(Member member);

    @Query("SELECT tm FROM TopicMember tm JOIN FETCH tm.member WHERE tm.topic = :topic AND tm.receiveNotification = true")
    List<TopicMember> findByTopicWithMemberAndReceiveNotificationTrue(@Param("topic") Topic topic);

    @Modifying
    @Query("DELETE FROM TopicMember tm WHERE tm.id IN :ids")
    void deleteAllByIds(@Param("ids") List<Long> ids);
}
