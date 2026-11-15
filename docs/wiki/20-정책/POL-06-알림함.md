---
type: 정책
status: 확정
source: policies.md 6장 · policies.json 6장
updated: 2026-11-15
tags: []
---

# 6장 알림함

원문: `policies.md` 6장. 각 항목의 정책·값·근거는 추출 JSON 원문 그대로다.

### POL-NOTI-01 알림함 기록 생성

- 정책: 클러스터를 만들 때 대상 회원마다 알림함 기록을 하나씩 만듭니다. 발송 성공 여부와 상관없이 남고, 같은 클러스터 안이면 활성 토큰이 없는 회원에게도 남습니다.
- 값: push_notifications — notification_type(TOPIC·KEYWORD), 클러스터의 title·body·image_url·click_url 복사, topic_id 또는 keyword_id, is_read=false, notified_at=생성 시각
- 근거: orchestrator/PushOrchestrator.java:33-45, service/notification/NotificationPushService.java:27-49, domain/notification/PushNotification.java:39-79

### POL-NOTI-02 알림 목록

- 정책: 토픽 알림과 키워드 알림을 따로 조회합니다. 최신순 고정이고 다음 페이지 여부만 알려 주는 방식(Slice)입니다.
- 값: GET /api/notification/topic, GET /api/notification/keyword, 기본 10개, notified_at 내림차순. 응답 {id, title(공지 제목), imageUrl, clickUrl, isRead, notifiedAt, topicName}(키워드는 keywordName 추가) + {hasPrevious, hasNext, currentPage, sort}
- 근거: controller/NotificationController.java:30-44, repository/adapter/notification/PushNotificationJpaRepositoryAdapter.java:32-36, dto/notification/TopicNotificationResponse.java, dto/notification/KeywordNotificationResponse.java, common/dto/SliceResponse.java

### POL-NOTI-03 읽음 처리

- 정책: 알림 하나를 누르면 그 알림만 읽음으로 바꾸고, 전체 읽음도 할 수 있습니다. 다른 회원의 알림은 찾을 수 없음으로 처리합니다.
- 값: POST /api/notification/click {pushNotificationId} → 204, 본인 알림이 아니거나 없으면 404 AE-NOTIFICATION-NOT-FOUND. POST /api/notification/readAll → 204. 읽음 시 is_read=true, clicked_at=now.
- 근거: controller/NotificationController.java:46-65, orchestrator/NotificationOrchestrator.java:35-47, domain/notification/PushNotification.java:81-84, repository/adapter/notification/PushNotificationJpaRepositoryAdapter.java:52-58

### POL-NOTI-04 읽지 않은 알림 수

- 정책: 토픽·키워드 구분 없이 회원의 읽지 않은 알림 전체 수를 돌려줍니다. 같은 값을 푸시 data의 unread_count로도 보냅니다.
- 값: GET /api/notification/unread-count → {count}
- 근거: service/notification/NotificationQueryService.java:39-41, repository/adapter/notification/PushNotificationJpaRepositoryAdapter.java:40

### POL-NOTI-05 보관 기간과 삭제

- 정책: 알림함 기록을 지우는 로직은 없습니다. 기한 없이 보관하고, 회원이 알림을 지우는 API도 없습니다.
- 값: 보관 기간 무기한
- 근거: 스케줄러 전체 목록(11장)에 정리 작업 없음, controller/NotificationController.java 전체(삭제 엔드포인트 없음)
