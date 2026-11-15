---
type: 정책
status: 확정
source: policies.md 9장 · policies.json 9장
updated: 2026-11-15
tags: []
---

# 9장 조회수

원문: `policies.md` 9장. 각 항목의 정책·값·근거는 추출 JSON 원문 그대로다.

### POL-VIEW-01 집계 시점

- 정책: 공지 상세를 열 때만 조회수를 셉니다. 목록·인기글 조회는 세지 않습니다.
- 값: 없음
- 근거: orchestrator/ClubEventOrchestrator.java:77-80, service/clubevent/ClubEventQueryService.java:43-49

### POL-VIEW-02 중복 제외 기준

- 정책: 같은 사람이 같은 공지를 24시간 안에 다시 열면 세지 않습니다. 로그인 회원은 이메일로, 비로그인은 IP와 User-Agent를 합친 해시로 구분합니다.
- 값: 키 ClubEvent:dedup:{eventId}:u:{email} / ClubEvent:dedup:{eventId}:a:{SHA-256(IP|UA) 앞 16자}, TTL 86,400초
- 근거: repository/adapter/clubevent/ClubEventCacheAdapter.java:138-149, repository/adapter/clubevent/ClubEventCacheAdapter.java:322-330, repository/adapter/clubevent/ClubEventCacheAdapter.java:67

### POL-VIEW-03 클라이언트 IP 판정

- 정책: 프록시 헤더를 차례로 보고 처음 나오는 값을 IP로 씁니다. 값에 쉼표가 있으면 첫 값만 씁니다.
- 값: 순서 X-Forwarded-For → Proxy-Client-IP → WL-Proxy-Client-IP → HTTP_CLIENT_IP → HTTP_X_FORWARDED_FOR → 접속 주소
- 근거: controller/ClubEventController.java:136-154

### POL-VIEW-04 Redis 누적

- 정책: 처음 보는 조회면 Lua 스크립트 한 번으로 중복 키 저장, 카운터 증가, 변경 목록 등록을 원자적으로 처리합니다.
- 값: 카운터 ClubEvent:views:{eventId}(안전 TTL 604,800초 = 7일, 증가할 때마다 갱신), 변경 목록 Set ClubEvent:dirty(TTL 없음)
- 근거: RES/scripts/increment_view.lua:8-15, repository/adapter/clubevent/ClubEventCacheAdapter.java:59-68, repository/adapter/clubevent/ClubEventCacheAdapter.java:312-320

### POL-VIEW-05 DB 반영(flush)

- 정책: 3분마다 Redis에 쌓인 조회수를 DB에 더합니다. DB 반영이 끝난 값은 committed 표식으로 남긴 뒤 Redis에서 뺍니다. DB 반영이 실패하면 Redis를 건드리지 않고 다음 주기에 다시 하고, 빼기가 실패하면 다음 주기에 빼기만 다시 합니다.
- 값: cron 0 0/3 * * * *. 대상 = 변경 목록 ∪ SCAN ClubEvent:views:*(count 100) ∪ 메모리 미정산분. 100개씩 처리, UPDATE club_events SET view_count = view_count + ? WHERE event_id = ? 배치. committed 키 ClubEvent:committed:{eventId} TTL 600초. 남은 값이 0 이하면 카운터 삭제·변경 목록 제거.
- 근거: scheduler/ViewCountScheduler.java:17-20, service/clubevent/ClubEventViewCountService.java:43-143, repository/adapter/clubevent/ClubEventJdbcRepositoryAdapter.java:13-22, RES/scripts/subtract_view_chunk.lua:8-17, repository/adapter/clubevent/ClubEventCacheAdapter.java:69

### POL-VIEW-06 Redis 장애 시

- 정책: Redis가 응답하지 않으면 조회수 증가는 건너뛰고 상세 조회는 정상으로 응답합니다. 캐시 조회는 미스로 보고 DB에서 읽습니다. flush 작업은 실패를 그대로 올려 그 주기를 건너뜁니다.
- 값: Redis 명령 타임아웃 2초, 연결 타임아웃 1초, 재연결 간격 100ms~1초
- 근거: repository/adapter/clubevent/ClubEventCacheAdapter.java:48-53, repository/adapter/clubevent/ClubEventCacheAdapter.java:230-245, config/RedisConfig.java:27-63, RES/config/db.yaml:20-29

### POL-VIEW-07 단일 인스턴스 전제

- 정책: 조회수 flush에는 분산 락(ShedLock)이 없습니다. 앱이 한 대만 떠 있다는 전제로 이중 반영을 막습니다.
- 값: 락 없음
- 근거: scheduler/ViewCountScheduler.java:17-20, service/clubevent/ClubEventViewCountService.java:34-36
