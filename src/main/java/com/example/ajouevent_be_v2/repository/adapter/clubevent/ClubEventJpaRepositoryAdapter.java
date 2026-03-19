package com.example.ajouevent_be_v2.repository.adapter.clubevent;

import com.example.ajouevent_be_v2.domain.clubevent.ClubEvent;
import com.example.ajouevent_be_v2.domain.clubevent.Type;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClubEventJpaRepositoryAdapter extends JpaRepository<ClubEvent, Long> {

    Slice<ClubEvent> findByTypeAndTitleContaining(Type type, String keyword, Pageable pageable);

    Slice<ClubEvent> findByTypeIn(List<Type> types, Pageable pageable);

    Slice<ClubEvent> findByTypeInAndTitleContaining(List<Type> types, String keyword, Pageable pageable);

    @Query("SELECT ce FROM ClubEvent ce WHERE ce.eventId IN :eventIds ORDER BY ce.createdAt DESC")
    Slice<ClubEvent> findByEventIds(@Param("eventIds") List<Long> eventIds, Pageable pageable);

    Slice<ClubEvent> findByEventIdInAndType(List<Long> eventId, Type type, Pageable pageable);

    Slice<ClubEvent> findByEventIdInAndTitleContaining(List<Long> eventId, String title, Pageable pageable);

    Slice<ClubEvent> findByEventIdInAndTypeAndTitleContaining(
        List<Long> eventId, Type type, String title, Pageable pageable);

    List<ClubEvent> findTop10ByCreatedAtBetweenOrderByViewCountDesc(
        LocalDateTime startOfWeek, LocalDateTime endOfWeek);

    List<ClubEvent> findTop10ByTypeOrderByCreatedAtDesc(Type type);

    @Modifying
    @Query("UPDATE ClubEvent e SET e.viewCount = :viewCount WHERE e.eventId = :eventId")
    void updateViews(@Param("viewCount") Long viewCount, @Param("eventId") Long eventId);
}
