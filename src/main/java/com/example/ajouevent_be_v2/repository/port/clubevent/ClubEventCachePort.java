package com.example.ajouevent_be_v2.repository.port.clubevent;

import com.example.ajouevent_be_v2.common.dto.SliceResult;
import com.example.ajouevent_be_v2.domain.clubevent.EventBanner;
import com.example.ajouevent_be_v2.domain.clubevent.Type;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventSummaryResult;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Pageable;

public interface ClubEventCachePort {

    Optional<SliceResult<ClubEventSummaryResult>> getTypeEvents(Type type, String keyword, Pageable pageable);

    void saveTypeEvents(Type type, String keyword, Pageable pageable, SliceResult<ClubEventSummaryResult> events);

    void evictTypeEvents(Type type);

    Optional<List<ClubEventSummaryResult>> getPopularEvents();

    void savePopularEvents(List<ClubEventSummaryResult> events);

    void evictPopularEvents();

    Optional<List<EventBanner>> getBanners();

    void saveBanners(List<EventBanner> banners);

    // 조회수 증가 — dedup + INCR + SADD dirty (Lua Script, 원자적)
    void incrementViewForUser(String userEmail, Long eventId);

    void incrementViewForAnonymous(String ip, String userAgent, Long eventId);

    // 스케줄러: 더티 셋에서 DB 반영 대상 eventId 조회
    Set<Long> getDirtyEventIds();

    // 스케줄러: SCAN으로 더티 셋 미등록 누락 키 탐지
    Set<Long> scanViewCountIds();

    // 스케줄러: eventId 별 누적 조회수 delta 조회
    Long getViewCountDelta(Long eventId);

    // 스케줄러: 청크 단위 DECRBY + 조건부 정리 (Lua Script, 원자적)
    void subtractViewCountChunk(Map<Long, Long> deltaMap);

    // 스케줄러: idempotent 보장용 committed delta 관리
    void saveCommittedDeltas(Map<Long, Long> deltaMap);

    Long getCommittedDelta(Long eventId);

    void deleteCommittedDeltas(Set<Long> eventIds);
}
