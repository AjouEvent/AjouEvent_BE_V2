package com.example.ajouevent_be_v2.repository.port.clubevent;

import com.example.ajouevent_be_v2.domain.clubevent.ClubEvent;
import com.example.ajouevent_be_v2.domain.clubevent.Type;
import com.example.ajouevent_be_v2.domain.keyword.Keyword;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventSummaryResult;
import com.example.ajouevent_be_v2.repository.adapter.clubevent.ClubEventJdbcRepositoryAdapter;
import com.example.ajouevent_be_v2.repository.adapter.clubevent.ClubEventJpaRepositoryAdapter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ClubEventRepositoryPort {

    private final ClubEventJpaRepositoryAdapter clubEventJpaRepositoryAdapter;
    private final ClubEventJdbcRepositoryAdapter clubEventJdbcRepositoryAdapter;

    public Optional<ClubEvent> findById(Long eventId) {
        return clubEventJpaRepositoryAdapter.findById(eventId);
    }

    public ClubEvent save(ClubEvent clubEvent) {
        return clubEventJpaRepositoryAdapter.save(clubEvent);
    }

    public Slice<ClubEventSummaryResult> findByTypeAndTitleContaining(Type type, String keyword, Pageable pageable) {
        return clubEventJpaRepositoryAdapter.findByTypeAndTitleContaining(type, keyword, pageable);
    }

    public Slice<ClubEventSummaryResult> findByTypeIn(List<Type> types, Pageable pageable) {
        return clubEventJpaRepositoryAdapter.findByTypeIn(types, pageable);
    }

    public Slice<ClubEventSummaryResult> findByTypeInAndTitleContaining(List<Type> types, String keyword, Pageable pageable) {
        return clubEventJpaRepositoryAdapter.findByTypeInAndTitleContaining(types, keyword, pageable);
    }

    public Slice<ClubEventSummaryResult> findByEventIds(List<Long> eventIds, Pageable pageable) {
        return clubEventJpaRepositoryAdapter.findByEventIds(eventIds, pageable);
    }

    public Slice<ClubEventSummaryResult> findByEventIdsAndType(List<Long> eventIds, Type type, Pageable pageable) {
        return clubEventJpaRepositoryAdapter.findByEventIdInAndType(eventIds, type, pageable);
    }

    public Slice<ClubEventSummaryResult> findByEventIdsAndTitleContaining(
        List<Long> eventIds, String keyword, Pageable pageable) {
        return clubEventJpaRepositoryAdapter.findByEventIdInAndTitleContaining(eventIds, keyword, pageable);
    }

    public Slice<ClubEventSummaryResult> findByEventIdsAndTypeAndTitleContaining(
        List<Long> eventIds, Type type, String keyword, Pageable pageable) {
        return clubEventJpaRepositoryAdapter.findByEventIdInAndTypeAndTitleContaining(
            eventIds, type, keyword, pageable);
    }

    public List<ClubEventSummaryResult> findTop10ByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        return clubEventJpaRepositoryAdapter.findTop10ByCreatedAtBetween(
            start, end, PageRequest.of(0, 10));
    }

    public List<ClubEventSummaryResult> findTop10ByType(Type type) {
        return clubEventJpaRepositoryAdapter.findTop10ByType(type, PageRequest.of(0, 10));
    }

    public Slice<ClubEventSummaryResult> findByTypeAndAnyKeyword(Type type, List<Keyword> keywords, Pageable pageable) {
        return clubEventJpaRepositoryAdapter.findByTypeAndAnyKeyword(type, keywords, pageable);
    }

    public void updateViews(Long viewCount, Long eventId) {
        clubEventJpaRepositoryAdapter.updateViews(viewCount, eventId);
    }

    public void batchIncrementViews(Map<Long, Long> deltaMap) {
        clubEventJdbcRepositoryAdapter.batchIncrementViews(deltaMap);
    }
}
