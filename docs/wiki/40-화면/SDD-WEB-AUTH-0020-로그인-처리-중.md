---
type: 화면
status: 확정
source: screens.json SDD-WEB-AUTH-0020 · src/pages/loginPage/LoginSuccess.jsx
updated: 2026-11-15
tags: []
---

# SDD-WEB-AUTH-0020 로그인 처리 중

- 경로: `/loginSuccess`
- 진입 조건·소스: src/pages/loginPage/LoginSuccess.jsx
- 하단 내비게이션: 숨김
- 상단 제목: 없음
- 화면 분류: 인증 > 로그인 > 로그인 처리

## 배치

근거: `screens.json` SDD-WEB-AUTH-0020 layout.

1. 로그인 중 (text)
2. 로딩 스피너 (image)

## 구성 설명

근거: `screens.json` SDD-WEB-AUTH-0020 descriptions.

### 1 [로그인 처리]

- URL의 인가 코드(code)로 자동 로그인 요청
- API: POST /api/users/oauth (code, fcmToken, redirectUri)
- 성공 시 토큰·이메일·이름·학과를 로컬에 저장
- 성공 시 '로그인 성공' 토스트
- 신규 회원이면 약관 동의(/privacy-agreement)로 이동
- 기존 회원이면 홈(/)으로 이동

### 2 [실패 처리]

- 400: '로그인 실패' 오류 토스트 후 /login 이동
- 기타 응답 오류: 서버 메시지 경고 토스트 후 /login
- 응답 없음: '서버 응답이 없습니다.' 후 /login
- code 없음: 'Missing URL parameters' 후 /login

## 팝업

근거: `screens.json` SDD-WEB-AUTH-0020 popups.

- 없음.

## API와 정책

- `POST /api/users/oauth` — [[API-목록]]

관련 정책: [[POL-01-회원-인증]] · [[API-목록]]
