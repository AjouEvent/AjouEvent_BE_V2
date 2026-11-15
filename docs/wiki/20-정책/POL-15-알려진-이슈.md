---
type: 정책
status: 확정
source: policies.md 15장 · policies.json 15장
updated: 2026-11-15
tags: []
---

# 15장 알려진 이슈

원문: `policies.md` 15장. 각 항목의 정책·값·근거는 추출 JSON 원문 그대로다.

- 코드를 읽고 찾은 문제입니다. 「[확인]」은 코드나 운영 측정으로 확인한 것, 「[추정]」은 코드로 보아 그렇게 동작할 것으로 판단한 것입니다.

### 이슈 01 [추정] 알림을 한 번이라도 받은 회원은 탈퇴가 실패합니다.

- 내용: 탈퇴는 members 행을 지우는데, push_notifications.member_id와 push_cluster_tokens.member_id가 cascade 없는 외래 키로 회원을 가리키고 JPA cascade 대상에도 없습니다. 외래 키 위반 예외는 500으로 응답됩니다.
- 근거: service/member/MemberService.java:71-75, domain/member/Member.java:49-60, RES/schema.sql:172, RES/schema.sql:194, common/exception/GlobalExceptionHandler.java:46-51

### 이슈 02 [확인: 코드 불일치 · 운영 DB 미확인] 크롤러와 백엔드의 토픽 이름이 달라 「미래모빌리티공학과」 공지는 푸시가 나가지 않을 수 있습니다.

- 내용: 크롤러는 koreanTopic="AI모빌리티공학과"를 보내는데, 백엔드 Type enum과 시드의 토픽명은 「미래모빌리티공학과」입니다. 웹훅은 공지를 저장한 뒤 한국어 이름으로 토픽을 찾으므로, 운영 DB도 시드와 같다면 404 AE-TOPIC-NOT-FOUND가 나고 공지만 저장된 채 푸시·알림함 기록이 생기지 않습니다(크롤러는 다시 보내지 않음). 운영 topics.korean_topic 값은 확인하지 않았습니다.
- 근거: CR/config/notifierConfigs.json:4-5, domain/clubevent/Type.java:10, RES/data.sql:2, orchestrator/WebhookOrchestrator.java:28-32, orchestrator/PushOrchestrator.java:31, service/topic/TopicQueryService.java:27-30

### 이슈 03 [확인] 구독 탭 「안 읽음」 표시가 새 공지에 다시 켜지지 않습니다.

- 내용: topic_members.is_read·keyword_members.is_read를 false로 바꾸는 코드는 구독 생성 시점뿐입니다. 새 공지가 와도 그대로라, 한 번 읽음이 된 구독은 계속 읽음으로 보입니다.
- 근거: service/topic/TopicCommandService.java:34-41, domain/keyword/KeywordMember.java:46-58, domain/topic/TopicMember.java:55-58, orchestrator/SubscriptionOrchestrator.java:28-32(웹훅·푸시 경로에 is_read=false 갱신 없음)

### 이슈 04 [확인] 키워드 푸시와 키워드 목록의 매칭 기준이 다릅니다.

- 내용: 푸시는 제목 또는 본문에 키워드가 있으면 보내지만, 키워드 탭 목록은 제목만 봅니다. 본문으로만 매칭돼 푸시를 받은 공지는 키워드 탭에 나오지 않습니다.
- 근거: service/keyword/KeywordQueryService.java:24-32, repository/adapter/clubevent/ClubEventJpaRepositoryAdapter.java:109-121, service/clubevent/ClubEventQueryService.java:145-150

### 이슈 05 [확인] 한 공지에 같은 회원이 여러 번 푸시를 받을 수 있습니다.

- 내용: 토픽 푸시와 키워드 푸시, 여러 키워드 푸시 사이에 회원 단위 중복 제거가 없습니다. 토픽 알림을 꺼도 키워드 푸시는 계속 옵니다.
- 근거: orchestrator/PushOrchestrator.java:33-46, service/push/PushClusterCommandService.java:80

### 이슈 06 [확인] 같은 FCM 토큰을 다른 계정으로 등록하면 500이 납니다.

- 내용: 토큰 값은 전체에서 유일한데, 등록 시 「같은 회원 + 같은 토큰」만 찾습니다. 한 기기에서 계정을 바꿔 로그인하면 새 행을 넣다가 유일 키 위반이 납니다. 로그인 요청에 fcmToken이 있으면 로그인 자체가 실패합니다.
- 근거: service/token/FcmTokenCommandService.java:28-48, RES/create-indexes.sql:13, orchestrator/AuthOrchestrator.java:49-51

### 이슈 07 [확인] 한 번 소프트 삭제된 토큰은 다시 등록해도 살아나지 않습니다.

- 내용: 같은 회원이 같은 토큰을 다시 등록하면 만료일만 갱신하고 is_deleted는 true로 남습니다. INVALID_ARGUMENT는 페이로드 문제로도 날 수 있는데 이때도 토큰을 지웁니다.
- 근거: service/token/FcmTokenCommandService.java:30-36, domain/member/Token.java:46-52, service/webhook/FcmPushResultService.java:226-230

### 이슈 08 [확인] refresh token과 access token을 구분하지 않습니다.

- 내용: 두 토큰이 같은 키·같은 issuer이고 종류 클레임이 없어서, refresh token을 Bearer 헤더에 넣어도 인증되고 access token을 refresh 쿠키로 써도 재발급됩니다. 로그아웃·재발급 시 이전 토큰을 무효로 만드는 저장소도 없습니다.
- 근거: common/util/JwtUtil.java:32-63, common/auth/JwtAuthFilter.java:35-45, orchestrator/AuthOrchestrator.java:61-65, controller/AuthController.java:49-54

### 이슈 09 [확인] 비인증 허용 경로에서도 잘못된 Bearer 헤더가 있으면 401입니다.

- 내용: 예를 들어 만료된 access token을 헤더에 남긴 채 PATCH /api/users/reissue-token을 호출하면 필터가 먼저 401 AE-AUTH-EXPIRED-TOKEN을 돌려줘 재발급이 되지 않습니다.
- 근거: common/auth/JwtAuthFilter.java:33-55, config/SecurityConfig.java:44-54

### 이슈 10 [확인] 웹훅 처리 단계가 하나의 트랜잭션이 아닙니다.

- 내용: 공지 저장, 클러스터 생성, 알림함 저장이 각각 커밋됩니다. 주석은 「원자적으로 저장」이라고 적었지만, 중간에 실패하면 공지만 저장되거나(이슈 2) 알림함 기록 없이 클러스터만 남아 복구 작업이 발송할 수 있습니다.
- 근거: orchestrator/WebhookOrchestrator.java:28-35, orchestrator/PushOrchestrator.java:28-50(@Transactional 없음), service/push/PushClusterCommandService.java:44, service/notification/NotificationPushService.java:27

### 이슈 11 [확인] 금요일 21:55 이후 실패한 푸시는 월요일 09:00까지 다시 보내지 않습니다.

- 내용: 즉시 발송은 24시간 하지만 복구 작업은 평일 09~21시에만 돕니다. 주말·야간 서버 재시작으로 멈춘 발송도 같습니다.
- 근거: scheduler/PushPollingPublisherScheduler.java:30

### 이슈 12 [추정] Discord 전송 실패가 발송 결과 반영을 되돌릴 수 있습니다.

- 내용: DiscordMessageService는 실패 시 예외를 던집니다. SENDER_ID_MISMATCH 처리는 결과 반영 트랜잭션 안에서 Discord를 호출하므로, Discord가 실패하면 그 배치의 결과 반영이 롤백되고 토큰이 IN_PROGRESS로 남아 10분 뒤 다시 발송됩니다. 발송 대기열 초과 알림이 실패하면 남은 클러스터 제출이 중단되고 웹훅이 502로 끝납니다.
- 근거: common/discord/DiscordMessageService.java:20-27, service/webhook/FcmPushResultService.java:48-77, service/webhook/FcmPushResultService.java:232-238, orchestrator/FcmOrchestrator.java:61-73

### 이슈 13 [추정] 캘린더 일정 추가가 날짜만 넣으면 실패할 가능성이 큽니다.

- 내용: 요청 예시는 yyyy-MM-dd인데 이 값을 Google Calendar의 start.dateTime·end.dateTime에 그대로 넣습니다. Google은 dateTime에 시각·시간대가 있는 RFC3339 값을, 종일 일정은 date 필드를 요구합니다. 실패는 모두 AE-AUTH-CALENDAR-NOT-CONNECTED로 보여서 원인을 구분할 수 없습니다.
- 근거: dto/clubevent/CalendarRequest.java:13-17, service/calendar/CalendarCommandService.java:156-161, service/calendar/CalendarCommandService.java:172-175

### 이슈 14 [확인] 캘린더 refresh token을 서버 파일에 평문으로 두고, 탈퇴해도 지우지 않습니다.

- 내용: 원문에 별도 설명 없음
- 근거: service/calendar/CalendarCommandService.java:88-101, service/member/MemberService.java:71-75

### 이슈 15 [확인] 배너가 기간과 상관없이 노출되고, 바꿔도 최대 24시간 반영되지 않습니다.

- 내용: 서버는 start_date·end_date로 거르지 않고, 배너 캐시는 무효화 경로가 없습니다.
- 근거: repository/port/clubevent/EventBannerRepositoryPort.java:28-30, service/clubevent/ClubEventQueryService.java:152-159, repository/adapter/clubevent/ClubEventCacheAdapter.java:72

### 이슈 16 [확인] Redis 데이터가 사라지면 다음 날 05:30까지 웹훅이 모두 401입니다.

- 내용: 크롤링 토큰은 05:30 스케줄러만 만들고 기동 시에는 만들지 않습니다. 크롤러는 Redis에 키가 없으면 log.Fatalf로 종료합니다. 운영 Redis는 AOF를 켜 두어 재시작만으로는 사라지지 않습니다.
- 근거: scheduler/CrawlingTokenScheduler.java:18-22, CR/src/utils/utils.go:48-51, BE/deploy/docker-compose.yml:58

### 이슈 17 [확인] 크롤러는 웹훅 실패 시 공지를 영구히 잃습니다.

- 내용: 저장값을 먼저 올리고 나서 웹훅을 보내며 재시도가 없습니다. 웹훅 오류는 ErrorLogger.Panic입니다. 그 밖에 지우고 다시 올린 글·고정 공지 교체는 새 글로 잡지 못합니다(노트 5.2).
- 근거: CR/src/notifiers/BaseNotifier.go:203-210, CR/src/notifiers/BaseNotifier.go:61-64, CR/src/utils/utils.go:210-214, CR/src/notifiers/BaseNotifier.go:123-135, CR/src/notifiers/BaseNotifier.go:172-184

### 이슈 18 [확인] 소프트웨어학과 게시판을 수집하지 못합니다.

- 내용: 설정 주소 http://software.ajou.ac.kr/bbs/board.php?tbl=notice가 404이고(2026-11-13 운영 로그 3시간에 crawl_page_load_failed 180회), 새 주소는 구조가 달라 주소만 바꿔서는 동작하지 않습니다. 실패해도 운영 알림이 가지 않습니다.
- 근거: CR/config/notifierConfigs.json:300, 노트 4.1~4.2

### 이슈 19 [확인] 크롤러의 별도 고루틴 패닉과 겹침 실행.

- 내용: 상세 페이지 수집 고루틴의 범위 초과 패닉은 Notify의 recover로 잡히지 않아 프로세스가 종료될 수 있고, 매 주기 앞 실행이 끝났는지 보지 않아 같은 게시판이 겹쳐 돌 수 있습니다.
- 근거: CR/src/notifiers/BaseNotifier.go:43-47, CR/src/notifiers/BaseNotifier.go:145-147, CR/main.go:47-54, 노트 9.1

### 이슈 20 [확인] 알림함·발송 기록이 계속 쌓입니다.

- 내용: push_notifications, push_clusters, push_cluster_tokens를 지우는 작업이 없습니다.
- 근거: 11장 스케줄러 목록(정리 작업 없음)

### 이슈 21 [확인] 잘못된 요청 형식이 500으로 응답됩니다.

- 내용: 깨진 JSON, crawling-token 헤더 누락, 숫자가 아닌 eventId 같은 Spring 예외를 따로 처리하지 않아 모두 500 AE-COMMON-INTERNAL-FAILURE가 됩니다.
- 근거: common/exception/GlobalExceptionHandler.java:46-51

### 이슈 22 [확인] 이메일 도메인 제한이 없어 아주대 계정이 아닌 Google 계정도 가입됩니다.

- 내용: 원문에 별도 설명 없음
- 근거: service/member/MemberService.java:24-35

### 이슈 23 [확인] 탈퇴해도 공지의 찜 수가 줄지 않고, 찜 수에 하한이 없습니다.

- 내용: 탈퇴 시 event_likes만 cascade로 지우고 likes_count는 그대로입니다. 찜 확인과 저장이 원자적이지 않다는 TODO도 코드에 남아 있습니다(DB 유일 키가 있어 중복 행은 막힘).
- 근거: domain/member/Member.java:52-53, domain/clubevent/ClubEvent.java:76-82, service/clubevent/ClubEventLikeCommandService.java:21, RES/create-indexes.sql:77

### 이슈 24 [확인] 테스트 토픽이 사용자에게 보입니다.

- 내용: TEST(「테스트」) 토픽이 전체 토픽 목록과 구독 상태 목록에 나오고 구독할 수 있습니다. 시드의 korean_order 55가 「행정학과」·「첨단바이오융합대학」에 중복되고, 「첨단바이오융합대학」의 분류가 「학과」로 들어가 있습니다.
- 근거: RES/data.sql:55, RES/data.sql:48, RES/data.sql:60, orchestrator/TopicOrchestrator.java:28-33

### 이슈 25 [확인] 키워드 전체 목록의 페이지 크기가 일정하지 않습니다.

- 내용: 토픽마다 size만큼 가져와 합치므로 키워드가 여러 토픽에 걸쳐 있으면 한 페이지에 size × 토픽 수까지 담기고, hasNext는 한 토픽이라도 다음이 있으면 true입니다.
- 근거: orchestrator/ClubEventOrchestrator.java:191-214

### 이슈 26 [확인] FCM 토큰 만료일(10주)을 기록만 하고 쓰지 않습니다.

- 내용: 발송 대상은 is_deleted만 보며, topic_tokens·keyword_tokens 매핑도 갱신만 하고 발송 대상 계산에는 쓰지 않습니다.
- 근거: service/token/FcmTokenCommandService.java:19, repository/port/token/TokenRepositoryPort.java:23-25, repository/port/token/TokenRepositoryPort.java:55-57, service/push/PushClusterCommandService.java:48-57
