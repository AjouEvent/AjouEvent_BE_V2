---
type: 정책
status: 확정
source: policies.md 11장 · policies.json 11장
updated: 2026-11-15
tags: []
---

# 11장 스케줄러 전체 목록

원문: `policies.md` 11장. 각 항목의 정책·값·근거는 추출 JSON 원문 그대로다.

- 모든 작업은 app-scheduler 풀(4스레드)에서 KST 기준으로 돕니다(config/SchedulerConfig.java:19-27, AjouEventBeV2Application.java:21-24).
- 크롤러 쪽 주기 작업은 4장 POL-CRAWL-02(환경변수 CRAWLING_PERIOD, 운영 60초)입니다.
- 앱 기동 시 1회 작업: IndexInitializer가 create-indexes.sql의 인덱스를 만들고 이미 있으면 건너뜁니다(config/IndexInitializer.java:28-55).

## 원문 표 1

| ID | 이름 | cron | 하는 일 | ShedLock | 근거 |
| --- | --- | --- | --- | --- | --- |
| POL-SCHED-01 | CrawlingTokenScheduler.scheduleTokenGeneration | 0 30 5 * * ? (매일 05:30) | 크롤링 웹훅 토큰 재발급, Redis crawling-token TTL 25시간 | 없음 | scheduler/CrawlingTokenScheduler.java:18-22 |
| POL-SCHED-02 | PopularEventCacheScheduler.refreshPopularEvents | 0 0 0/1 * * * (매시 정각) | 이번 주 인기글 10개 재계산 → Redis 저장 | 없음 | scheduler/PopularEventCacheScheduler.java:14-17 |
| POL-SCHED-03 | PushPollingPublisherScheduler.run | 0 */5 9-21 * * MON-FRI (평일 09:00~21:55, 5분) | 멈춘·재시도 대상 푸시 토큰 복구 발송. SCHEDULER_PUSH_POLLING_ENABLED=false면 등록 안 됨 | 있음 (pushPollingPublisher, 최대 9분·최소 30초) | scheduler/PushPollingPublisherScheduler.java:23-36, config/ShedLockConfig.java:12 |
| POL-SCHED-04 | TokenValidationScheduler.validateAndRemoveInvalidTokens | 0 0 5 ? * SUN (일요일 05:00) | 전체 활성 FCM 토큰 dry-run 검사(400개씩), 무효 토큰 소프트 삭제 | 없음 | scheduler/TokenValidationScheduler.java:30-58 |
| POL-SCHED-05 | ViewCountScheduler.updateDBFromRedis | 0 0/3 * * * * (3분마다) | Redis 조회수 → DB view_count 반영 | 없음 | scheduler/ViewCountScheduler.java:17-20 |
