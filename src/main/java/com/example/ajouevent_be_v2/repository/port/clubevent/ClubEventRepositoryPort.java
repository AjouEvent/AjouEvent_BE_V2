package com.example.ajouevent_be_v2.repository.port.clubevent;

import com.example.ajouevent_be_v2.domain.clubevent.ClubEvent;
import com.example.ajouevent_be_v2.domain.clubevent.Type;
import com.example.ajouevent_be_v2.repository.adapter.clubevent.ClubEventJpaRepositoryAdapter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ClubEventRepositoryPort {

    private final ClubEventJpaRepositoryAdapter clubEventJpaRepositoryAdapter;

    public Optional<ClubEvent> findById(Long eventId) {
        return clubEventJpaRepositoryAdapter.findById(eventId);
    }

    public ClubEvent save(ClubEvent clubEvent) {
        return clubEventJpaRepositoryAdapter.save(clubEvent);
    }

    public Slice<ClubEvent> findByTypeAndTitleContaining(Type type, String keyword, Pageable pageable) {
        return clubEventJpaRepositoryAdapter.findByTypeAndTitleContaining(type, keyword, pageable);
    }

    public Slice<ClubEvent> findByTypeIn(List<Type> types, Pageable pageable) {
        return clubEventJpaRepositoryAdapter.findByTypeIn(types, pageable);
    }

    public Slice<ClubEvent> findByTypeInAndTitleContaining(List<Type> types, String keyword, Pageable pageable) {
        return clubEventJpaRepositoryAdapter.findByTypeInAndTitleContaining(types, keyword, pageable);
    }

    public Slice<ClubEvent> findByEventIds(List<Long> eventIds, Pageable pageable) {
        return clubEventJpaRepositoryAdapter.findByEventIds(eventIds, pageable);
    }

    public Slice<ClubEvent> findByEventIdsAndType(List<Long> eventIds, Type type, Pageable pageable) {
        return clubEventJpaRepositoryAdapter.findByEventIdInAndType(eventIds, type, pageable);
    }

    public Slice<ClubEvent> findByEventIdsAndTitleContaining(
        List<Long> eventIds, String keyword, Pageable pageable) {
        return clubEventJpaRepositoryAdapter.findByEventIdInAndTitleContaining(eventIds, keyword, pageable);
    }

    public Slice<ClubEvent> findByEventIdsAndTypeAndTitleContaining(
        List<Long> eventIds, Type type, String keyword, Pageable pageable) {
        return clubEventJpaRepositoryAdapter.findByEventIdInAndTypeAndTitleContaining(
            eventIds, type, keyword, pageable);
    }

    public List<ClubEvent> findTop10ByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        return clubEventJpaRepositoryAdapter.findTop10ByCreatedAtBetweenOrderByViewCountDesc(start, end);
    }

    public List<ClubEvent> findTop10ByTypeOrderByCreatedAtDesc(Type type) {
        return clubEventJpaRepositoryAdapter.findTop10ByTypeOrderByCreatedAtDesc(type);
    }

    public void updateViews(Long viewCount, Long eventId) {
        clubEventJpaRepositoryAdapter.updateViews(viewCount, eventId);
    }
}
