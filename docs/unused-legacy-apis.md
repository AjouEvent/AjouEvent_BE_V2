# 레거시 BE에서 구현됐지만 FE V1에서 사용하지 않는 API 목록

> 레거시 BE: `chore/ajou-event-v1` 브랜치의 `ajou-event-v1/` 디렉토리
> 레거시 FE: `chore/ajou-event-fe-v1` 브랜치의 `ajou-event-fe-v1/` 디렉토리
> 분석 기준일: 2026-03-15

---

## AlarmController

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/alarm` | 알림 생성 |

---

## WebhookController

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/webhook/crawling` | 크롤링 웹훅 처리 |

---

## FCMController

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/send/registeration-token` | FCM 등록 토큰 저장 |

---

## S3Controller

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/auth/image` | 이미지 업로드 |
| POST | `/api/file/multiple-presigned-urls` | 다중 Presigned URL 발급 |
| GET | `/api/file/{fileName}` | S3 파일 조회 |

> `GET /api/file/presigned-url/{fileName}` 은 FE에서 사용함 (제외)

---

## PushClusterController

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/push-cluster` | 푸시 클러스터 통계 조회 |
| POST | `/api/push-cluster/received` | 푸시 수신 카운트 증가 |
| POST | `/api/push-cluster/clicked` | 푸시 클릭 카운트 증가 |

---

## EventController

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/event/new` | 이벤트 생성 (multipart) — FE는 `/api/event/post` 사용 |
| PATCH | `/api/event/{eventId}` | 이벤트 데이터 수정 |
| PATCH | `/api/event/{eventId}/images` | 이벤트 이미지 수정 |
| DELETE | `/api/event/{eventId}` | 이벤트 삭제 |
| GET | `/api/event/popular` | 인기 이벤트 조회 |
| POST | `/api/event/addBanner` | 이벤트 배너 추가 |
| DELETE | `/api/event/deleteBanner/{eventBannerId}` | 이벤트 배너 삭제 |
| GET | `/api/event/test` | 테스트용 엔드포인트 |

---

## TopicController

| 메서드 | 경로 | 설명 |
|--------|------|------|
| DELETE | `/api/topic/subscriptions/reset` | 토픽 구독 초기화 |
| GET | `/api/topic/all` | 전체 토픽 목록 조회 |
| GET | `/api/topic/subscriptions` | 사용자 구독 토픽 조회 |
| POST | `/api/topic/subscriptions/notification` | 토픽별 알림 수신 설정 변경 |

> `GET /api/topic/subscriptionsStatus`, `POST /api/topic/subscribe`, `POST /api/topic/unsubscribe` 는 FE에서 사용함 (제외)

---

## KeywordController

| 메서드 | 경로 | 설명 |
|--------|------|------|
| DELETE | `/api/keyword/subscriptions/reset` | 키워드 구독 초기화 |

> `POST /api/keyword/subscribe`, `POST /api/keyword/unsubscribe`, `GET /api/keyword/userKeywords` 는 FE에서 사용함 (제외)

---

## MemberController

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/users/login` | 일반 로그인 — FE는 OAuth 로그인만 사용 |
| POST | `/api/users/register-info` | 회원가입 추가 정보 입력 |
| GET | `/api/users/emailExists` | 이메일 존재 여부 확인 |
| GET | `/api/users/accountExists` | 계정 존재 여부 확인 |
| POST | `/api/users/calendar` | Google Calendar 연동 — FE는 `/api/event/calendar` 사용 |