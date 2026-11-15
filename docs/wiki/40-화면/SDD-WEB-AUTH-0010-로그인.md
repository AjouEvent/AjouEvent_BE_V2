---
type: 화면
status: 확정
source: screens.json SDD-WEB-AUTH-0010 · src/pages/loginPage/LoginPage.jsx, src/services/fcm/GetUserPermission.jsx
updated: 2026-11-15
tags: []
---

# SDD-WEB-AUTH-0010 로그인

- 경로: `/login`
- 진입 조건·소스: src/pages/loginPage/LoginPage.jsx, src/services/fcm/GetUserPermission.jsx
- 하단 내비게이션: 표시
- 상단 제목: 없음
- 화면 분류: 인증 > 로그인

## 배치

근거: `screens.json` SDD-WEB-AUTH-0010 layout.

1. 로그인 (text) — 기본 로그인이 구글 로그인으로 통합되었습니다.
2. Google 계정으로 로그인 (button)
3. 서비스 안내 문구 (text) — AjouEvent는 2024-1학기 아주대학교 파란학기제에서 진행한 프로젝트로 아주대학교 공식 서비스가 아닙니다. / AjouEvent 계정은 아주대학교 포탈 계정과 무관합니다.
4. 서비스 문의 (text) — BE: 심재엽 / FE: 윤석찬
5. 알림 서비스 등록 중 ... (modal)

## 구성 설명

근거: `screens.json` SDD-WEB-AUTH-0010 descriptions.

### 1 [제목]

- '구글 로그인' 문구는 파란색 강조

### 2 [Google 로그인 버튼]

- 선택 시 구글 OAuth 동의 화면으로 이동
- ajou.ac.kr 도메인 계정만 허용(hd 파라미터)
- 요청 권한: 이메일·프로필·캘린더·소속 정보
- 완료 후 /loginSuccess로 돌아옴
- FCM 토큰이 없으면 이동하지 않고 오류 토스트 노출

### 4 [서비스 문의 링크]

- 선택 시 메일 앱 열림 (제목: ajouevent 서비스 문의)

### 5 [알림 등록 로딩]

- 진입 시 브라우저 알림 권한 요청
- 허용 시 FCM 토큰 발급 중 반투명 오버레이 노출
- 토큰 새로 저장 시 '알림 토큰 저장 성공' 토스트
- 실패 시 '알림 토큰 요청 실패' 토스트
- 알림 미지원 브라우저면 홈 화면 추가 안내 alert

## 팝업

근거: `screens.json` SDD-WEB-AUTH-0010 popups.

### 2-1 알림 토큰 미등록

- 본문: '홈화면에 추가'를 통해 설치 / 알림 설정(허용)을 확인해주세요
- 버튼:

### 5-1 알림 미지원 안내(alert)

- 본문: 알림 서비스를 원활하게 사용하시려면 홈화면에 추가 후, 알림 허용을 해주세요.
- 버튼: 확인

## API와 정책

- 이 화면 설명에 명시된 API 없음. 전체 경로는 [[API-목록]] 참조.

관련 정책: [[POL-01-회원-인증]] · [[API-목록]]
