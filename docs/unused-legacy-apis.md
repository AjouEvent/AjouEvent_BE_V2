# 레거시 BE에서 구현됐지만 FE V1에서 사용하지 않는 API 목록

> 레거시 BE: `chore/ajou-event-v1` 브랜치의 `ajou-event-v1/` 디렉토리
> 레거시 FE: `chore/ajou-event-fe-v1` 브랜치의 `ajou-event-fe-v1/` 디렉토리
> 분석 기준일: 2026-03-19 (FE 코드 전체 대조 완료)

---

## MemberController

| 메서드 | 경로 | 미사용 이유 |
|--------|------|------------|
| PATCH | `/api/users` | 회원 정보 수정 기능 FE 미구현 |
| GET | `/api/users/emailExists` | FE는 `/api/users/duplicateEmail` 사용 |
| POST | `/api/users/verify-current-password` | FE는 `/api/users/login`을 비밀번호 검증 용도로 대신 사용 |
| POST | `/api/users/calendar` | FE는 `/api/event/calendar` 사용 |
| PATCH | `/api/users/change-password` | FE는 `/api/users/reset-password` 사용 |
| POST | `/api/users/reissue-password` | FE 호출 코드 없음 |

---

## EventController

| 메서드 | 경로 | 미사용 이유 |
|--------|------|------------|
| POST | `/api/event/new` | FE는 `/api/event/post` 사용 (서버 측 이미지 변환 방식 미사용) |
| GET | `/api/event/all` | FE는 `/{type}`, `/subscribed` 사용 |
| PATCH | `/api/event/{eventId}` | FE 호출 코드 없음 |
| PATCH | `/api/event/{eventId}/images` | FE 호출 코드 없음 |
| DELETE | `/api/event/{eventId}` | FE 호출 코드 없음 |
| POST | `/api/event/addBanner` | FE 호출 코드 없음 |
| DELETE | `/api/event/deleteBanner/{eventBannerId}` | FE 호출 코드 없음 |
| GET | `/api/event/test` | 테스트용 엔드포인트 |

---

## TopicController

| 메서드 | 경로 | 미사용 이유 |
|--------|------|------------|
| GET | `/api/topic/all` | FE 호출 코드 없음 |

---

## FCMController

| 메서드 | 경로 | 미사용 이유 |
|--------|------|------------|
| POST | `/send/registeration-token` | FE는 Firebase SDK로 FCM 토큰을 직접 획득하여 `/api/users/oauth` body에 포함 |

---

## S3Controller

| 메서드 | 경로 | 미사용 이유 |
|--------|------|------------|
| POST | `/api/auth/image` | FE 호출 코드 없음 |
| GET | `/api/file/presigned-url/{fileName}` | FE 호출 코드 없음 |
| GET | `/api/file/{fileName}` | FE 호출 코드 없음 |

---

## PushClusterController

| 메서드 | 경로 | 미사용 이유 |
|--------|------|------------|
| GET | `/api/push-cluster` | FE 호출 코드 없음 |
| POST | `/api/push-cluster/received` | FE 호출 코드 없음 |
| POST | `/api/push-cluster/clicked` | FE 호출 코드 없음 |

---

## AlarmController

| 메서드 | 경로 | 미사용 이유 |
|--------|------|------------|
| POST | `/api/alarm` | FE 호출 코드 없음 |

---

## WebhookController

| 메서드 | 경로 | 미사용 이유 |
|--------|------|------------|
| POST | `/api/webhook/crawling` | 서버 간 호출 전용 (FE 호출 아님) |
