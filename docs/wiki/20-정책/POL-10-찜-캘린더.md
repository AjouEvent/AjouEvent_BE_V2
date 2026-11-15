---
type: 정책
status: 확정
source: policies.md 10장 · policies.json 10장
updated: 2026-11-15
tags: []
---

# 10장 찜(북마크) · 캘린더 연동

원문: `policies.md` 10장. 각 항목의 정책·값·근거는 추출 JSON 원문 그대로다.

### POL-LIKE-01 찜하기

- 정책: 로그인 회원은 공지를 찜할 수 있습니다. 찜하면 공지의 찜 수를 바로 1 올립니다.
- 값: POST /api/event/like/{eventId} → 204. 없는 공지 404, 이미 찜 409 AE-EVENT-ALREADY-LIKED. 회원·공지 유일 키 uq_event_likes.
- 근거: controller/ClubEventController.java:94-101, service/clubevent/ClubEventLikeCommandService.java:21-33, RES/create-indexes.sql:77

### POL-LIKE-02 찜 취소

- 정책: 찜 기록을 지우고 찜 수를 1 내립니다.
- 값: DELETE /api/event/like/{eventId} → 204. 찜하지 않은 공지 404 AE-EVENT-NOT-LIKED.
- 근거: controller/ClubEventController.java:103-110, orchestrator/ClubEventOrchestrator.java:253-257, service/clubevent/ClubEventLikeCommandService.java:35-40, service/clubevent/ClubEventLikeQueryService.java:37-40

### POL-LIKE-03 찜 개수 제한

- 정책: 찜 개수에는 상한이 없습니다.
- 값: 무제한
- 근거: service/clubevent/ClubEventLikeCommandService.java:21-33(개수 검사 없음)

### POL-LIKE-04 찜 목록

- 정책: 내가 찜한 공지를 토픽과 제목 검색어로 거를 수 있습니다. 없는 토픽을 넣으면 빈 목록입니다.
- 값: GET /api/event/liked?type=&keyword=, 기본 10개, created_at 내림차순, 모든 항목 star=true
- 근거: controller/ClubEventController.java:112-120, orchestrator/ClubEventOrchestrator.java:153-168, service/clubevent/ClubEventQueryService.java:130-143, repository/adapter/clubevent/ClubEventJpaRepositoryAdapter.java:50-87

### POL-CAL-01 캘린더 연동

- 정책: POST /api/users/connect-calendar로 Google 인가 코드를 보내면 refresh token을 받아 저장합니다. Google이 refresh token을 주지 않으면(오프라인 접근 동의가 없으면) 연동 실패입니다. 로그인할 때 refresh token이 함께 오면 그것도 저장합니다.
- 값: 요청 {authorizationCode, redirectUri} → 204. refresh token 없음 400 AE-AUTH-CALENDAR-NOT-CONNECTED, 코드 교환 실패 400 AE-AUTH-INVALID-AUTHORIZATION-CODE.
- 근거: controller/MemberController.java:52-56, service/calendar/CalendarCommandService.java:43-80, orchestrator/AuthOrchestrator.java:52-57

### POL-CAL-02 refresh token 저장 위치

- 정책: Google refresh token은 DB가 아니라 서버 파일에 평문으로 저장합니다. 회원 이메일로 파일 이름을 정하고, 다시 연동하면 덮어씁니다.
- 값: 경로 {CALENDAR_TOKEN_DIRECTORY, 기본 /app/tokens}/{이메일에서 [^a-zA-Z0-9_-]를 _로 바꾼 값}. 운영은 Docker 볼륨 calendar_tokens.
- 근거: service/calendar/CalendarCommandService.java:35, service/calendar/CalendarCommandService.java:88-126, RES/config/google.yaml:7, BE/deploy/docker-compose.yml:41-42

### POL-CAL-03 일정 추가

- 정책: POST /api/event/calendar는 저장한 refresh token으로 access token을 받은 뒤, 회원의 기본 캘린더(캘린더 ID = 이메일)에 일정을 만듭니다. 시작·종료 값은 받은 문자열을 그대로 dateTime에 넣습니다. 어떤 단계에서 실패해도 같은 오류를 줍니다. 횟수 제한이나 중복 방지는 없습니다.
- 값: 요청 {summary, description, startDate, endDate}(문서 예시 yyyy-MM-dd) → 204. 실패 400 AE-AUTH-CALENDAR-NOT-CONNECTED. 호출 POST https://www.googleapis.com/calendar/v3/calendars/{email}/events.
- 근거: controller/ClubEventController.java:128-134, service/calendar/CalendarCommandService.java:82-86, service/calendar/CalendarCommandService.java:128-176, dto/clubevent/CalendarRequest.java:6-18, RES/config/google.yaml:4-5

### POL-CAL-04 연동 해제

- 정책: 캘린더 연동을 끊는 API는 없고, 탈퇴해도 토큰 파일을 지우지 않습니다.
- 값: 없음
- 근거: controller/MemberController.java 전체, service/member/MemberService.java:71-75
