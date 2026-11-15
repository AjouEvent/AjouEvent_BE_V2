---
type: 화면
status: 확정
source: screens.json SDD-WEB-EVT-0020 · src/pages/eventPage/EventDetailPage.jsx, src/pages/eventPage/EventBanner.jsx, src/pages/eventPage/ImageModal.jsx, src/components/CalendarModal.jsx, src/components/layout/TabBar.jsx
updated: 2026-11-15
tags: []
---

# SDD-WEB-EVT-0020 공지사항 상세

- 경로: `/event/:id`
- 진입 조건·소스: src/pages/eventPage/EventDetailPage.jsx, src/pages/eventPage/EventBanner.jsx, src/pages/eventPage/ImageModal.jsx, src/components/CalendarModal.jsx, src/components/layout/TabBar.jsx
- 하단 내비게이션: 숨김
- 상단 제목: 공지사항
- 화면 분류: 공지 > 공지사항 검색 > 공지사항 상세

## 배치

근거: `screens.json` SDD-WEB-EVT-0020 layout.

1. ← 공지사항 (header)
2. 공지 이미지 슬라이드 (화면 높이 75%) (image) — ‹ 이전 / › 다음 / 하단 점 인디케이터
3. 카테고리 태그 + 제목 (text)
4. 작성일시 · 조회수 · 좋아요 수 (text) — YYYY년 MM월 DD일 HH:mm
5. 본문 (text) — 작성자: {작성자}
6. 하단 고정 액션 바 (button) — 북마크 아이콘 / 사이트 바로가기 / 캘린더에 추가

## 구성 설명

근거: `screens.json` SDD-WEB-EVT-0020 descriptions.

### 1 [뒤로가기]

- 이전 기록이 있으면 뒤로 이동
- 기록이 없으면 공지사항 검색(/event)으로 이동

### 2 [이미지 슬라이드]

- 2장 이상이면 좌우 화살표·점 인디케이터 노출
- 스와이프(50px 이상) 또는 점 선택으로 이동
- 이미지 선택 시 이미지 확대 팝업 노출
- 이미지 없으면 영역 비움

### 3 [상세 조회]

- 로그인: GET /api/event/detail/{id} (토큰 포함)
- 비로그인: 같은 API를 토큰 없이 호출
- 서버가 24시간 내 중복 조회를 막고 조회수 증가
- 로딩 중 전체 스켈레톤 화면 노출
- 401 응답 시 '세션 만료' 토스트

### 3 [알림에서 진입 시]

- 알림 ID를 받아 POST /api/notification/click 호출
- 해당 알림을 읽음 처리

### 5 [본문]

- 줄바꿈(\n) 기준으로 문단 나눠 표시

### 6 [북마크(찜) 버튼]

- 찜 상태면 채운 북마크 + 연분홍 배경
- 찜: POST /api/event/like/{id}
- 찜 해제: DELETE /api/event/like/{id}
- 좋아요 수 ±1 반영, 실패 시 '좋아요 에러' 토스트

### 6 [사이트 바로가기]

- 선택 시 원문 공지 URL로 이동(같은 창)
- URL 없으면 'url 에러' 경고 토스트

### 6 [캘린더에 추가]

- 선택 시 이벤트 캘린더 등록 팝업 노출

## 팝업

근거: `screens.json` SDD-WEB-EVT-0020 popups.

### 2-1 이미지 확대 (제목 없음)

- 본문: 검은 반투명 배경 위 원본 이미지. 2장 이상이면 < > 버튼·스와이프로 이동, 배경 선택 시 닫힘
- 버튼: < / >

### 6-1 이벤트 캘린더 등록

- 본문: 제목[초기화] 입력(기본: 공지 제목, placeholder '제목을 입력해 주세요') / 내용[초기화] 입력(기본: 공지 본문) / 시작 날짜 / 종료 날짜(기본: 현재 시각). 시작>종료면 종료를 맞춤. 빈 값이면 '제목을 입력해 주세요' 등 오류 문구. [등록] 시 POST /api/event/calendar
- 버튼: 취소 / 등록

### 6-2 구글 캘린더 등록 성공

- 본문: 구글캘린더에 이벤트가 등록되었습니다.
- 버튼: 

### 6-3 구글 캘린더 등록 실패

- 본문: 소셜로그인으로 로그인한 사용자만 이용가능한 서비스 입니다.
- 버튼: 

### 6-4 url 에러

- 본문: 바로가기 가능한 url이 없습니다.
- 버튼: 

### 3-1 세션 만료

- 본문: 다시 로그인 해주세요.
- 버튼: 

## API와 정책

- `DELETE /api/event/like/{id}` — [[API-목록]]
- `GET /api/event/detail/{id}` — [[API-목록]]
- `POST /api/event/calendar` — [[API-목록]]
- `POST /api/event/like/{id}` — [[API-목록]]
- `POST /api/notification/click` — [[API-목록]]

관련 정책: [[POL-07-공지-검색]] · [[API-목록]]
