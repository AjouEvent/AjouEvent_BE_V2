---
type: 화면
status: 확정
source: screens.json SDD-WEB-EVT-0010 · src/pages/searchPage/SearchEventPage.jsx, src/pages/searchPage/SearchDropBox.jsx, src/components/layout/SearchBar.jsx, src/constants/searchDropOption.js, src/components/EventCard.jsx
updated: 2026-11-15
tags: []
---

# SDD-WEB-EVT-0010 공지사항 검색

- 경로: `/event`
- 진입 조건·소스: src/pages/searchPage/SearchEventPage.jsx, src/pages/searchPage/SearchDropBox.jsx, src/components/layout/SearchBar.jsx, src/constants/searchDropOption.js, src/components/EventCard.jsx
- 하단 내비게이션: 표시
- 상단 제목: 공지사항 검색
- 화면 분류: 공지 > 공지사항 검색

## 배치

근거: `screens.json` SDD-WEB-EVT-0010 layout.

1. 공지사항 검색 (header)
2. 분류 선택 드롭다운 2개 (chips) — 단체 선택 (기본: 아주대 공지사항) / 상세 선택 (기본: 아주대학교-일반)
3. 검색어를 입력해 주세요 (input)
4. 공지 카드 목록 (list) — 썸네일 / 제목 / 본문 미리보기 / 카테고리 태그 · 조회수 · 좋아요 수 / 하트 버튼
5. 더 이상 불러올 이벤트가 없습니다. (empty)
6. 서버 에러 (text)

## 구성 설명

근거: `screens.json` SDD-WEB-EVT-0010 descriptions.

### 2 [단체 선택]

- 옵션: 아주대 공지사항 / 학과 공지사항
- 옵션: 단과대 공지사항 / 기숙사 / 대학원
- 변경 시 상세 선택 목록이 바뀜
- 이전에 고른 상세 값이 새 목록에 없으면 비움

### 2 [상세 선택]

- 아주대: 아주대학교-일반, 아주대학교-장학
- 학과 43개, 단과대 13개, 기숙사·대학원 각 1개
- 변경 시 목록 초기화 후 첫 페이지 다시 조회
- 선택값은 앱 내에서 유지 (재진입 시 복원)

### 3 [검색창]

- 돋보기 선택 또는 Enter 시 검색 실행
- 입력값이 있으면 X(지우기) 버튼 노출
- X 선택 시 검색어 비우고 전체 목록 재조회
- 검색어는 앱 내에서 유지
- 제목 기준 부분 일치(서버 title LIKE)

### 4 [공지 목록]

- API: GET /api/event/{카테고리코드}?page&size=10&keyword
- 최신 작성순(createdAt 내림차순), 10개씩
- 목록 끝 도달 시 다음 페이지 자동 조회(무한 스크롤)
- 중복 글은 제외하고 이어 붙임
- 로딩 중 스켈레톤 카드 3개
- 카드 선택 시 공지 상세(/event/:id)로 이동
- 하트 동작은 홈 인기글 카드와 동일

### 5 [빈 목록]

- 결과가 0건이고 더 불러올 글이 없을 때 노출

### 6 [오류]

- 조회 실패 시 빨간 글씨 노출, 이후 추가 조회 중단

## 팝업

근거: `screens.json` SDD-WEB-EVT-0010 popups.

### 4-1 좋아요 에러

- 본문: 로그인이 필요한 기능입니다.
- 버튼:

## API와 정책

- `GET /api/event/{카테고리코드}?page&size=10&keyword` — [[API-목록]]

관련 정책: [[POL-07-공지-검색]] · [[API-목록]]
