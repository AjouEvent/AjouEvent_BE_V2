---
type: 화면
status: 확정
source: screens.json SDD-WEB-EVT-0030 · src/pages/likedPage/LikedEventPage.jsx, src/components/layout/SearchBar.jsx, src/components/EventCard.jsx
updated: 2026-11-15
tags: []
---

# SDD-WEB-EVT-0030 찜 목록

- 경로: `/liked`
- 진입 조건·소스: src/pages/likedPage/LikedEventPage.jsx, src/components/layout/SearchBar.jsx, src/components/EventCard.jsx
- 하단 내비게이션: 표시
- 상단 제목: 내가 찜한 이벤트
- 화면 분류: 공지 > 찜

## 배치

근거: `screens.json` SDD-WEB-EVT-0030 layout.

1. 내가 찜한 이벤트 (header)
2. 검색어를 입력해 주세요 (input)
3. 찜한 공지 카드 목록 (list) — 썸네일 / 제목 / 본문 미리보기 / 카테고리 태그 · 조회수 · 좋아요 수 / 채운 하트
4. 불러올 이벤트가 없습니다. (empty)
5. 비로그인: 로그인이 필요한 서비스입니다 + [로그인] (empty)

## 구성 설명

근거: `screens.json` SDD-WEB-EVT-0030 descriptions.

### 2 [검색창]

- 돋보기 선택 또는 Enter 시 찜 목록 내 검색
- X 선택 시 검색어 비우고 전체 재조회
- 검색어는 앱 내에서 유지

### 3 [찜 목록]

- API: GET /api/event/liked?page&size=10&keyword
- 최신 작성순, 10개씩 무한 스크롤
- 로딩 중 스켈레톤 카드 3개
- 카드 선택 시 공지 상세(/event/:id)로 이동
- 하트 선택 시 찜 해제(DELETE), 목록에서 바로 빠지지 않음

### 4 [빈 목록]

- 로딩이 끝나고 0건일 때 노출

### 5 [비로그인 상태]

- 토큰이 없으면 목록 대신 안내 문구 노출
- [로그인] 선택 시 /login으로 이동

## 팝업

근거: `screens.json` SDD-WEB-EVT-0030 popups.

- 없음.

## API와 정책

- `GET /api/event/liked?page&size=10&keyword` — [[API-목록]]

관련 정책: [[POL-07-공지-검색]] · [[API-목록]]
