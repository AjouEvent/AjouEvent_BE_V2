---
type: 정책
status: 확정
source: policies.md 13장 · policies.json 13장
updated: 2026-11-15
tags: []
---

# 13장 외부 연동

원문: `policies.md` 13장. 각 항목의 정책·값·근거는 추출 JSON 원문 그대로다.

- 근거: common/discord/DiscordMessageService.java:20-36
- 운영 설정: --appendonly yes --maxmemory 256mb --maxmemory-policy noeviction(BE/deploy/docker-compose.yml:58)
- Spring CacheManager(기본 300초, popular-events 1시간, type-events 6시간)도 설정돼 있지만 @Cacheable을 쓰는 곳은 없습니다(config/RedisCacheConfig.java:18-38).
- 근거: 표 안에 적음

### POL-EXT-01 Firebase Cloud Messaging

- 정책: 서비스 계정 파일로 Firebase를 초기화하고, Firebase SDK의 스레드 풀 대신 fcm-default 풀을 쓰게 합니다. 초기화에 실패해도 앱은 뜨고 오류 로그만 남깁니다.
- 값: 인증 파일 firebase/firebase-service-account.json(접두사 없으면 classpath, file:이면 서버 파일), 연결 10초 · 읽기 30초
- 근거: FcmInitializer.java:32-65, RES/config/fcm.yaml:3, RES/config/fcm.yaml:8-9

### POL-EXT-02 Google

- 정책: 로그인(OAuth 토큰 교환·사용자 정보), 학과 조회(People API), 캘린더(토큰 갱신·일정 생성)에 Google API를 씁니다.
- 값: 토큰 https://oauth2.googleapis.com/token, People https://people.googleapis.com/v1/people/me?personFields=organizations, Calendar https://www.googleapis.com/calendar/v3/calendars/{calendarId}/events, 클라이언트 GOOGLE_CLIENT_ID·GOOGLE_CLIENT_SECRET
- 근거: RES/config/google.yaml:1-7, RES/config/oauth.yaml:1-13, service/auth/OauthService.java:95-113, service/calendar/CalendarCommandService.java:128-146

### POL-EXT-03 Discord 채널

- 정책: Discord 웹훅 채널 두 개를 씁니다. 전송 실패 시 DiscordException(502)을 던집니다.
- 값: 
- 근거: 

### POL-EXT-04 Redis 키와 TTL

- 정책: Redis는 캐시, 조회수 집계, 크롤링 토큰 저장에 씁니다. 운영 Redis는 AOF로 영속화하고 메모리가 차도 키를 지우지 않습니다.
- 값: 
- 근거: 

### POL-EXT-05 MySQL과 기타

- 정책: 주 저장소는 MySQL이고, ShedLock 락도 DB(shedlock 테이블, DB 시각 기준)에 둡니다. 운영은 스키마 자동 변경 없이 검증만 합니다. 로그는 Loki로 보냅니다.
- 값: Hikari 최대 10 · 최소 유휴 5(환경변수로 변경), 연결 타임아웃 30초. 운영 ddl-auto=validate.
- 근거: RES/config/db.yaml:1-18, config/ShedLockConfig.java:15-22, RES/schema.sql:207-213, RES/application-prod.yaml:1-5, RES/logback-spring.xml:15-38

## 원문 표 1

| 채널 | 환경변수 | 보내는 내용 | 근거 |
| --- | --- | --- | --- |
| error (ajouevent-discord-error) | DISCORD_ERROR_WEBHOOK_URL | FCM 발송 대기열 초과, 발송 오류, 발송 허가 대기 초과, SENDER_ID_MISMATCH | RES/config/discord.yaml:3-5, config/DiscordFeignClient.java:9-14, POL-PUSH-16 |
| member (ajouevent-discord-member) | DISCORD_MEMBER_WEBHOOK_URL | 신규 회원 가입 | RES/config/discord.yaml:6-8, config/DiscordMemberFeignClient.java:9-14, POL-AUTH-03 |

## 원문 표 2

| 키 | 용도 | TTL | 근거 |
| --- | --- | --- | --- |
| crawling-token | 웹훅 인증 토큰 | 90,000초(25시간) | repository/adapter/webhook/CrawlingTokenCacheAdapter.java:17, RES/config/webhook.yaml:3 |
| ClubEvent:type-events:{TYPE}:page:{n}:size:{s}:sort:{..}:keyword:{..} | 카테고리 목록 캐시 | 6시간 | repository/adapter/clubevent/ClubEventCacheAdapter.java:63, :70, :290-297 |
| ClubEvent:popular-events | 인기글 캐시 | 1시간 | repository/adapter/clubevent/ClubEventCacheAdapter.java:64, :71 |
| ClubEvent:banners | 배너 캐시 | 24시간 | repository/adapter/clubevent/ClubEventCacheAdapter.java:65, :72 |
| ClubEvent:dedup:{eventId}:u:{email} · ...:a:{hash} | 조회 중복 제외 | 86,400초(24시간) | repository/adapter/clubevent/ClubEventCacheAdapter.java:67, :138-149 |
| ClubEvent:views:{eventId} | 조회수 누적 | 604,800초(7일, 증가 시 갱신) | repository/adapter/clubevent/ClubEventCacheAdapter.java:59, :68 |
| ClubEvent:dirty | 조회수 변경 목록(Set) | 없음 | repository/adapter/clubevent/ClubEventCacheAdapter.java:61 |
| ClubEvent:committed:{eventId} | DB 반영 완료 표식 | 600초(10분) | repository/adapter/clubevent/ClubEventCacheAdapter.java:62, :69 |
