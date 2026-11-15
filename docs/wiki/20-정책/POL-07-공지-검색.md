---
type: 정책
status: 확정
source: policies.md 7장 · policies.json 7장
updated: 2026-11-15
tags: []
---

# 7장 공지 목록 · 상세 · 검색

원문: `policies.md` 7장. 각 항목의 정책·값·근거는 추출 JSON 원문 그대로다.

### POL-EVENT-01 카테고리(토픽)별 목록

- 정책: GET /api/event/{type}은 로그인 없이 토픽 하나의 공지를 최신순으로 돌려줍니다. type은 대소문자를 구분하지 않고, 없는 type이면 오류 대신 빈 목록을 줍니다.
- 값: 기본 size=10, sort=createdAt,DESC(요청 파라미터로 바꿀 수 있음, 상한은 Spring Data 기본값). 응답 {result[], hasPrevious, hasNext, currentPage, sort}. 목록 항목의 content는 앞 200자, imgUrl은 첫 이미지.
- 근거: controller/ClubEventController.java:48-56, orchestrator/ClubEventOrchestrator.java:94-104, orchestrator/ClubEventOrchestrator.java:328-335, dto/clubevent/ClubEventResponse.java, common/dto/SliceResponse.java

### POL-EVENT-02 검색

- 정책: 검색어(keyword)는 공지 제목에만 부분 일치로 적용합니다. 본문·작성자는 검색하지 않습니다.
- 값: title LIKE CONCAT('%', :keyword, '%')(대소문자 구분은 DB 콜레이션을 따름)
- 근거: repository/adapter/clubevent/ClubEventJpaRepositoryAdapter.java:24-31, repository/adapter/clubevent/ClubEventJpaRepositoryAdapter.java:41-48, repository/adapter/clubevent/ClubEventJpaRepositoryAdapter.java:67-87

### POL-EVENT-03 카테고리 목록 캐시

- 정책: 카테고리 목록은 페이지·크기·정렬·검색어 조합마다 Redis에 6시간 캐시합니다. 그 토픽에 새 공지가 들어오면 그 토픽의 캐시를 모두 지웁니다. 캐시에서 꺼낼 때 찜 수·조회수는 DB 최신값으로 바꿔 줍니다.
- 값: 키 ClubEvent:type-events:{TYPE}:page:{n}:size:{s}:sort:{속성-방향}:keyword:{검색어}, TTL 6시간
- 근거: service/clubevent/ClubEventQueryService.java:51-62, service/clubevent/ClubEventQueryService.java:85-120, repository/adapter/clubevent/ClubEventCacheAdapter.java:63, repository/adapter/clubevent/ClubEventCacheAdapter.java:70, repository/adapter/clubevent/ClubEventCacheAdapter.java:107-110, repository/adapter/clubevent/ClubEventCacheAdapter.java:290-297

### POL-EVENT-04 목록 조회 시 읽음 처리

- 정책: 로그인한 회원이 카테고리 목록을 열면 그 토픽 구독을 읽음으로 바꿉니다(구독하지 않았으면 아무 일 없음).
- 값: topic_members.is_read=true, last_read_at=now
- 근거: orchestrator/ClubEventOrchestrator.java:100-102, service/topic/TopicCommandService.java:62-67

### POL-EVENT-05 구독 피드

- 정책: GET /api/event/subscribed는 로그인 회원에게 구독한 모든 토픽의 공지를 최신순으로 보여 줍니다. 비로그인이면 「아주대학교-일반」 목록을 대신 줍니다. 구독이 없으면 빈 목록입니다. 캐시하지 않습니다.
- 값: 기본 10개, created_at 내림차순, keyword는 제목 검색
- 근거: controller/ClubEventController.java:64-75, orchestrator/ClubEventOrchestrator.java:134-143, service/clubevent/ClubEventQueryService.java:122-128

### POL-EVENT-06 공지 상세

- 정책: GET /api/event/detail/{eventId}는 로그인 없이 공지 전체 본문과 이미지 목록을 돌려주고, 조회수를 처리합니다(POL-VIEW). 로그인 회원에게는 찜 여부를 함께 줍니다.
- 값: 없는 공지 404 AE-EVENT-NOT-FOUND. 응답 {eventId, title, writer, imgUrl[], createdAt, likesCount, viewCount, star, subject, type, url, content}. 비로그인 star=false.
- 근거: controller/ClubEventController.java:36-46, orchestrator/ClubEventOrchestrator.java:77-84, dto/clubevent/ClubEventDetailResponse.java, service/clubevent/ClubEventLikeQueryService.java:28-31

### POL-EVENT-07 공지 등록·수정·삭제

- 정책: 공지는 크롤러 웹훅으로만 생깁니다. 사용자·관리자가 공지를 등록·수정·삭제하는 API는 없습니다.
- 값: 없음
- 근거: controller/ClubEventController.java 전체, controller/WebhookController.java:21-26
