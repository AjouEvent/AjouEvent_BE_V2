package com.example.ajouevent_be_v2.repository.adapter.notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.notification.NotificationType;
import com.example.ajouevent_be_v2.domain.notification.PushNotification;
import com.example.ajouevent_be_v2.dto.push.UnreadNotificationCountResult;

public interface PushNotificationJpaRepositoryAdapter extends JpaRepository<PushNotification, Long> {

    @Query("SELECT new com.example.ajouevent_be_v2.dto.push.UnreadNotificationCountResult(" +
        "tm.member.id, CAST(COALESCE(COUNT(pn), 0) AS long)) " +
        "FROM TopicMember tm " +
        "LEFT JOIN PushNotification pn ON pn.member.id = tm.member.id AND pn.isRead = false " +
        "WHERE tm.topic.koreanTopic = :koreanTopic " +
        "GROUP BY tm.member.id")
    List<UnreadNotificationCountResult> countUnreadNotificationsForTopic(@Param("koreanTopic") String koreanTopic);

    Slice<PushNotification> findByMemberAndNotificationType(
        Member member, NotificationType notificationType, Pageable pageable);

    @Query("SELECT p FROM PushNotification p LEFT JOIN FETCH p.topic WHERE p.member = :member AND p.notificationType = 'TOPIC' ORDER BY p.notifiedAt DESC")
    Slice<PushNotification> findTopicNotificationsByMemberWithTopic(@Param("member") Member member, Pageable pageable);

    @Query("SELECT p FROM PushNotification p LEFT JOIN FETCH p.topic LEFT JOIN FETCH p.keyword WHERE p.member = :member AND p.notificationType = 'KEYWORD' ORDER BY p.notifiedAt DESC")
    Slice<PushNotification> findKeywordNotificationsByMemberWithTopicAndKeyword(@Param("member") Member member, Pageable pageable);

    Optional<PushNotification> findByMemberAndId(Member member, Long id);

    long countByMemberAndIsReadFalse(Member member);

    @Query("SELECT new com.example.ajouevent_be_v2.dto.push.UnreadNotificationCountResult(" +
        "km.member.id, CAST(COALESCE(COUNT(pn), 0) AS long)) " +
        "FROM KeywordMember km " +
        "LEFT JOIN PushNotification pn ON pn.member.id = km.member.id AND pn.isRead = false " +
        "WHERE km.keyword.encodedKeyword = :encodedKeyword " +
        "GROUP BY km.member.id")
    List<UnreadNotificationCountResult> countUnreadNotificationsForKeyword(@Param("encodedKeyword") String encodedKeyword);

    Optional<PushNotification> findFirstByPushClusterId(Long pushClusterId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PushNotification p SET p.isRead = true, p.clickedAt = :now WHERE p.id IN :ids")
    void updateReadStatusByIds(@Param("ids") List<Long> ids, @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PushNotification p SET p.isRead = true, p.clickedAt = :now WHERE p.id IN :ids AND p.isRead = false")
    void updateReadStatusByIdsWhereUnread(@Param("ids") List<Long> ids, @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PushNotification p SET p.isRead = true, p.clickedAt = :now WHERE p.member = :member AND p.isRead = false")
    void updateAllUnreadByMember(@Param("member") Member member, @Param("now") LocalDateTime now);
}
