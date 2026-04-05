package com.example.ajouevent_be_v2.orchestrator;

import com.example.ajouevent_be_v2.common.dto.SliceResponse;
import com.example.ajouevent_be_v2.common.dto.SliceResult;
import com.example.ajouevent_be_v2.domain.clubevent.ClubEvent;
import com.example.ajouevent_be_v2.domain.clubevent.ClubEventLike;
import com.example.ajouevent_be_v2.domain.clubevent.Type;
import com.example.ajouevent_be_v2.domain.keyword.Keyword;
import com.example.ajouevent_be_v2.domain.keyword.KeywordMember;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.clubevent.CalendarRequest;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventCommand;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventDetailResponse;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventKeywordPair;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventResponse;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventWithKeywordResponse;
import com.example.ajouevent_be_v2.dto.clubevent.EventBannerResponse;
import com.example.ajouevent_be_v2.service.calendar.CalendarCommandService;
import com.example.ajouevent_be_v2.service.clubevent.ClubEventCommandService;
import com.example.ajouevent_be_v2.service.clubevent.ClubEventLikeCommandService;
import com.example.ajouevent_be_v2.service.clubevent.ClubEventLikeQueryService;
import com.example.ajouevent_be_v2.service.clubevent.ClubEventQueryService;
import com.example.ajouevent_be_v2.service.keyword.KeywordCommandService;
import com.example.ajouevent_be_v2.service.keyword.KeywordQueryService;
import com.example.ajouevent_be_v2.service.topic.TopicCommandService;
import com.example.ajouevent_be_v2.service.topic.TopicQueryService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * ClubEvent 도메인의 조합(Orchestration) 레이어.
 *
 * <p>여러 도메인 서비스의 결과를 조합해 클라이언트에 반환할 Response DTO를 조립한다.
 * Service 레이어는 엔티티/SliceResult만 반환하며, DTO 변환과 도메인 간 데이터 결합은 이 클래스에서만 수행한다.
 *
 * <p>@Transactional 선언 기준:
 * - 복수 CommandService 원자적 쓰기가 필요한 경우에만 선언
 * - 조회 메서드는 선언하지 않음 — Lazy 로딩은 QueryService 내에서 선제 해결
 */
@Component
@RequiredArgsConstructor
public class ClubEventOrchestrator {

    private final ClubEventCommandService clubEventCommandService;
    private final ClubEventQueryService clubEventQueryService;
    private final ClubEventLikeCommandService clubEventLikeCommandService;
    private final ClubEventLikeQueryService clubEventLikeQueryService;
    private final TopicQueryService topicQueryService;
    private final TopicCommandService topicCommandService;
    private final KeywordQueryService keywordQueryService;
    private final KeywordCommandService keywordCommandService;
    private final CalendarCommandService calendarCommandService;

    /**
     * 공지사항 생성 (Webhook 플로우 전용)
     *
     * 1. 중복 공지사항 검증 — 같은 타입 최근 10건 중 title+url 일치 시 409
     * 2. ClubEvent 빌드 및 저장
     */
    public ClubEvent createClubEvent(ClubEventCommand command) {
        clubEventCommandService.isDuplicateNotice(command.englishTopic(), command.title(), command.url());
        return clubEventCommandService.save(command);
    }

    /**
     * 게시글 상세 조회
     *
     * 1. eventId로 게시글 조회 (없으면 404)
     * 2. 조회수 처리 — 인증 사용자: email 기준, 익명 사용자: IP+UA 기준으로 24시간 내 중복 조회 차단.
     *       최초 조회 시 Redis에 increment. DB 동기화는 ViewCountScheduler가 3분마다 처리.
     * 3. 찜 여부 조회 (비인증이면 false 고정)
     * 4. 이미지 URL 배치 조회 (IN 절 단일 쿼리)
     * 5. 응답 조립 후 반환
     */
    public ClubEventDetailResponse getEventDetail(Long eventId, Member member, String clientIp, String userAgent) {
        ClubEvent event = clubEventQueryService.getEventById(eventId);
        clubEventQueryService.handleViewCount(event, member, clientIp, userAgent);
        boolean star = clubEventLikeQueryService.isEventLiked(member, event);
        List<String> imgUrls = clubEventQueryService.getImageUrlsByEventIds(List.of(eventId))
            .getOrDefault(eventId, List.of());
        return ClubEventDetailResponse.from(event, star, imgUrls);
    }

    /**
     * 카테고리(type)별 게시글 목록 조회
     *
     * 1. type 문자열 → Type enum 변환 (유효하지 않으면 빈 응답 반환)
     * 2. 해당 type의 게시글 목록 조회 (keyword 있으면 title LIKE 필터 적용)
     * 3. 이미지·찜 여부 조립 후 반환
     * 4. 인증 사용자에 한해 TopicMember.isRead 갱신 → 알림 뱃지 초기화
     */
    public SliceResponse<ClubEventResponse> getEventTypeList(String type, String keyword, Pageable pageable, Member member) {
        Optional<Type> eventType = resolveEventType(type);
        if (eventType.isEmpty()) {
            return SliceResponse.empty(pageable.getPageNumber());
        }
        SliceResult<ClubEvent> result = clubEventQueryService.getEventsByType(eventType.get(), keyword, pageable);
        if (member != null) {
            topicCommandService.markTopicAsRead(member, eventType.get().getEnglishTopic());
        }
        return buildClubEventSlice(result, member);
    }

    /**
     * 이번 주 인기 게시글 Top 10 조회
     *
     * 1. 이번 주(월~일) 생성 게시글 중 DB view_count 기준 상위 10개 조회
     *       (Redis에 미flush된 조회수는 미반영)
     * 2. 이미지·찜 여부 조립 후 반환
     */
    public List<ClubEventResponse> getTopPopularEvents(Member member) {
        List<ClubEvent> events = clubEventQueryService.getPopularEvents();
        List<Long> eventIds = events.stream().map(ClubEvent::getEventId).toList();
        Map<Long, List<String>> images = clubEventQueryService.getImageUrlsByEventIds(eventIds);
        Set<Long> likedIds = clubEventLikeQueryService.getLikedEventIds(member);
        return events.stream()
            .map(e -> ClubEventResponse.from(e, likedIds.contains(e.getEventId()), images.get(e.getEventId())))
            .toList();
    }

    /**
     * 구독 카테고리 게시글 목록 조회
     *
     * 비인증 요청은 Controller에서 AJOUNORMAL 카테고리로 분기되므로, 이 메서드 진입 시 member는 항상 존재한다.
     *
     * 1. 구독 중인 Topic → Type 목록 추출
     * 2. 해당 Type 목록으로 게시글 목록 조회 (keyword 있으면 title LIKE 필터 적용)
     * 3. 이미지·찜 여부 조립 후 반환
     *
     * * TopicMember.isRead 갱신 없음 — 읽음 처리는 단일 카테고리 조회(getEventTypeList)를 통해서만 이루어진다.
     */
    public SliceResponse<ClubEventResponse> getSubscribedEvents(Pageable pageable, Member member, String keyword) {
        List<Type> subscribedTypes = topicQueryService.getSubscribedTopics(member).stream()
            .map(tm -> tm.getTopic().getType())
            .toList();
        if (subscribedTypes.isEmpty()) {
            return SliceResponse.empty(pageable.getPageNumber());
        }
        SliceResult<ClubEvent> result = clubEventQueryService.getEventsByTypes(subscribedTypes, keyword, pageable);
        return buildClubEventSlice(result, member);
    }

    /**
     * 찜한 게시글 목록 조회
     *
     * 1. 내가 찜한 전체 eventId 목록 조회
     * 2. type/keyword 조합에 따라 DB 쿼리 선택 후 게시글 목록 조회
     *       (type O + keyword O / type O + keyword X / type X + keyword O / type X + keyword X — 총 4가지)
     * 3. 이미지 URL 배치 조회 후 응답 조립 (찜 목록이므로 star 필드 = true 고정)
     */
    public SliceResponse<ClubEventResponse> getLikedEvents(String type, String keyword, Pageable pageable, Member member) {
        List<Long> likedEventIds = clubEventLikeQueryService.getLikedEventsWithDetails(member).stream()
            .map(el -> el.getClubEvent().getEventId())
            .toList();
        if (likedEventIds.isEmpty()) {
            return SliceResponse.empty(pageable.getPageNumber());
        }
        boolean hasTypeFilter = type != null && !type.isBlank();
        Optional<Type> resolvedType = hasTypeFilter ? resolveEventType(type) : Optional.empty();
        if (hasTypeFilter && resolvedType.isEmpty()) {
            return SliceResponse.empty(pageable.getPageNumber());
        }
        SliceResult<ClubEvent> result = clubEventQueryService.getEventsByIds(likedEventIds, resolvedType.orElse(null), keyword, pageable);
        Map<Long, List<String>> images = clubEventQueryService.getImageUrlsByEventIds(toEventIds(result));
        return SliceResponse.from(result, e -> ClubEventResponse.from(e, true, images.get(e.getEventId())));
    }

    /**
     * 구독 키워드 전체 게시글 목록 조회
     *
     * 1. 구독 중인 Keyword 목록 조회 → type 기준으로 그룹핑
     * 2. type당 1번의 DB 쿼리 (EXISTS 서브쿼리: title LIKE any keyword of that type)
     * 3. 각 이벤트에 첫 번째 매칭 키워드 할당 (Java-side, 추가 쿼리 없음)
     * 4. 이미지·찜 여부 조립 후 createdAt 기준 내림차순 반환
     *
     * * KeywordMember.isRead 갱신 없음 — 읽음 처리는 단일 키워드 조회(getByKeyword)에서만 이루어진다.
     */
    public SliceResponse<ClubEventWithKeywordResponse> getAllBySubscribedKeywords(Member member, Pageable pageable) {
        List<Keyword> keywords = keywordQueryService.getUserKeywords(member).stream()
            .map(KeywordMember::getKeyword)
            .toList();
        if (keywords.isEmpty()) {
            return SliceResponse.empty(pageable.getPageNumber());
        }

        Map<Type, List<Keyword>> keywordsByType = keywords.stream()
            .collect(Collectors.groupingBy(kw -> kw.getTopic().getType()));

        List<ClubEventKeywordPair> allPairs = new ArrayList<>();
        boolean hasNext = false;

        for (Map.Entry<Type, List<Keyword>> entry : keywordsByType.entrySet()) {
            SliceResult<ClubEvent> result = clubEventQueryService.getEventsByTypeAndKeywords(
                entry.getKey(), entry.getValue(), pageable);
            List<String> lowerKeywords = toLowerKeywords(entry.getValue());
            for (ClubEvent event : result.result()) {
                findFirstMatchingKeyword(event, entry.getValue(), lowerKeywords).ifPresent(allPairs::add);
            }
            if (result.hasNext()) hasNext = true;
        }

        List<Long> eventIds = allPairs.stream().map(p -> p.event().getEventId()).distinct().toList();
        Map<Long, List<String>> images = clubEventQueryService.getImageUrlsByEventIds(eventIds);
        Set<Long> likedIds = clubEventLikeQueryService.getLikedEventIds(member);

        return new SliceResponse<>(
            toKeywordResponses(allPairs, likedIds, images),
            pageable.getPageNumber() > 0,
            hasNext,
            pageable.getPageNumber(),
            new SliceResponse.SortResponse(true, "DESC", "createdAt")
        );
    }

    /**
     * 단일 구독 키워드 게시글 목록 조회
     *
     * 1. 구독한 키워드 조회 (없으면 404) + KeywordMember.isRead 갱신 → 알림 뱃지 초기화
     * 2. keyword.topic.type + keyword.koreanKeyword 조건으로 게시글 목록 조회
     * 3. 이미지·찜 여부·키워드 조립 후 반환
     */
    public SliceResponse<ClubEventWithKeywordResponse> getByKeyword(String searchKeyword, Member member, Pageable pageable) {
        Keyword keyword = keywordQueryService.getSubscribedKeywordByName(member, searchKeyword);
        keywordCommandService.markKeywordMemberAsRead(member, keyword);
        SliceResult<ClubEvent> result = clubEventQueryService.getEventsByKeyword(keyword, pageable);
        Map<Long, List<String>> images = clubEventQueryService.getImageUrlsByEventIds(toEventIds(result));
        Set<Long> likedIds = clubEventLikeQueryService.getLikedEventIds(member);
        return SliceResponse.from(result, e -> ClubEventWithKeywordResponse.from(
            e, keyword.getKoreanKeyword(), likedIds.contains(e.getEventId()), images.get(e.getEventId())));
    }

    /**
     * 게시글 찜하기
     *
     * 1. eventId로 게시글 조회 (없으면 404)
     * 2. 이미 찜한 경우 ClubEventException(ALREADY_LIKED)
     * 3. EventLike 저장 + ClubEvent.likeCount 즉시 증가
     */
    public void likeEvent(Long eventId, Member member) {
        ClubEvent event = clubEventQueryService.getEventById(eventId);
        clubEventLikeCommandService.likeEvent(event, member);
    }

    /**
     * 게시글 찜 취소
     *
     * 1. eventId로 게시글 조회 (없으면 404)
     * 2. 해당 게시글에 대한 찜 레코드 조회 (없으면 ClubEventException(EVENT_NOT_LIKED))
     * 3. ClubEvent.likeCount 즉시 감소 + EventLike 삭제 (atomic)
     */
    public void cancelLikeEvent(Long eventId, Member member) {
        ClubEvent event = clubEventQueryService.getEventById(eventId);
        ClubEventLike clubEventLike = clubEventLikeQueryService.getEventLike(event, member);
        clubEventLikeCommandService.cancelLike(event, clubEventLike);
    }

    /**
     * 이벤트 배너 목록 조회
     */
    public List<EventBannerResponse> getBanners() {
        return clubEventQueryService.getBanners().stream()
            .map(EventBannerResponse::from)
            .toList();
    }

    /**
     * Google 캘린더에 일정 추가
     */
    public void addToCalendar(CalendarRequest request, Member member) {
        calendarCommandService.addEvent(member.getEmail(), request);
    }

    // ── 헬퍼 메서드 ──────────────────────────────────────────────────────────

    /**
     * 이미지 배치 조회 + 찜 여부 조회 + SliceResponse 조립을 한 번에 처리한다.
     * getEventTypeList / getSubscribedEvents 처럼 "일반 목록 조회" 패턴에서 공통으로 사용한다.
     */
    private SliceResponse<ClubEventResponse> buildClubEventSlice(SliceResult<ClubEvent> result, Member member) {
        Map<Long, List<String>> images = clubEventQueryService.getImageUrlsByEventIds(toEventIds(result));
        Set<Long> likedIds = clubEventLikeQueryService.getLikedEventIds(member);
        return SliceResponse.from(result, e -> ClubEventResponse.from(e, likedIds.contains(e.getEventId()), images.get(e.getEventId())));
    }

    /**
     * 이벤트 제목에서 첫 번째로 매칭되는 키워드를 찾아 ClubEventKeywordPair로 반환한다.
     * 매칭 키워드가 없으면 Optional.empty()를 반환한다.
     *
     * @param lowerKeywords 반복 toLowerCase 호출을 피하기 위해 미리 소문자로 변환한 키워드 목록
     */
    private Optional<ClubEventKeywordPair> findFirstMatchingKeyword(
        ClubEvent event, List<Keyword> keywords, List<String> lowerKeywords) {
        if (event.getTitle() == null) return Optional.empty();
        String lowerTitle = event.getTitle().toLowerCase();
        for (int i = 0; i < lowerKeywords.size(); i++) {
            if (lowerTitle.contains(lowerKeywords.get(i))) {
                return Optional.of(new ClubEventKeywordPair(event, keywords.get(i).getKoreanKeyword()));
            }
        }
        return Optional.empty();
    }

    /** 키워드 목록을 소문자 문자열 목록으로 변환한다 (반복 toLowerCase 방지용 사전 계산). */
    private List<String> toLowerKeywords(List<Keyword> keywords) {
        return keywords.stream()
            .map(kw -> kw.getKoreanKeyword().toLowerCase())
            .toList();
    }

    private List<Long> toEventIds(SliceResult<ClubEvent> result) {
        return result.result().stream().map(ClubEvent::getEventId).toList();
    }

    private List<ClubEventWithKeywordResponse> toKeywordResponses(
        List<ClubEventKeywordPair> pairs, Set<Long> likedIds, Map<Long, List<String>> images) {
        return pairs.stream()
            .sorted(Comparator.comparing(pair -> pair.event().getCreatedAt(), Comparator.reverseOrder()))
            .map(pair -> ClubEventWithKeywordResponse.from(
                pair.event(),
                pair.keyword(),
                likedIds.contains(pair.event().getEventId()),
                images.get(pair.event().getEventId())))
            .toList();
    }

    private Optional<Type> resolveEventType(String type) {
        if (type == null || type.isBlank()) return Optional.empty();
        try {
            return Optional.of(Type.valueOf(type.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
