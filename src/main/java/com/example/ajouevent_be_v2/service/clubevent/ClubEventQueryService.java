package com.example.ajouevent_be_v2.service.clubevent;

import com.example.ajouevent_be_v2.common.dto.SliceResult;
import com.example.ajouevent_be_v2.common.exception.clubevent.ClubEventErrorCode;
import com.example.ajouevent_be_v2.common.exception.clubevent.ClubEventException;
import com.example.ajouevent_be_v2.domain.clubevent.ClubEvent;
import com.example.ajouevent_be_v2.domain.clubevent.EventBanner;
import com.example.ajouevent_be_v2.domain.clubevent.Type;
import com.example.ajouevent_be_v2.domain.keyword.Keyword;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.repository.port.clubevent.ClubEventCachePort;
import com.example.ajouevent_be_v2.repository.port.clubevent.ClubEventImageRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.clubevent.ClubEventRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.clubevent.EventBannerRepositoryPort;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClubEventQueryService {

    private final ClubEventRepositoryPort clubEventRepositoryPort;
    private final ClubEventCachePort clubEventCachePort;
    private final EventBannerRepositoryPort eventBannerRepositoryPort;
    private final ClubEventImageRepositoryPort clubEventImageRepositoryPort;

    public ClubEvent getEventById(Long eventId) {
        return clubEventRepositoryPort.findById(eventId)
            .orElseThrow(() -> new ClubEventException(ClubEventErrorCode.EVENT_NOT_FOUND));
    }

    /**
     *
     * @param clubEvent
     * @param member: 가입된 사용자라면, Email 기반으로 조회수 처리
     * @param clientIp: 가입된 사용자가 아니라면, IP 기반으로 조회수 처리
     * @param userAgent: 가입된 사용자가 아닐때,
     */
    public void handleViewCount(ClubEvent clubEvent, Member member, String clientIp, String userAgent) {
        if (member != null) {
            if (clubEventCachePort.isFirstUserRequest(member.getEmail(), clubEvent.getEventId())) {
                clubEventCachePort.recordUserRequest(member.getEmail(), clubEvent.getEventId());
                clubEventCachePort.incrementViewCount(clubEvent.getEventId(), clubEvent.getViewCount());
            }
        } else {
            if (clubEventCachePort.isFirstAnonymousRequest(clientIp, userAgent, clubEvent.getEventId())) {
                clubEventCachePort.recordAnonymousRequest(clientIp, userAgent, clubEvent.getEventId());
                clubEventCachePort.incrementViewCount(clubEvent.getEventId(), clubEvent.getViewCount());
            }
        }
    }

    public SliceResult<ClubEvent> getEventsByType(Type type, String keyword, Pageable pageable) {
        String searchKeyword = keyword != null ? keyword : "";
        Slice<ClubEvent> slice = clubEventRepositoryPort.findByTypeAndTitleContaining(
            type, searchKeyword, pageable);
        return SliceResult.from(slice, pageable);
    }

    public List<ClubEvent> getPopularEvents() {
        LocalDate now = LocalDate.now();
        LocalDateTime start = now.with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime end = now.with(DayOfWeek.SUNDAY).atTime(LocalTime.MAX);
        return clubEventRepositoryPort.findTop10ByCreatedAtBetween(start, end);
    }

    public SliceResult<ClubEvent> getEventsByTypes(List<Type> types, String keyword, Pageable pageable) {
        String searchKeyword = keyword != null ? keyword : "";
        Slice<ClubEvent> slice = searchKeyword.isEmpty()
            ? clubEventRepositoryPort.findByTypeIn(types, pageable)
            : clubEventRepositoryPort.findByTypeInAndTitleContaining(types, searchKeyword, pageable);
        return SliceResult.from(slice, pageable);
    }

    public SliceResult<ClubEvent> getEventsByIds(List<Long> eventIds, Type type, String keyword, Pageable pageable) {
        String searchKeyword = keyword != null ? keyword : "";
        Slice<ClubEvent> slice;
        if (type != null && !searchKeyword.isEmpty()) {
            slice = clubEventRepositoryPort.findByEventIdsAndTypeAndTitleContaining(eventIds, type, searchKeyword, pageable);
        } else if (type != null) {
            slice = clubEventRepositoryPort.findByEventIdsAndType(eventIds, type, pageable);
        } else if (!searchKeyword.isEmpty()) {
            slice = clubEventRepositoryPort.findByEventIdsAndTitleContaining(eventIds, searchKeyword, pageable);
        } else {
            slice = clubEventRepositoryPort.findByEventIds(eventIds, pageable);
        }
        return SliceResult.from(slice, pageable);
    }

    public SliceResult<ClubEvent> getEventsByKeyword(Keyword keyword, Pageable pageable) {
        Type type = keyword.getTopic().getType();
        Slice<ClubEvent> slice = clubEventRepositoryPort.findByTypeAndTitleContaining(
            type, keyword.getKoreanKeyword(), pageable);
        return SliceResult.from(slice, pageable);
    }

    public List<EventBanner> getBanners() {
        return eventBannerRepositoryPort.findAllOrderByBannerOrder();
    }

    public SliceResult<ClubEvent> getEventsByTypeAndKeywords(Type type, List<Keyword> keywords, Pageable pageable) {
        Slice<ClubEvent> slice = clubEventRepositoryPort.findByTypeAndAnyKeyword(type, keywords, pageable);
        return SliceResult.from(slice, pageable);
    }

    public Map<Long, List<String>> getImageUrlsByEventIds(List<Long> eventIds) {
        return clubEventImageRepositoryPort.findImageUrlsByEventIds(eventIds);
    }
}
