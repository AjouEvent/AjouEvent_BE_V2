---
type: 정책
status: 확정
source: policies.md 8장 · policies.json 8장
updated: 2026-11-15
tags: []
---

# 8장 인기글 · 주간 트렌드 · 배너

원문: `policies.md` 8장. 각 항목의 정책·값·근거는 추출 JSON 원문 그대로다.

### POL-POP-01 인기글 계산

- 정책: GET /api/event/popular는 로그인 없이 이번 주에 올라온 공지 중 조회수가 높은 10개를 돌려줍니다.
- 값: 기간 이번 주 월요일 00:00 ~ 일요일 23:59:59.999999999, 정렬 view_count 내림차순, 10개
- 근거: controller/ClubEventController.java:58-62, service/clubevent/ClubEventQueryService.java:78-83, repository/port/clubevent/ClubEventRepositoryPort.java:69-72, repository/adapter/clubevent/ClubEventJpaRepositoryAdapter.java:90-98

### POL-POP-02 인기글 갱신과 캐시

- 정책: 인기글은 Redis에 1시간 캐시하고, 매시 정각에 다시 계산합니다. 새 공지가 들어오면 캐시를 지웁니다. 캐시에서 꺼낼 때 찜 수·조회수만 DB값으로 바꾸고 순서는 캐시 시점 그대로 둡니다. Redis에 쌓였지만 아직 DB에 반영되지 않은 조회수(최대 3분)는 순위에 들어가지 않습니다.
- 값: 키 ClubEvent:popular-events, TTL 1시간, 갱신 cron 0 0 0/1 * * *
- 근거: scheduler/PopularEventCacheScheduler.java:14-17, service/clubevent/ClubEventQueryService.java:64-76, repository/adapter/clubevent/ClubEventCacheAdapter.java:64, repository/adapter/clubevent/ClubEventCacheAdapter.java:71, repository/adapter/clubevent/ClubEventCacheAdapter.java:112-126, service/clubevent/ClubEventCommandService.java:77

### POL-POP-03 주간 트렌드

- 정책: 「주간 트렌드」는 별도 계산이 없고 인기글(POL-POP-01)과 같습니다. 주간 배너 자동 추천은 설계 문서만 있고 코드는 없습니다.
- 값: 없음
- 근거: orchestrator/ClubEventOrchestrator.java:106-121, BE/docs/weekly-banner-recommendation.md(설계), scheduler/(해당 스케줄러 없음)

### POL-POP-04 배너 노출

- 정책: GET /api/event/banner는 로그인 없이 event_banners의 모든 배너를 순서대로 돌려줍니다. 시작일·종료일로 거르지 않고 값만 함께 내려 줍니다. 배너를 등록·수정하는 API는 없어 DB에 직접 넣습니다.
- 값: 정렬 banner_order 오름차순. 응답 [{bannerOrder, imgUrl, siteUrl, startDate, endDate}]. 캐시 키 ClubEvent:banners, TTL 24시간, 무효화 없음.
- 근거: controller/ClubEventController.java:122-126, service/clubevent/ClubEventQueryService.java:152-159, repository/port/clubevent/EventBannerRepositoryPort.java:28-30, repository/adapter/clubevent/ClubEventCacheAdapter.java:65, repository/adapter/clubevent/ClubEventCacheAdapter.java:72, repository/adapter/clubevent/ClubEventCacheAdapter.java:128-136, dto/clubevent/EventBannerResponse.java
