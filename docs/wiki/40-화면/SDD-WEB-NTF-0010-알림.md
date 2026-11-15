---
type: 화면
status: 확정
source: screens.json SDD-WEB-NTF-0010 · src/pages/notificationPage/NotificationPage.jsx, src/pages/notificationPage/NotificationList.jsx, src/hooks/usePagination.js, src/components/EventCard.jsx
updated: 2026-11-15
tags: []
---

# SDD-WEB-NTF-0010 알림

- 경로: `/notification`
- 진입 조건·소스: src/pages/notificationPage/NotificationPage.jsx, src/pages/notificationPage/NotificationList.jsx, src/hooks/usePagination.js, src/components/EventCard.jsx
- 하단 내비게이션: 숨김
- 상단 제목: 알림
- 화면 분류: 홈 > 알림

## 배치

근거: `screens.json` SDD-WEB-NTF-0010 layout.

1. ← 알림  [모두 읽음] (header)
2. 탭 (tabs) — 구독 알림 / 키워드 알림
3. 알림 카드 목록 (list) — 썸네일 / 🔔 게시판명 · 키워드 배지 / 제목 (2줄) / n분 전 / n시간 전 / n일 전 / 안 읽은 알림은 연파랑 배경
4. 알림 불러오는 중... (text)
5. 알림이 없습니다 (empty)
6. 알림을 불러오지 못했습니다. (text)

## 구성 설명

근거: `screens.json` SDD-WEB-NTF-0010 descriptions.

### 1 [뒤로가기]

- 이전 기록이 있으면 뒤로, 없으면 /event로 이동

### 1 [모두 읽음]

- 선택 시 확인 다이얼로그 노출
- 확인 시 POST /api/notification/readAll
- 성공 시 '읽음 처리 완료' 토스트, 목록 새로 조회
- 실패 시 '오류' 토스트

### 2 [탭]

- 기본: 구독 알림
- 구독 알림: GET /api/notification/topic?page&size=10
- 키워드 알림: GET /api/notification/keyword?page&size=10

### 3 [알림 카드]

- 10개씩 무한 스크롤, 첫 로딩 시 스켈레톤 3개
- 키워드 알림은 배지에 '· 키워드' 추가
- 1분 미만은 '방금 전'
- 선택 시 즉시 읽음 표시 후 알림 링크 경로로 이동
- 이동한 상세 화면에서 POST /api/notification/click 호출

### 5 [빈 상태]

- 알림 0건이면 종 아이콘과 함께 노출

## 팝업

근거: `screens.json` SDD-WEB-NTF-0010 popups.

### 1-1 알림 읽음 처리

- 본문: 정말 모든 알림을 읽음 처리할까요?
- 버튼: 취소 / 확인

### 1-2 읽음 처리 완료

- 본문: 모든 알림을 읽음 처리했습니다.
- 버튼: 

### 1-3 오류

- 본문: 알림 읽음 처리 중 오류가 발생했습니다.
- 버튼: 

## API와 정책

- `GET /api/notification/keyword?page&size=10` — [[API-목록]]
- `GET /api/notification/topic?page&size=10` — [[API-목록]]
- `POST /api/notification/click` — [[API-목록]]
- `POST /api/notification/readAll` — [[API-목록]]

관련 정책: [[POL-06-알림함]] · [[API-목록]]
