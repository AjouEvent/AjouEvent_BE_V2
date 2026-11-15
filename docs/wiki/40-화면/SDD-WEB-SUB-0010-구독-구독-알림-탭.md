---
type: 화면
status: 확정
source: screens.json SDD-WEB-SUB-0010 · src/pages/subscribePage/SubscribePage.jsx, src/pages/subscribePage/SubscribeTab.jsx, src/pages/subscribePage/SubscribeBar.jsx, src/pages/subscribePage/SubscribeStatusDropdown.jsx, src/pages/subscribePage/SubscribeEvent.jsx, src/store/useSubscriptionStore.js
updated: 2026-11-15
tags: []
---

# SDD-WEB-SUB-0010 구독 - 구독 알림 탭

- 경로: `/subscribe`
- 진입 조건·소스: src/pages/subscribePage/SubscribePage.jsx, src/pages/subscribePage/SubscribeTab.jsx, src/pages/subscribePage/SubscribeBar.jsx, src/pages/subscribePage/SubscribeStatusDropdown.jsx, src/pages/subscribePage/SubscribeEvent.jsx, src/store/useSubscriptionStore.js
- 하단 내비게이션: 표시
- 상단 제목: 구독
- 화면 분류: 구독 > 구독 알림

## 배치

근거: `screens.json` SDD-WEB-SUB-0010 layout.

1. 구독 (header)
2. 아직 구독한 항목이 없습니다. 아래의 ⚙️ 구독 설정에서 관심있는 공지를 구독해보세요! (banner)
3. 탭 (tabs) — 구독 알림 / 키워드 알림
4. 구독 칩 가로 스크롤 (chips) — ⚙ 구독 설정 / 클릭해서 구독하기 (안내 말풍선) / 구독한 게시판 칩 (예: 소프트웨어학과) / 안 읽은 칩 빨간 점
5. 검색어를 입력해 주세요 (input)
6. 구독 공지 카드 목록 (list) — 썸네일 / 제목 / 본문 미리보기 / 카테고리 태그 · 조회수 · 좋아요 수 / 하트 버튼
7. 불러올 이벤트가 없습니다. (empty)
8. 비로그인: 로그인이 필요한 서비스입니다 + [로그인] (empty)

## 구성 설명

근거: `screens.json` SDD-WEB-SUB-0010 descriptions.

### 2 [구독 안내 배너]

- 구독한 게시판이 0개일 때만 노출
- 이때 [구독 설정] 버튼에 파란 테두리 강조

### 3 [탭]

- 기본 탭: 구독 알림 (마지막 선택 탭을 세션에 기억)
- 탭에 안 읽은 항목 있으면 빨간 점 노출
- 진입 시 GET /api/keyword/userKeywords 조회

### 4 [구독 설정 버튼]

- 선택 시 '전체 구독 항목' 팝업 노출
- API: GET /api/topic/subscriptionsStatus

### 4 [구독 게시판 칩]

- API: GET /api/topic/subscriptions
- 선택 시 파란색으로 강조, 해당 게시판 글만 조회
- API: GET /api/event/{게시판코드}?page&size=10&keyword
- 같은 칩 다시 선택 시 해제 → 전체 구독 글
- 선택 시 해당 칩 읽음 처리(빨간 점 제거)

### 5 [검색창]

- 현재 목록(전체 또는 선택 칩) 안에서 제목 검색
- 검색어는 앱 내에서 유지

### 6 [구독 공지 목록]

- 칩 미선택: GET /api/event/subscribed?page&size=10&keyword
- 구독한 모든 게시판 글을 최신순으로 표시
- 10개씩 무한 스크롤, 로딩 중 스켈레톤 3개
- 카드 선택 시 공지 상세로 이동
- 조회 실패 시 '서버 에러' 노출

### 8 [비로그인 상태]

- 토큰이 없으면 탭·목록 대신 안내 문구
- [로그인] 선택 시 /login으로 이동

## 팝업

근거: `screens.json` SDD-WEB-SUB-0010 popups.

### 4-1 전체 구독 항목

- 본문: 분류 아코디언(학과 / 단과대 / 공지사항 / 기숙사 / 대학원), 펼치면 게시판명 + [구독] 또는 [🔔 구독중 ▾]. 분류 안은 가나다 순서(koreanOrder). [구독] 선택 시 '{게시판} 구독 중' 안내 토스트 → POST /api/topic/subscribe → 목록 갱신, 종 흔들림 효과. 실패 시 '구독 실패/서버 에러'
- 버튼: 구독 / 구독중 ▾

### 4-2 구독중 드롭다운

- 본문: 알림 받기(✓) / 알림 없음 / 구독 취소(빨간색). 알림 받기·없음: POST /api/topic/subscriptions/notification → '알림 설정 변경 완료'. 구독 취소: POST /api/topic/unsubscribe → '구독 취소 완료 / {게시판} 구독을 취소했습니다.' 알림 꺼짐이면 버튼이 회색·종 꺼짐 아이콘
- 버튼: 알림 받기 / 알림 없음 / 구독 취소

## API와 정책

- `GET /api/event/subscribed?page&size=10&keyword` — [[API-목록]]
- `GET /api/event/{게시판코드}?page&size=10&keyword` — [[API-목록]]
- `GET /api/keyword/userKeywords` — [[API-목록]]
- `GET /api/topic/subscriptions` — [[API-목록]]
- `GET /api/topic/subscriptionsStatus` — [[API-목록]]
- `POST /api/topic/subscribe` — [[API-목록]]
- `POST /api/topic/subscriptions/notification` — [[API-목록]]
- `POST /api/topic/unsubscribe` — [[API-목록]]

관련 정책: [[POL-02-구독-토픽]] · [[API-목록]]
