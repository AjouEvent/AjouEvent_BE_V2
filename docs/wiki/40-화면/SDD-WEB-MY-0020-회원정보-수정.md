---
type: 화면
status: 확정
source: screens.json SDD-WEB-MY-0020 · src/pages/myPage/ProfileModificationPage.jsx
updated: 2026-11-15
tags: []
---

# SDD-WEB-MY-0020 회원정보 수정

- 경로: `/profile-modification`
- 진입 조건·소스: src/pages/myPage/ProfileModificationPage.jsx
- 하단 내비게이션: 표시
- 상단 제목: 회원정보 수정
- 화면 분류: 프로필 > 회원정보 수정

## 배치

근거: `screens.json` SDD-WEB-MY-0020 layout.

1. ← 회원정보 수정 (header)
2. 로그인 정보 (section)
3. 아이디 (이메일) — 읽기 전용 (input)
4. 이름 (placeholder: 이름을 입력해주세요) (input)
5. 학과 (placeholder: 학과를 입력해주세요) (input)
6. 저장하기 (button)
7. 회원 탈퇴하기 (빨간 글씨, 우측) (button)

## 구성 설명

근거: `screens.json` SDD-WEB-MY-0020 descriptions.

### 1 [뒤로가기]

- 이전 기록이 있으면 뒤로, 없으면 /event로 이동

### 3 [이메일]

- 프로필 화면에서 받은 이메일 표시, 수정 불가

### 4 [이름·학과]

- 기존 값이 미리 채워짐
- 학과는 자유 입력

### 6 [저장하기]

- 이름·학과가 바뀌지 않으면 비활성
- 이름이 비면 '이름을 입력해주세요.' 경고
- API: PATCH /api/users (name, major)
- 성공 시 '수정 완료' 토스트 후 /mypage 이동
- 저장 중 버튼 문구 '저장 중...'
- 실패 시 '수정 실패' 토스트

### 7 [회원 탈퇴하기]

- 선택 시 회원 탈퇴(/delete-account)로 이동

## 팝업

근거: `screens.json` SDD-WEB-MY-0020 popups.

### 6-1 수정 완료

- 본문: 회원정보가 수정되었습니다.
- 버튼: 

### 6-2 수정 실패

- 본문: 회원정보 수정에 실패했습니다.
- 버튼: 

## API와 정책

- `PATCH /api/users` — [[API-목록]]

관련 정책: [[POL-01-회원-인증]] · [[API-목록]]
