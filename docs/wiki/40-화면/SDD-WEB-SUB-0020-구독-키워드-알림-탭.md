---
type: 화면
status: 확정
source: screens.json SDD-WEB-SUB-0020 · src/pages/subscribePage/SubscribePage.jsx, src/pages/subscribePage/KeywordTab.jsx, src/pages/subscribePage/KeywordBar.jsx
updated: 2026-11-15
tags: []
---

# SDD-WEB-SUB-0020 구독 - 키워드 알림 탭

- 경로: `/subscribe`
- 진입 조건·소스: src/pages/subscribePage/SubscribePage.jsx, src/pages/subscribePage/KeywordTab.jsx, src/pages/subscribePage/KeywordBar.jsx
- 하단 내비게이션: 표시
- 상단 제목: 구독
- 화면 분류: 구독 > 키워드 알림

## 배치

근거: `screens.json` SDD-WEB-SUB-0020 layout.

1. 구독 (header)
2. 아직 구독한 키워드가 없습니다. 아래의 🔔 키워드 설정에서 관심있는 키워드를 구독해보세요! (banner)
3. 탭 (키워드 알림 선택) (tabs) — 구독 알림 / 키워드 알림
4. 키워드 칩 가로 스크롤 (chips) — 🔔 키워드 설정 / 클릭해서 구독하기 (안내 말풍선) / 구독 키워드 칩 (예: 장학금) / 안 읽은 칩 빨간 점
5. 검색어를 입력해 주세요 (input)
6. 키워드 공지 카드 목록 (list) — 썸네일 / 제목 / 본문 미리보기 / 카테고리 태그 · 조회수 · 좋아요 수 / 하트 버튼
7. 서버 에러 (text)

## 구성 설명

근거: `screens.json` SDD-WEB-SUB-0020 descriptions.

### 2 [키워드 안내 배너]

- 구독 키워드가 0개일 때만 노출
- 이때 [키워드 설정] 버튼에 파란 테두리 강조

### 4 [키워드 설정 버튼]

- 선택 시 키워드 구독(/subscribe/keywordSubscribe)으로 이동

### 4 [키워드 칩]

- 선택 시 강조, 해당 키워드 글만 조회
- API: GET /api/event/getSubscribedPostsByKeyword/{키워드}
- 서버에서 해당 키워드 읽음 처리
- 같은 칩 다시 선택 시 해제 → 전체 키워드 글
- 선택 시 칩의 빨간 점 제거

### 6 [키워드 공지 목록]

- 칩 미선택: GET /api/event/getSubscribedPostsByKeyword
- 구독 키워드가 제목에 포함된 글, 최신순
- 10개씩 무한 스크롤, 10개 미만이면 끝
- 로딩 중 스켈레톤 3개
- 카드 선택 시 공지 상세로 이동

### 5 [검색창]

- 입력 후 검색 시 칩 선택이 풀리고 전체 키워드 글로 재조회
- 입력한 검색어로 걸러지지 않음 (코드상 필터 없음)

## 팝업

근거: `screens.json` SDD-WEB-SUB-0020 popups.

- 없음.

## API와 정책

- `GET /api/event/getSubscribedPostsByKeyword` — [[API-목록]]
- `GET /api/event/getSubscribedPostsByKeyword/{키워드}` — [[API-목록]]

관련 정책: [[POL-02-구독-토픽]] · [[API-목록]]
