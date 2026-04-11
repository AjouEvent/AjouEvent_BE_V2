package com.example.ajouevent_be_v2.repository.adapter.clubevent;

import com.example.ajouevent_be_v2.domain.clubevent.ClubEvent;
import com.example.ajouevent_be_v2.domain.clubevent.Type;
import com.example.ajouevent_be_v2.domain.keyword.Keyword;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventSummaryResult;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClubEventJpaRepositoryAdapter extends JpaRepository<ClubEvent, Long> {

    // ── 공통 SELECT 절 (content 제외, contentPreview 포함) ──────────────────
    // SELECT new com.example.ajouevent_be_v2.dto.clubevent.ClubEventSummaryResult(
    //     ce.eventId, ce.title, ce.contentPreview, ce.writer, ce.createdAt,
    //     ce.likesCount, ce.viewCount, ce.subject, ce.type, ce.url)

    @Query(
        "SELECT new com.example.ajouevent_be_v2.dto.clubevent.ClubEventSummaryResult("
            + "ce.eventId, ce.title, ce.contentPreview, ce.writer, ce.createdAt,"
            + "ce.likesCount, ce.viewCount, ce.subject, ce.type, ce.url) "
            + "FROM ClubEvent ce "
            + "WHERE ce.type = :type AND ce.title LIKE CONCAT('%', :keyword, '%')")
    Slice<ClubEventSummaryResult> findByTypeAndTitleContaining(
        @Param("type") Type type, @Param("keyword") String keyword, Pageable pageable);

    @Query(
        "SELECT new com.example.ajouevent_be_v2.dto.clubevent.ClubEventSummaryResult("
            + "ce.eventId, ce.title, ce.contentPreview, ce.writer, ce.createdAt,"
            + "ce.likesCount, ce.viewCount, ce.subject, ce.type, ce.url) "
            + "FROM ClubEvent ce "
            + "WHERE ce.type IN :types")
    Slice<ClubEventSummaryResult> findByTypeIn(@Param("types") List<Type> types, Pageable pageable);

    @Query(
        "SELECT new com.example.ajouevent_be_v2.dto.clubevent.ClubEventSummaryResult("
            + "ce.eventId, ce.title, ce.contentPreview, ce.writer, ce.createdAt,"
            + "ce.likesCount, ce.viewCount, ce.subject, ce.type, ce.url) "
            + "FROM ClubEvent ce "
            + "WHERE ce.type IN :types AND ce.title LIKE CONCAT('%', :keyword, '%')")
    Slice<ClubEventSummaryResult> findByTypeInAndTitleContaining(
        @Param("types") List<Type> types, @Param("keyword") String keyword, Pageable pageable);

    @Query(
        "SELECT new com.example.ajouevent_be_v2.dto.clubevent.ClubEventSummaryResult("
            + "ce.eventId, ce.title, ce.contentPreview, ce.writer, ce.createdAt,"
            + "ce.likesCount, ce.viewCount, ce.subject, ce.type, ce.url) "
            + "FROM ClubEvent ce "
            + "WHERE ce.eventId IN :eventIds ORDER BY ce.createdAt DESC")
    Slice<ClubEventSummaryResult> findByEventIds(@Param("eventIds") List<Long> eventIds, Pageable pageable);

    @Query(
        "SELECT new com.example.ajouevent_be_v2.dto.clubevent.ClubEventSummaryResult("
            + "ce.eventId, ce.title, ce.contentPreview, ce.writer, ce.createdAt,"
            + "ce.likesCount, ce.viewCount, ce.subject, ce.type, ce.url) "
            + "FROM ClubEvent ce "
            + "WHERE ce.eventId IN :eventIds AND ce.type = :type")
    Slice<ClubEventSummaryResult> findByEventIdInAndType(
        @Param("eventIds") List<Long> eventIds, @Param("type") Type type, Pageable pageable);

    @Query(
        "SELECT new com.example.ajouevent_be_v2.dto.clubevent.ClubEventSummaryResult("
            + "ce.eventId, ce.title, ce.contentPreview, ce.writer, ce.createdAt,"
            + "ce.likesCount, ce.viewCount, ce.subject, ce.type, ce.url) "
            + "FROM ClubEvent ce "
            + "WHERE ce.eventId IN :eventIds AND ce.title LIKE CONCAT('%', :title, '%')")
    Slice<ClubEventSummaryResult> findByEventIdInAndTitleContaining(
        @Param("eventIds") List<Long> eventIds, @Param("title") String title, Pageable pageable);

    @Query(
        "SELECT new com.example.ajouevent_be_v2.dto.clubevent.ClubEventSummaryResult("
            + "ce.eventId, ce.title, ce.contentPreview, ce.writer, ce.createdAt,"
            + "ce.likesCount, ce.viewCount, ce.subject, ce.type, ce.url) "
            + "FROM ClubEvent ce "
            + "WHERE ce.eventId IN :eventIds AND ce.type = :type "
            + "AND ce.title LIKE CONCAT('%', :title, '%')")
    Slice<ClubEventSummaryResult> findByEventIdInAndTypeAndTitleContaining(
        @Param("eventIds") List<Long> eventIds,
        @Param("type") Type type,
        @Param("title") String title,
        Pageable pageable);

    // Top10 — Pageable(size=10) 로 제한, ORDER BY 는 쿼리에 명시
    @Query(
        "SELECT new com.example.ajouevent_be_v2.dto.clubevent.ClubEventSummaryResult("
            + "ce.eventId, ce.title, ce.contentPreview, ce.writer, ce.createdAt,"
            + "ce.likesCount, ce.viewCount, ce.subject, ce.type, ce.url) "
            + "FROM ClubEvent ce "
            + "WHERE ce.createdAt BETWEEN :start AND :end "
            + "ORDER BY ce.viewCount DESC")
    List<ClubEventSummaryResult> findTop10ByCreatedAtBetween(
        @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    @Query(
        "SELECT new com.example.ajouevent_be_v2.dto.clubevent.ClubEventSummaryResult("
            + "ce.eventId, ce.title, ce.contentPreview, ce.writer, ce.createdAt,"
            + "ce.likesCount, ce.viewCount, ce.subject, ce.type, ce.url) "
            + "FROM ClubEvent ce "
            + "WHERE ce.type = :type "
            + "ORDER BY ce.createdAt DESC")
    List<ClubEventSummaryResult> findTop10ByType(@Param("type") Type type, Pageable pageable);

    @Query(
        "SELECT new com.example.ajouevent_be_v2.dto.clubevent.ClubEventSummaryResult("
            + "ce.eventId, ce.title, ce.contentPreview, ce.writer, ce.createdAt,"
            + "ce.likesCount, ce.viewCount, ce.subject, ce.type, ce.url) "
            + "FROM ClubEvent ce "
            + "WHERE ce.type = :type "
            + "AND EXISTS ("
            + "SELECT 1 FROM Keyword k "
            + "WHERE k IN :keywords "
            + "AND LOWER(ce.title) LIKE CONCAT('%', LOWER(k.koreanKeyword), '%')) "
            + "ORDER BY ce.createdAt DESC")
    Slice<ClubEventSummaryResult> findByTypeAndAnyKeyword(
        @Param("type") Type type, @Param("keywords") List<Keyword> keywords, Pageable pageable);

    @Modifying
    @Query("UPDATE ClubEvent e SET e.viewCount = :viewCount WHERE e.eventId = :eventId")
    void updateViews(@Param("viewCount") Long viewCount, @Param("eventId") Long eventId);
}
