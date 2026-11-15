---
type: 화면
status: 확정
source: screens.json SDD-WEB-AUTH-0030 · src/pages/signupPage/PrivacyAgreementPage.jsx, public/terms_of_service.html, public/privacy_consent_form.html
updated: 2026-11-15
tags: []
---

# SDD-WEB-AUTH-0030 약관 동의

- 경로: `/privacy-agreement`
- 진입 조건·소스: src/pages/signupPage/PrivacyAgreementPage.jsx, public/terms_of_service.html, public/privacy_consent_form.html
- 하단 내비게이션: 숨김
- 상단 제목: 없음
- 화면 분류: 인증 > 회원가입 > 약관 동의

## 배치

근거: `screens.json` SDD-WEB-AUTH-0030 layout.

1. AjouEvent 서비스 이용 약관에 동의해주세요 (text) — AjouEvent는 2024-1학기 아주대학교 파란학기제로 진행한 프로젝트로 아주대학교 공식 서비스가 아닙니다. / AjouEvent 계정은 아주대학교 포탈 계정과 무관합니다.
2. 약관 전체 동의하기 (toggle)
3. 필수 약관 체크박스 (list) — (필수) 만 14세 이상입니다. / (필수) 서비스 이용약관에 동의  보기 / (필수) 개인정보 수집이용에 동의  보기
4. 다음 (button)

## 구성 설명

근거: `screens.json` SDD-WEB-AUTH-0030 descriptions.

### 2 [전체 동의]

- 체크 시 세 항목 모두 체크, 해제 시 모두 해제
- 세 항목이 모두 체크되면 자동으로 체크 상태

### 3 [약관 항목]

- 항목별 개별 체크 가능
- '보기' 선택 시 약관 전문 팝업 노출
- 팝업 [확인] 선택 시 해당 항목 자동 체크
- 약관 로드 실패 시 '문서 로드 오류' 토스트

### 4 [다음 버튼]

- 세 항목 모두 체크 전에는 회색 비활성
- 선택 시 홈(/)으로 이동
- 서버 저장 API 호출 없음 (화면 내 확인만)

## 팝업

근거: `screens.json` SDD-WEB-AUTH-0030 popups.

### 3-1 약관 전문 (서비스 이용약관 / 개인정보 수집이용)

- 본문: HTML 약관 본문 스크롤 영역
- 버튼: 확인

### 4-1 동의 필요

- 본문: 모든 필수 항목에 동의해 주세요.
- 버튼: 

## API와 정책

- 이 화면 설명에 명시된 API 없음. 전체 경로는 [[API-목록]] 참조.

관련 정책: [[POL-01-회원-인증]] · [[API-목록]]
