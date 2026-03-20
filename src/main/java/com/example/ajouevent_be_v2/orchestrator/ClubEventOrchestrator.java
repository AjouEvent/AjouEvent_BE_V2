package com.example.ajouevent_be_v2.orchestrator;

import com.example.ajouevent_be_v2.common.dto.SliceResponse;
import com.example.ajouevent_be_v2.common.dto.SliceResult;
import com.example.ajouevent_be_v2.domain.clubevent.ClubEvent;
import com.example.ajouevent_be_v2.domain.clubevent.ClubEventLike;
import com.example.ajouevent_be_v2.domain.clubevent.Type;
import com.example.ajouevent_be_v2.domain.keyword.Keyword;
import com.example.ajouevent_be_v2.domain.keyword.KeywordMember;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventCommand;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventDetailResponse;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventKeywordPair;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventResponse;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventWithKeywordResponse;
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
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * ClubEvent 도메인의 조합(Orchestration) 레이어.
 *
 * <p>여러 도메인 서비스의 결과를 조합해 클라이언트에 반환할 Response DTO를 조립한다.
 * Service 레이어는 엔티티/SliceResult만 반환하며, DTO 변환과 도메인 간 데이터 결합은 이 클래스에서만 수행한다.
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
     * 4. 응답 조립 후 반환
     */
    public ClubEventDetailResponse getEventDetail(
        Long eventId, Member member, String clientIp, String userAgent) {
        ClubEvent event = clubEventQueryService.getEventById(eventId);
        clubEventQueryService.handleViewCount(event, member, clientIp, userAgent);
        boolean star = clubEventLikeQueryService.isEventLiked(member, event);
        return ClubEventDetailResponse.from(event, star);
    }

    /**
     * 카테고리(type)별 게시글 목록 조회
     *
     * 1. type 문자열 → Type enum 변환 (유효하지 않으면 빈 응답 반환)
     * 2. 해당 type의 게시글 목록 조회 (keyword 있으면 title LIKE 필터 적용)
     * 3. 내가 찜한 게시글 ID 목록 조회
     * 4. 인증 사용자에 한해 TopicMember.isRead 갱신 → 알림 뱃지 초기화
     * 5. 각 게시글에 찜(like) 여부(star 필드) 포함해 응답 조립 후 반환
     */
    public SliceResponse<ClubEventResponse> getEventTypeList(
        String type, String keyword, Pageable pageable, Member member) {
        Optional<Type> eventType = resolveEventType(type);
        if (eventType.isEmpty()) {
            return SliceResponse.empty(pageable.getPageNumber());
        }
        SliceResult<ClubEvent> result = clubEventQueryService.getEventsByType(eventType.get(), keyword, pageable);
        Set<Long> likedIds = clubEventLikeQueryService.getLikedEventIds(member);
        if (member != null) {
            topicCommandService.markTopicAsRead(member, eventType.get().getEnglishTopic());
        }
        return SliceResponse.from(result, e -> ClubEventResponse.from(e, likedIds.contains(e.getEventId())));
    }

    /**
     * 이번 주 인기 게시글 Top 10 조회
     *
     * 1. 이번 주(월~일) 생성 게시글 중 DB view_count 기준 상위 10개 조회
     *       (Redis에 미flush된 조회수는 미반영)
     * 2. 내가 찜한 게시글 ID 목록 조회
     * 3. 각 게시글에 찜(like) 여부(star 필드) 포함해 응답 조립 후 반환
     */
    public List<ClubEventResponse> getTopPopularEvents(Member member) {
        List<ClubEvent> events = clubEventQueryService.getPopularEvents();
        Set<Long> likedIds = clubEventLikeQueryService.getLikedEventIds(member);
        return events.stream()
            .map(e -> ClubEventResponse.from(e, likedIds.contains(e.getEventId())))
            .toList();
    }

    /**
     * 구독 카테고리 게시글 목록 조회
     *
     * 비인증 요청은 Controller에서 AJOUNORMAL 카테고리로 분기되므로, 이 메서드 진입 시 member는 항상 존재한다.
     *
     * 1. 구독 중인 Topic 목록 조회 → Type 목록 추출
     * 2. 해당 Type 목록으로 게시글 목록 조회 (keyword 있으면 title LIKE 필터 적용)
     * 3. 내가 찜한 게시글 ID 목록 조회
     * 4. 각 게시글에 찜(like) 여부(star 필드) 포함해 응답 조립 후 반환
     *
     * * TopicMember.isRead 갱신 없음 — 여러 카테고리가 합산되어 어느 TopicMember를 읽음 처리할지 특정 불가.
     *   읽음 처리는 단일 카테고리 조회(getEventTypeList)를 통해서만 이루어진다.
     */
    public SliceResponse<ClubEventResponse> getSubscribedEvents(
        Pageable pageable, Member member, String keyword) {
        List<Type> subscribedTypes = topicQueryService.getSubscribedTopics(member).stream()
            .map(tm -> tm.getTopic().getType())
            .toList();
        if (subscribedTypes.isEmpty()) {
            return SliceResponse.empty(pageable.getPageNumber());
        }
        SliceResult<ClubEvent> result = clubEventQueryService.getEventsByTypes(subscribedTypes, keyword, pageable);
        Set<Long> likedIds = clubEventLikeQueryService.getLikedEventIds(member);
        return SliceResponse.from(result, e -> ClubEventResponse.from(e, likedIds.contains(e.getEventId())));
    }

    /**
     * 찜한 게시글 목록 조회
     *
     * 1. 내가 찜한 전체 eventId 목록 조회
     * 2. type 문자열 → Type enum 변환 (유효하지 않으면 빈 응답 반환)
     * 3. type/keyword 조합에 따라 DB 쿼리 선택 후 게시글 목록 조회
     *       (type O + keyword O / type O + keyword X / type X + keyword O / type X + keyword X — 총 4가지)
     * 4. 모든 결과의 찜여부(star 필드) = true 고정 후 응답 조립 반환 (찜 목록이므로 별도 확인 불필요)
     */
    public SliceResponse<ClubEventResponse> getLikedEvents(
        String type, String keyword, Pageable pageable, Member member) {
        List<Long> eventIds = clubEventLikeQueryService.getLikedEventsWithDetails(member).stream()
            .map(el -> el.getClubEvent().getEventId())
            .toList();
        if (eventIds.isEmpty()) {
            return SliceResponse.empty(pageable.getPageNumber());
        }
        boolean hasTypeFilter = type != null && !type.isBlank();
        Optional<Type> resolvedType = hasTypeFilter ? resolveEventType(type) : Optional.empty();
        if (hasTypeFilter && resolvedType.isEmpty()) {
            return SliceResponse.empty(pageable.getPageNumber());
        }
        Type eventType = resolvedType.orElse(null);
        SliceResult<ClubEvent> result = clubEventQueryService.getEventsByIds(eventIds, eventType, keyword, pageable);
        return SliceResponse.from(result, e -> ClubEventResponse.from(e, true));
    }

    /**
     * 구독 키워드 전체 게시글 목록 조회
     *
     * 1. 구독 중인 KeywordMember 목록 조회 → Keyword 엔티티 목록 추출
     * 2. 각 키워드별로 반복:
     *       keyword.topic.type + keyword.koreanKeyword 조건으로 게시글 조회
     *       결과를 (ClubEvent, keyword) 쌍으로 수집
     * 3. 전체 수집 결과를 createdAt 기준 내림차순 정렬 후 응답 조립 반환
     *       hasNext: 하나의 키워드라도 다음 페이지가 있으면 true
     *
     * * KeywordMember.isRead 갱신 없음 — 읽음 처리는 단일 키워드 조회(getByKeyword)에서만 이루어진다.
     */
    public SliceResponse<ClubEventWithKeywordResponse> getAllBySubscribedKeywords(
        Member member, Pageable pageable) {
        List<Keyword> keywords = keywordQueryService.getUserKeywords(member).stream()
            .map(KeywordMember::getKeyword)
            .toList();
        Set<Long> likedIds = clubEventLikeQueryService.getLikedEventIds(member);
        List<ClubEventKeywordPair> allPairs = new ArrayList<>();
        boolean hasNext = false;
        for (Keyword keyword : keywords) {
            SliceResult<ClubEvent> result = clubEventQueryService.getEventsByKeyword(keyword, pageable);
            result.result().stream()
                .map(e -> new ClubEventKeywordPair(e, keyword.getKoreanKeyword()))
                .forEach(allPairs::add);
            if (result.hasNext()) {
                hasNext = true;
            }
        }
        return new SliceResponse<>(
            toKeywordResponses(allPairs, likedIds),
            pageable.getPageNumber() > 0,
            hasNext,
            pageable.getPageNumber(),
            new SliceResponse.SortResponse(true, "DESC", "createdAt")
        );
    }

    /**
     * 단일 구독 키워드 게시글 목록 조회
     *
     * 1. 내가 구독한 키워드 중 요청 키워드와 일치하는 Keyword 엔티티 조회
     *       (구독하지 않은 키워드이면 KeywordException(KEYWORD_NOT_FOUND))
     * 2. KeywordMember.isRead 갱신 → 알림 뱃지 초기화 (이미 true이면 UPDATE 생략)
     * 3. keyword.topic.type + keyword.koreanKeyword 조건으로 게시글 목록 조회
     * 4. 내가 찜한 게시글 ID 목록 조회
     * 5. 각 게시글에 찜(like) 여부(star 필드)와 키워드 포함해 응답 조립 후 반환
     */
    public SliceResponse<ClubEventWithKeywordResponse> getByKeyword(
        String searchKeyword, Member member, Pageable pageable) {
        Keyword keyword = keywordQueryService.getSubscribedKeywordByName(member, searchKeyword);
        keywordCommandService.markKeywordMemberAsRead(member, keyword);
        SliceResult<ClubEvent> result = clubEventQueryService.getEventsByKeyword(keyword, pageable);
        Set<Long> likedIds = clubEventLikeQueryService.getLikedEventIds(member);
        return SliceResponse.from(result, e -> ClubEventWithKeywordResponse.from(
            e, keyword.getKoreanKeyword(), likedIds.contains(e.getEventId())));
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

    private List<ClubEventWithKeywordResponse> toKeywordResponses(
        List<ClubEventKeywordPair> pairs, Set<Long> likedIds) {
        return pairs.stream()
            .sorted(Comparator.comparing(pair -> pair.event().getCreatedAt(), Comparator.reverseOrder()))
            .map(pair -> ClubEventWithKeywordResponse.from(
                pair.event(), pair.keyword(), likedIds.contains(pair.event().getEventId())))
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
