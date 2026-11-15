---
type: 정책
status: 확정
source: policies.md 12장 · policies.json 12장
updated: 2026-11-15
tags: []
---

# 12장 에러 코드 목록

원문: `policies.md` 12장. 각 항목의 정책·값·근거는 추출 JSON 원문 그대로다.

- 오류 응답 본문은 {code, message}입니다(common/exception/ErrorResponse.java:3-8).
- 공통 처리: 서비스 예외 → 해당 코드(common/exception/GlobalExceptionHandler.java:16-22), @Valid 실패·IllegalArgumentException → 400 AE-COMMON-VALIDATION-ERROR(:24-37), refresh 쿠키 없음 → 401 AE-AUTH-UNAUTHORIZED(:39-44), 그 밖의 모든 예외(잘못된 JSON, 필수 헤더 없음, 경로 변수 형식 오류 포함) → 500 AE-COMMON-INTERNAL-FAILURE(:46-51).

## 원문 표 1

| 코드 | HTTP | 메시지 | 발생 위치(예) | 근거 |
| --- | --- | --- | --- | --- |
| AE-AUTH-UNAUTHORIZED | 401 | 인증이 필요합니다. | 비로그인으로 @AuthUser API 호출, 회원 없음, refresh 쿠키 없음 | common/exception/auth/AuthErrorCode.java:11 |
| AE-AUTH-INVALID-TOKEN | 401 | 유효하지 않은 토큰입니다. | 서명 오류, issuer 불일치 | common/exception/auth/AuthErrorCode.java:12 |
| AE-AUTH-EXPIRED-TOKEN | 401 | 토큰이 만료되었습니다. | 만료된 JWT | common/exception/auth/AuthErrorCode.java:13 |
| AE-AUTH-UNSUPPORTED-TOKEN | 401 | 지원하지 않는 토큰입니다. | 지원하지 않는 JWT 형식 | common/exception/auth/AuthErrorCode.java:14 |
| AE-AUTH-ILLEGAL-ARGUMENT-TOKEN | 401 | 토큰 값이 올바르지 않습니다. | 빈 JWT | common/exception/auth/AuthErrorCode.java:15 |
| AE-AUTH-INVALID-AUTHORIZATION-CODE | 400 | 잘못된 인가 코드입니다. | Google 인가 코드 교환 실패 | common/exception/auth/AuthErrorCode.java:17 |
| AE-AUTH-CALENDAR-NOT-CONNECTED | 400 | Google 캘린더가 연동되지 않았습니다. | 캘린더 연동·일정 추가 실패 전부 | common/exception/auth/AuthErrorCode.java:18 |
| AE-AUTH-FORBIDDEN | 403 | 접근 권한이 없습니다. | 권한 부족(현재 ADMIN 전용 API 없음) | common/exception/auth/AuthErrorCode.java:20 |
| AE-EVENT-NOT-FOUND | 404 | 존재하지 않는 공지사항입니다. | 상세·찜 대상 공지 없음 | common/exception/clubevent/ClubEventErrorCode.java:11 |
| AE-EVENT-NOT-LIKED | 404 | 찜하지 않은 공지사항입니다. | 찜 취소 시 기록 없음 | common/exception/clubevent/ClubEventErrorCode.java:12 |
| AE-EVENT-ALREADY-LIKED | 409 | 이미 찜한 공지사항입니다. | 중복 찜 | common/exception/clubevent/ClubEventErrorCode.java:13 |
| AE-EVENT-DUPLICATE-NOTICE | 409 | 이미 처리된 공지사항입니다. | 웹훅 중복 공지 | common/exception/clubevent/ClubEventErrorCode.java:14 |
| AE-EVENT-INVALID-TYPE | 400 | 유효하지 않은 공지사항 타입입니다. | 웹훅 englishTopic 오류 | common/exception/clubevent/ClubEventErrorCode.java:15 |
| AE-COMMON-INTERNAL-FAILURE | 500 | 서버 내부 오류입니다. | 처리되지 않은 예외 | common/exception/common/CommonErrorCode.java:11 |
| AE-COMMON-VALIDATION-ERROR | 400 | 입력값이 올바르지 않습니다. | 검증 실패, IllegalArgumentException | common/exception/common/CommonErrorCode.java:12 |
| AE-COMMON-THROTTLING | 429 | 요청이 너무 많습니다. | 사용처 없음 | common/exception/common/CommonErrorCode.java:13 |
| AE-COMMON-SERVICE-UNAVAILABLE | 503 | 서비스를 사용할 수 없습니다. | 사용처 없음 | common/exception/common/CommonErrorCode.java:14 |
| AE-DISCORD-WEBHOOK-FAILED | 502 | Discord 웹훅 전송에 실패했습니다. | Discord 전송 실패 | common/exception/discord/DiscordErrorCode.java:11 |
| AE-KEYWORD-NOT-FOUND | 404 | 존재하지 않는 키워드입니다. | 키워드 해지·단일 키워드 목록 | common/exception/keyword/KeywordErrorCode.java:11 |
| AE-KEYWORD-ALREADY-SUBSCRIBED | 409 | 이미 구독 중인 키워드입니다. | 중복 키워드 구독 | common/exception/keyword/KeywordErrorCode.java:12 |
| AE-KEYWORD-MAX-SUBSCRIPTION-LIMIT | 400 | 키워드 구독은 최대 10개까지 가능합니다. | 11번째 키워드 | common/exception/keyword/KeywordErrorCode.java:13 |
| AE-MEMBER-NOT-FOUND | 404 | 존재하지 않는 사용자입니다. | 재발급 시 회원 없음 | common/exception/member/MemberErrorCode.java:11 |
| AE-NOTIFICATION-NOT-FOUND | 404 | 존재하지 않는 알림입니다. | 알림 읽음 처리 대상 없음 | common/exception/notification/NotificationErrorCode.java:11 |
| AE-PUSH-CLUSTER-NOT-FOUND | 404 | PushCluster를 찾을 수 없습니다. | 발송 스레드 내부(응답으로 나가지 않음) | common/exception/push/PushErrorCode.java:11 |
| AE-TOPIC-NOT-FOUND | 404 | 존재하지 않는 토픽입니다. | 구독·키워드 등록, 웹훅의 한국어 토픽명 조회 | common/exception/topic/TopicErrorCode.java:11 |
| AE-TOPIC-ALREADY-SUBSCRIBED | 409 | 이미 구독 중인 토픽입니다. | 중복 토픽 구독 | common/exception/topic/TopicErrorCode.java:12 |
| AE-TOPIC-SUBSCRIPTION-NOT-FOUND | 404 | 구독 정보를 찾을 수 없습니다. | 미구독 토픽의 수신 설정 변경 | common/exception/topic/TopicErrorCode.java:13 |
| AE-WEBHOOK-INVALID-TOKEN | 401 | 유효하지 않은 크롤링 토큰입니다. | 웹훅 토큰 불일치 | common/exception/webhook/WebhookErrorCode.java:11 |
| AE-WEBHOOK-PROCESSING-FAILED | 400 | Webhook 처리 중 오류가 발생했습니다. | 사용처 없음 | common/exception/webhook/WebhookErrorCode.java:12 |
