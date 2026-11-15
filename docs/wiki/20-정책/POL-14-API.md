---
type: 정책
status: 확정
source: policies.md 14장 · policies.json 14장
updated: 2026-11-15
tags: []
---

# 14장 주요 API 목록

원문: `policies.md` 14장. 각 항목의 정책·값·근거는 추출 JSON 원문 그대로다.

- 인증: 필수 = Bearer access token 필요, 선택 = 없어도 되고 있으면 회원 정보 반영, 없음 = 인증 없음
- 운영 API 38개 + local 전용 3개. 관리용 /actuator/health, /actuator/prometheus는 별도 포트 9090(RES/application.yaml:21-30).

## 원문 표 1

| 메서드 | 경로 | 인증 | 용도 | 근거 |
| --- | --- | --- | --- | --- |
| POST | /api/users/oauth | 없음 | Google 로그인(자동 가입) · 토큰 발급 | controller/AuthController.java:29 |
| PATCH | /api/users/reissue-token | refresh 쿠키 | access·refresh 토큰 재발급 | controller/AuthController.java:37 |
| POST | /api/users/logout | 없음 | refresh 쿠키 만료 | controller/AuthController.java:49 |
| GET | /api/users | 필수 | 내 정보 조회 | controller/MemberController.java:28 |
| PATCH | /api/users | 필수 | 이름·학과 수정 | controller/MemberController.java:33 |
| POST | /api/users/register-info | 필수 | 가입 후 학과 입력 | controller/MemberController.java:39 |
| DELETE | /api/users | 필수 | 회원 탈퇴 | controller/MemberController.java:46 |
| POST | /api/users/connect-calendar | 필수 | Google 캘린더 연동 | controller/MemberController.java:52 |
| POST | /send/registration-token | 필수 | FCM 토큰 등록·갱신 | controller/FcmTokenController.java:21 |
| GET | /api/topic/all | 없음 | 전체 토픽 목록 | controller/TopicController.java:29 |
| GET | /api/topic/subscriptions | 필수 | 구독 토픽 목록 | controller/TopicController.java:35 |
| POST | /api/topic/subscribe | 필수 | 토픽 구독 | controller/TopicController.java:41 |
| POST | /api/topic/unsubscribe | 필수 | 토픽 구독 해지 | controller/TopicController.java:48 |
| DELETE | /api/topic/subscriptions/reset | 필수 | 토픽 구독 전체 해지 | controller/TopicController.java:55 |
| GET | /api/topic/subscriptionsStatus | 필수 | 전체 토픽 + 구독·수신 상태 | controller/TopicController.java:62 |
| POST | /api/topic/subscriptions/notification | 필수 | 토픽 알림 수신 켜기·끄기 | controller/TopicController.java:68 |
| POST | /api/keyword/subscribe | 필수 | 키워드 구독 | controller/KeywordController.java:26 |
| POST | /api/keyword/unsubscribe | 필수 | 키워드 해지 | controller/KeywordController.java:35 |
| GET | /api/keyword/userKeywords | 필수 | 내 키워드 목록 | controller/KeywordController.java:44 |
| DELETE | /api/keyword/subscriptions/reset | 필수 | 키워드 전체 해지 | controller/KeywordController.java:49 |
| GET | /api/subscriptions/isSubscribedTabRead | 필수 | 구독 탭 읽음 여부 | controller/SubscriptionController.java:20 |
| GET | /api/event/{type} | 선택 | 토픽별 공지 목록·제목 검색 | controller/ClubEventController.java:49 |
| GET | /api/event/detail/{eventId} | 선택 | 공지 상세(조회수 집계) | controller/ClubEventController.java:37 |
| GET | /api/event/popular | 선택 | 이번 주 인기글 10개 | controller/ClubEventController.java:59 |
| GET | /api/event/subscribed | 선택 | 구독 토픽 피드(비로그인은 일반 공지) | controller/ClubEventController.java:65 |
| GET | /api/event/getSubscribedPostsByKeyword | 필수 | 구독 키워드 전체 게시글 | controller/ClubEventController.java:78 |
| GET | /api/event/getSubscribedPostsByKeyword/{keyword} | 필수 | 키워드 하나의 게시글(읽음 처리) | controller/ClubEventController.java:86 |
| POST | /api/event/like/{eventId} | 필수 | 찜하기 | controller/ClubEventController.java:95 |
| DELETE | /api/event/like/{eventId} | 필수 | 찜 취소 | controller/ClubEventController.java:104 |
| GET | /api/event/liked | 필수 | 찜 목록 | controller/ClubEventController.java:113 |
| GET | /api/event/banner | 없음 | 배너 목록 | controller/ClubEventController.java:123 |
| POST | /api/event/calendar | 필수 | Google 캘린더 일정 추가 | controller/ClubEventController.java:129 |
| GET | /api/notification/topic | 필수 | 토픽 알림함 | controller/NotificationController.java:31 |
| GET | /api/notification/keyword | 필수 | 키워드 알림함 | controller/NotificationController.java:39 |
| POST | /api/notification/click | 필수 | 알림 하나 읽음 | controller/NotificationController.java:47 |
| GET | /api/notification/unread-count | 필수 | 읽지 않은 알림 수 | controller/NotificationController.java:55 |
| POST | /api/notification/readAll | 필수 | 전체 읽음 | controller/NotificationController.java:61 |
| POST | /api/webhook/crawling | crawling-token 헤더 | 크롤러 공지 수신 · 푸시 생성 | controller/WebhookController.java:21 |
| POST | /api/v2/auth/test-login | 없음(local 전용) | 이메일로 즉시 로그인 | controller/TestAuthController.java:32 |
| POST | /api/v2/auth/test-login/fcm | 없음(local 전용) | 즉시 로그인 + FCM 토큰 등록 | controller/TestAuthController.java:41 |
| POST | /api/v2/auth/test-crawling-token | 없음(local 전용) | 크롤링 토큰 발급 | controller/TestAuthController.java:50 |
