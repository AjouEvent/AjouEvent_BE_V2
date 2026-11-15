---
type: 화면
status: 확정
source: screens.json SDD-WEB-HOME-0010 · src/pages/homePage/HomePage.jsx, src/pages/homePage/HomeBanner.jsx, src/pages/homePage/HomeHotEvent.jsx, src/components/layout/HelpBox.jsx, src/components/EventCard.jsx
updated: 2026-11-15
tags: []
---

# SDD-WEB-HOME-0010 홈

- 경로: `/`
- 진입 조건·소스: src/pages/homePage/HomePage.jsx, src/pages/homePage/HomeBanner.jsx, src/pages/homePage/HomeHotEvent.jsx, src/components/layout/HelpBox.jsx, src/components/EventCard.jsx
- 하단 내비게이션: 표시
- 상단 제목: 없음
- 화면 분류: 홈

## 배치

근거: `screens.json` SDD-WEB-HOME-0010 layout.

1. 우측 상단 플로팅 버튼 (알림 종 / 앱 다운로드) (button) — 알림(종) + 안 읽은 수 배지 / 앱 다운로드
2. 이벤트 배너 캐러셀 (정사각형) (banner) — n / 전체 페이지 표시 / 하단 점 인디케이터
3. 이번주 인기글 (section)
4. 인기 공지 카드 목록 (list) — 썸네일 / 제목 (2줄) / 본문 미리보기 (1줄) / 카테고리 태그 · 조회수 · 좋아요 수 / 하트 버튼
5. 데이터를 불러오지 못했습니다 (text)
6. 설치 · 푸시 알림 안내 팝업 (조건부) (modal)

## 구성 설명

근거: `screens.json` SDD-WEB-HOME-0010 descriptions.

### 1 [알림 종 버튼]

- 선택 시 알림 화면(/notification)으로 이동
- API: GET /api/notification/unread-count
- 안 읽은 알림 수 배지 노출, 100 이상은 '99+'
- 앱 아이콘 배지 숫자도 함께 갱신

### 1 [앱 다운로드 버튼]

- 선택 시 설치 안내 노션 페이지로 이동

### 2 [배너]

- API: GET /api/event/banner
- 3초마다 자동 넘김 (이미지 2장 이상일 때)
- 좌우 스와이프(50px 이상)로 이전/다음
- 배너 선택 시 배너의 외부 링크(siteUrl)로 이동
- 로딩 중/데이터 없음: 회색 스켈레톤 노출

### 4 [인기 공지 목록]

- API: GET /api/event/popular
- 이번 주(월~일) 글 중 조회수 상위 10개
- 로딩 중 스켈레톤 카드 3개 노출
- 카드 선택 시 공지 상세(/event/:id)로 이동
- http 이미지는 프록시(weserv) 경유로 표시

### 4 [하트(찜) 버튼]

- 찜 상태면 빨간 채운 하트, 아니면 회색 외곽선
- 찜: POST /api/event/like/{id}
- 찜 해제: DELETE /api/event/like/{id}
- 성공 시 좋아요 수 ±1 반영
- 실패(비로그인 등) 시 '좋아요 에러' 토스트

### 5 [오류 문구]

- 인기글 조회 실패 시 빨간 글씨로 노출

### 6 [진입 시 자동 처리]

- 알림 권한 요청 → FCM 토큰 발급
- 토큰 있으면 POST /send/registration-token 등록
- 카카오톡 인앱 브라우저면 외부 브라우저로 다시 열기

## 팝업

근거: `screens.json` SDD-WEB-HOME-0010 popups.

### 6-1 AjouEvent를 설치하고 공지사항 알림을 받아보세요!

- 본문: 앱에서 공지사항, 키워드 구독을 통해 푸시 알림을 받을 수 있어요. (PWA 미설치 상태 + 브라우저 설치 이벤트 발생 시 노출)
- 버튼: 설치하기 / 나중에 설치

### 6-2 푸시 알림 받기 (전체 화면)

- 본문: 푸시 알림을 설정하고 각종 공지사항, 키워드 알림을 받아보세요! (설치된 앱으로 처음 열 때 1회 노출) / [알림 받기] 선택 시 권한 요청 후 약관 동의(/privacy-agreement)로 이동
- 버튼: 알림 받기 / 나중에 받을게요

### 6-3 AjouEvent 앱 설치하기 - 아이폰 (iOS 전용)

- 본문: 홈 화면에 앱을 추가하고 각종 공지사항, 키워드 알림을 받아보세요. / 하단 메뉴에서 '공유' 아이콘을 눌러주세요. / 아래의 '홈 화면에 추가' 버튼을 눌러주세요.
- 버튼:

### 4-1 좋아요 에러

- 본문: 로그인이 필요한 기능입니다.
- 버튼:

## API와 정책

- `DELETE /api/event/like/{id}` — [[API-목록]]
- `GET /api/event/banner` — [[API-목록]]
- `GET /api/event/popular` — [[API-목록]]
- `GET /api/notification/unread-count` — [[API-목록]]
- `POST /api/event/like/{id}` — [[API-목록]]

관련 정책: [[POL-07-공지-검색]] · [[API-목록]]
