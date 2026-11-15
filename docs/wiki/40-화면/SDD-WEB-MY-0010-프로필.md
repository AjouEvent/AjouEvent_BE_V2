---
type: 화면
status: 확정
source: screens.json SDD-WEB-MY-0010 · src/pages/myPage/MyPage.jsx
updated: 2026-11-15
tags: []
---

# SDD-WEB-MY-0010 프로필

- 경로: `/mypage`
- 진입 조건·소스: src/pages/myPage/MyPage.jsx
- 하단 내비게이션: 표시
- 상단 제목: 프로필
- 화면 분류: 프로필

## 배치

근거: `screens.json` SDD-WEB-MY-0010 layout.

1. 프로필  [로그아웃] (header)
2. 회원 정보 (card) — 프로필 이미지 (없으면 사람 아이콘) / 이름 / 학과 / 이메일
3. 메뉴 (list) — 회원정보 수정 › / FAQ › / 버전 & 히스토리 › / 피드백 / 오류 제보 › / 팀소개 ›

## 구성 설명

근거: `screens.json` SDD-WEB-MY-0010 descriptions.

### 1 [로그아웃]

- API: POST /api/users/logout
- 서버 실패와 무관하게 로컬 로그인 정보 삭제
- '로그아웃 성공' 토스트 후 /login으로 이동

### 2 [회원 정보]

- API: GET /api/users
- 값이 없으면 '-' 표시
- 비로그인 진입 시 /login으로 바로 이동

### 3 [메뉴]

- 회원정보 수정: /profile-modification (회원 정보 전달)
- FAQ: /faq
- 버전 & 히스토리: /version
- 피드백 / 오류 제보: 구글 폼 새 창
- 팀소개: 노션 페이지 새 창

## 팝업

근거: `screens.json` SDD-WEB-MY-0010 popups.

### 1-1 로그아웃 성공

- 본문: 로그아웃 했습니다.
- 버튼:

## API와 정책

- `GET /api/users` — [[API-목록]]
- `POST /api/users/logout` — [[API-목록]]

관련 정책: [[POL-01-회원-인증]] · [[API-목록]]
