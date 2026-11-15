---
type: 정책
status: 확정
source: policies.md 5장 · policies.json 5장
updated: 2026-11-15
tags: []
---

# 5장 알림 발송

원문: `policies.md` 5장. 각 항목의 정책·값·근거는 추출 JSON 원문 그대로다.

- 기동 검사: callback max + queue ≥ 3, default max + queue ≥ (3 + 1) × 500(config/FcmConfig.java:32-33, config/FcmConfig.java:64-66)
- FCM HTTP 타임아웃 연결 10초 · 읽기 30초(RES/config/fcm.yaml:8-9, FcmInitializer.java:39-40)
- 종료 대기 30초, 종료 순서 dispatch → default → callback(config/FcmConfig.java:57-60)
- 근거: 표 안에 적음
- 근거: service/webhook/FcmPushResultService.java:48-77, service/webhook/FcmPushResultService.java:140-144, service/webhook/FcmPushResultService.java:198-261, repository/adapter/token/TokenBulkRepositoryAdapter.java:44-45

### POL-PUSH-01 발송 흐름

- 정책: 웹훅 처리 중에 토픽 푸시 묶음(클러스터) 1개와 매칭 키워드마다 키워드 클러스터 1개를 만듭니다. 클러스터·대상 토큰·알림함 기록을 DB에 PENDING으로 저장한 뒤, 발송은 fcm-dispatch 실행기에 넘기고 웹훅은 바로 응답합니다.
- 값: 테이블 push_clusters(클러스터), push_cluster_tokens(토큰별 작업), push_notifications(알림함). 클러스터 생성·알림함 저장은 서로 다른 트랜잭션입니다.
- 근거: orchestrator/WebhookOrchestrator.java:28-35, orchestrator/PushOrchestrator.java:28-50, service/push/PushClusterCommandService.java:107-135, service/notification/NotificationPushService.java:27-49, orchestrator/FcmOrchestrator.java:61-73

### POL-PUSH-02 토픽 푸시 대상

- 정책: 토픽 푸시는 그 토픽을 구독하고 수신을 켠 회원이 가진 활성 FCM 토큰 전부로 보냅니다. 대상 회원이 없거나 활성 토큰이 하나도 없으면 클러스터도 알림함 기록도 만들지 않습니다.
- 값: 활성 토큰 = tokens.is_deleted = false(만료일은 보지 않음)
- 근거: service/push/PushClusterCommandService.java:44-74, repository/adapter/topic/TopicMemberJpaRepositoryAdapter.java:28-29, repository/adapter/token/TokenJpaRepositoryAdapter.java:20-21

### POL-PUSH-03 회원 단위 중복 제거 없음

- 정책: 한 공지에 대해 회원이 받는 푸시 수를 하나로 줄이지 않습니다. 토픽을 구독하면서 그 공지에 맞는 키워드도 구독했으면 토픽 푸시와 키워드 푸시를 둘 다 받고, 키워드가 여러 개 맞으면 그 수만큼 받습니다.
- 값: 최대 푸시 수 = 1(토픽) + 매칭 키워드 수
- 근거: orchestrator/PushOrchestrator.java:33-46

### POL-PUSH-04 대상 기기

- 정책: 회원이 여러 기기(토큰)를 등록했으면 활성 토큰마다 한 건씩 보냅니다.
- 값: 대상 수 total_count = 활성 토큰 수
- 근거: service/push/PushClusterCommandService.java:57-69, service/push/PushClusterCommandService.java:124-135

### POL-PUSH-05 푸시 문구와 데이터

- 정책: 제목은 토픽명(키워드 푸시는 키워드 + 토픽명), 본문은 공지 제목입니다. 누르면 AjouEvent 공지 상세로 이동합니다.
- 값: 토픽 푸시 제목 [{토픽 한국어명}], 키워드 푸시 제목 {키워드} - [{토픽 한국어명}], 본문 {공지 제목} / 이미지 공지의 첫 이미지(없으면 https://www.ajou.ac.kr/_res/ajou/kr/img/intro/img-symbol.png) / data click_action=https://www.ajouevent.com/event/{eventId}, push_cluster_id, unread_count(회원의 읽지 않은 알림 전체 수)
- 근거: service/push/PushClusterCommandService.java:63-66, service/push/PushClusterCommandService.java:93-96, service/push/PushClusterCommandService.java:137-150, service/webhook/FcmPushService.java:136-158, RES/config/fcm.yaml:4-6, repository/adapter/notification/PushNotificationJpaRepositoryAdapter.java:21-27

### POL-PUSH-06 발송 시간대

- 정책: 방해 금지 시간은 없습니다. 공지가 들어오면 시간과 요일에 상관없이 바로 보냅니다. 실패분을 다시 보내는 복구 작업만 평일 09~21시에 돕니다(POL-PUSH-12).
- 값: 즉시 발송 24시간·매일
- 근거: orchestrator/WebhookOrchestrator.java:32-35, orchestrator/FcmOrchestrator.java:61-73(시간 조건 없음), scheduler/PushPollingPublisherScheduler.java:30

### POL-PUSH-07 배치와 동시 처리 한도

- 정책: 클러스터의 토큰을 400개씩 나눠 보냅니다. 동시에 처리 중인 배치는 3개까지이고, 자리가 나지 않으면 발송 스레드가 기다립니다. 300초를 기다려도 자리가 나지 않으면 남은 배치를 멈추고 Discord로 알립니다(남은 토큰은 복구 작업이 보냄).
- 값: 배치 400토큰, max-in-flight-batches=3, acquire-timeout-seconds=300, FCM 호출 sendEachAsync(메시지마다 HTTP 요청)
- 근거: orchestrator/FcmOrchestrator.java:30, orchestrator/FcmOrchestrator.java:135-164, service/webhook/FcmPushService.java:70-99, RES/config/fcm.yaml:10-15

### POL-PUSH-08 토큰 선점(중복 발송 방지)

- 정책: 배치를 보내기 직전에 해당 토큰 행을 잠그고, 아직 예상 상태(최초 발송은 PENDING, 재시도는 RETRY_PENDING)인 토큰만 IN_PROGRESS로 바꿔 그 토큰에만 보냅니다. 다른 경로가 먼저 가져간 토큰은 건너뜁니다.
- 값: SELECT id, job_status ... FOR UPDATE
- 근거: service/webhook/FcmPushResultService.java:117-136, repository/adapter/push/PushClusterTokenBulkRepositoryAdapter.java:97-98, domain/push/PushClusterToken.java:66-69

### POL-PUSH-09 스레드 풀

- 정책: 발송은 용도별 스레드 풀 세 개로 나눠 돌립니다. 기동할 때 풀 용량이 동시 처리 상한을 수용하는지 검사해, 맞지 않으면 앱이 뜨지 않습니다.
- 값: 
- 근거: 

### POL-PUSH-10 발송 결과 처리

- 정책: 토큰별 결과를 FCM 오류 코드에 따라 아래처럼 처리합니다.
- 값: 
- 근거: 

### POL-PUSH-11 재시도 횟수

- 정책: 토큰 하나는 최초 발송을 포함해 최대 3번 보냅니다. retry_count가 3에 닿은 토큰은 복구 작업이 다시 보내지 않고 PERMANENT_FAIL로 바꿉니다.
- 값: max-retry-count=3. 대기 1분(1회차 실패) → 2분(2회차) → 3회차 실패 후 retry_count=3 → PERMANENT_FAIL. 복구 작업이 5분마다 돌기 때문에 실제 재발송 간격은 5분 단위입니다.
- 근거: RES/application.yaml:56-58, config/properties/PushProperties.java:14-15, service/webhook/FcmPushResultService.java:151-192, service/webhook/FcmPushResultService.java:247-261

### POL-PUSH-12 복구 작업(폴링 릴레이)

- 정책: 서버가 발송 도중 내려가거나 재시도할 토큰이 생기면 PushPollingPublisherScheduler가 평일 업무 시간에 5분마다 다시 보냅니다. 오래 멈춘 PENDING·IN_PROGRESS 토큰은 retry_count를 늘리지 않고 RETRY_PENDING으로 바꾼 뒤 바로 보냅니다. 재발송 결과는 클러스터의 재시도 카운터에만 쌓습니다.
- 값: cron 0 */5 9-21 * * MON-FRI(평일 09:00~21:55, 5분 간격) / ShedLock 이름 pushPollingPublisher, 최대 9분 · 최소 30초 점유 / 대상: PENDING이고 request_time이 10분 지남 / IN_PROGRESS이고 processed_time이 10분 지남 / RETRY_PENDING이고 retry_after 도래 및 retry_count ≤ 3 / 켜고 끄기: 환경변수 SCHEDULER_PUSH_POLLING_ENABLED(기본 true). false면 빈 자체가 등록되지 않습니다(앱 기동 시 판정).
- 근거: scheduler/PushPollingPublisherScheduler.java:23-64, config/ShedLockConfig.java:12-22, service/push/PushClusterQueryService.java:33-37, repository/adapter/push/PushClusterTokenJpaRepositoryAdapter.java:18-33, domain/push/PushClusterToken.java:93-97, RES/application.yaml:48-51, RES/application.yaml:56-57

### POL-PUSH-13 클러스터 상태

- 정책: 배치 결과가 올 때마다 성공·실패 수를 더하고 상태를 다시 정합니다. 실패가 한 건이라도 있으면 PARTIAL_FAIL입니다. 재시도 결과는 상태를 바꾸지 않습니다.
- 값: 실패 수 > 0 → PARTIAL_FAIL, 성공 수 > 0 → SUCCESS, 둘 다 0 → NONE. 대상 토큰 없음 → NONE. 발송 시작 시 IN_PROGRESS·start_at 기록, 결과마다 end_at 갱신. 재시도 결과 → retry_success_count, retry_fail_count.
- 근거: repository/adapter/push/PushClusterJpaRepositoryAdapter.java:17-45, service/webhook/FcmPushResultService.java:37-46, domain/push/PushCluster.java:96-99, domain/clubevent/JobStatus.java:3-12

### POL-PUSH-14 FCM 토큰 등록

- 정책: 앱은 POST /send/registration-token(또는 로그인 요청의 fcmToken)으로 기기 토큰을 등록합니다. 같은 회원의 같은 토큰이면 만료일만 늘리고, 새 토큰이면 저장한 뒤 회원의 기존 토픽·키워드 구독에 연결합니다. 만료일은 발송 대상 판정에 쓰지 않습니다.
- 값: 만료일 오늘 + 10주. 토큰 값은 전체에서 유일(uq_tokens_token_value). 응답 200.
- 근거: controller/FcmTokenController.java:21-25, orchestrator/FcmTokenOrchestrator.java:37-56, service/token/FcmTokenCommandService.java:19-48, domain/member/Token.java:32-52, RES/create-indexes.sql:13, repository/port/token/TokenRepositoryPort.java:23-25

### POL-PUSH-15 토큰 무효화

- 정책: 무효 토큰은 두 경로로 소프트 삭제(is_deleted=true)합니다. 발송 결과가 UNREGISTERED·INVALID_ARGUMENT일 때, 그리고 매주 일요일 05:00 전체 활성 토큰을 FCM dry-run으로 검사할 때입니다. 검사 요청이 통째로 실패하면 토큰마다 하나씩 다시 검사합니다.
- 값: 주간 검사 배치 400개, 판정 오류 코드 UNREGISTERED·INVALID_ARGUMENT
- 근거: scheduler/TokenValidationScheduler.java:25-80, service/webhook/FcmPushService.java:160-214, service/token/TokenService.java:124-128, service/webhook/FcmPushResultService.java:220-230

### POL-PUSH-16 발송 관련 운영 알림

- 정책: 아래 상황은 Discord 오류 채널로 알립니다.
- 값: [FCM 발송 대기열 초과](dispatch 큐 초과), [FCM 발송 오류](클러스터 발송 중 예외), [FCM 발송 허가 대기 초과](300초 대기 초과), [FCM SENDER_ID_MISMATCH]
- 근거: orchestrator/FcmOrchestrator.java:65-71, orchestrator/FcmOrchestrator.java:82-87, orchestrator/FcmOrchestrator.java:153-160, service/webhook/FcmPushResultService.java:232-238

## 원문 표 1

| 풀 | core / max / queue | 하는 일 | 근거 |
| --- | --- | --- | --- |
| fcm-dispatch | 2 / 2 / 100 | 클러스터 발송 루프(세마포어 대기 담당). 큐가 차면 클러스터는 PENDING으로 남고 Discord 알림 | RES/config/fcm.yaml:32-38, config/FcmConfig.java:92-107 |
| fcm-default | 200 / 200 / 2000, 유휴 시 스레드 반납 | Firebase SDK 전송 스레드 | RES/config/fcm.yaml:23-31, config/FcmConfig.java:58-80, FcmInitializer.java:41-56 |
| fcm-callback | 4 / 4 / 100 | 발송 결과 DB 반영 | RES/config/fcm.yaml:16-22, config/FcmConfig.java:28-45 |
| app-scheduler | 4 | 모든 @Scheduled 작업 | RES/application.yaml:45-47, config/SchedulerConfig.java:19-27 |

## 원문 표 2

| 결과 | 토큰 상태 | 추가 처리 | 실패 수 반영 |
| --- | --- | --- | --- |
| 성공 | SUCCESS | - | 성공 +1 |
| INTERNAL, UNAVAILABLE | RETRY_PENDING, retry_after = now + 2^retry_count분 | retry_count +1 | 0 |
| QUOTA_EXCEEDED | RETRY_PENDING, 대기 시간 2배 | retry_count +1 | 0 |
| UNREGISTERED, INVALID_ARGUMENT | FAIL | tokens.is_deleted = true(소프트 삭제) | +1 |
| SENDER_ID_MISMATCH | PERMANENT_FAIL | Discord 오류 채널 알림 | +1 |
| 그 밖의 코드, 코드 없음 | FAIL | - | +1 |
| 배치 전체 예외(네트워크·타임아웃 등) | RETRY_PENDING | retry_count +1 | 0 |
