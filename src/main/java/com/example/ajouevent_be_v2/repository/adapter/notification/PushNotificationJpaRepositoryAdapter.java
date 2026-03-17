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

public interface PushNotificationJpaRepositoryAdapter extends JpaRepository<PushNotification, Long> {

    Slice<PushNotification> findByMemberAndNotificationType(
        Member member, NotificationType notificationType, Pageable pageable);

    Optional<PushNotification> findByMemberAndId(Member member, Long id);

    long countByMemberAndIsReadFalse(Member member);

    List<PushNotification> findByMemberAndIsReadFalse(Member member);

    @Modifying
    @Query("UPDATE PushNotification p SET p.isRead = true, p.clickedAt = :now WHERE p.id IN :ids")
    void updateReadStatusByIds(@Param("ids") List<Long> ids, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE PushNotification p SET p.isRead = true, p.clickedAt = :now WHERE p.id IN :ids AND p.isRead = false")
    void updateReadStatusByIdsWhereUnread(@Param("ids") List<Long> ids, @Param("now") LocalDateTime now);
}
