package com.example.ajouevent_be_v2.repository.adapter.notification;

import java.util.List;

import com.example.ajouevent_be_v2.domain.notification.PushNotification;
import com.example.ajouevent_be_v2.dto.push.UnreadNotificationCountResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PushNotificationJpaRepositoryAdapter extends JpaRepository<PushNotification, Long> {

    @Query("SELECT new com.example.ajouevent_be_v2.dto.push.UnreadNotificationCountResult(" +
        "tm.member.id, CAST(COALESCE(COUNT(pn), 0) AS long)) " +
        "FROM TopicMember tm " +
        "LEFT JOIN PushNotification pn ON pn.member.id = tm.member.id AND pn.isRead = false " +
        "WHERE tm.topic.koreanTopic = :koreanTopic " +
        "GROUP BY tm.member.id")
    List<UnreadNotificationCountResult> countUnreadNotificationsForTopic(@Param("koreanTopic") String koreanTopic);

    @Query("SELECT new com.example.ajouevent_be_v2.dto.push.UnreadNotificationCountResult(" +
        "km.member.id, CAST(COALESCE(COUNT(pn), 0) AS long)) " +
        "FROM KeywordMember km " +
        "LEFT JOIN PushNotification pn ON pn.member.id = km.member.id AND pn.isRead = false " +
        "WHERE km.keyword.encodedKeyword = :encodedKeyword " +
        "GROUP BY km.member.id")
    List<UnreadNotificationCountResult> countUnreadNotificationsForKeyword(@Param("encodedKeyword") String encodedKeyword);
}
