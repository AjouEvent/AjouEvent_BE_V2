---
type: 화면
status: 확정
source: screens.json SDD-WEB-MY-0040 · src/pages/myPage/DeleteAccountPage.jsx
updated: 2026-11-15
tags: []
---

# SDD-WEB-MY-0040 회원 탈퇴 - 탈퇴 절차

- 경로: `/delete-account`
- 진입 조건·소스: src/pages/myPage/DeleteAccountPage.jsx
- 하단 내비게이션: 표시
- 상단 제목: 없음
- 화면 분류: 프로필 > 회원정보 수정 > 회원 탈퇴 > 탈퇴 절차

## 배치

근거: `screens.json` SDD-WEB-MY-0040 layout.

1. 탈퇴 절차 (text) — 탈퇴 전 구독 중인 토픽과 키워드를 초기화하고, 탈퇴 사유를 선택해주세요.
2. 구독한 토픽 초기화 (button)
3. 구독한 키워드 초기화 (button)
4. 탈퇴 사유를 선택하세요 (드롭다운) (input) — 알림이 너무 많음 / 사용 빈도가 낮음 / 기타
5. 탈퇴하기 (button)

## 구성 설명

근거: `screens.json` SDD-WEB-MY-0040 descriptions.

### 2 [토픽 초기화]

- API: DELETE /api/topic/subscriptions/reset
- 처리 중 '토픽 초기화 중...'
- 완료 시 '✓ 토픽 초기화 완료'로 바뀌고 비활성
- 실패 시 '토픽 초기화에 실패했습니다.' 토스트

### 3 [키워드 초기화]

- API: DELETE /api/keyword/subscriptions/reset
- 처리 중 '키워드 초기화 중...'
- 완료 시 '✓ 키워드 초기화 완료'로 바뀌고 비활성
- 실패 시 '키워드 초기화에 실패했습니다.' 토스트

### 4 [탈퇴 사유]

- 사유는 화면 조건 확인용, 서버로 보내지 않음

### 5 [탈퇴하기]

- 토픽·키워드 초기화 + 사유 선택 전에는 회색 비활성
- 조건 충족 시 빨간 버튼으로 활성
- API: DELETE /api/users
- 성공 시 로컬 로그인 정보 삭제, '탈퇴 완료' 토스트
- 이후 /login으로 이동
- 실패 시 '탈퇴에 실패했습니다.' 토스트

## 팝업

근거: `screens.json` SDD-WEB-MY-0040 popups.

### 5-1 탈퇴 완료

- 본문: 정상적으로 탈퇴되었습니다.
- 버튼:

### 5-2 알림

- 본문: 모든 조건을 충족해야 탈퇴할 수 있습니다.
- 버튼:

## API와 정책

- `DELETE /api/keyword/subscriptions/reset` — [[API-목록]]
- `DELETE /api/topic/subscriptions/reset` — [[API-목록]]
- `DELETE /api/users` — [[API-목록]]

관련 정책: [[POL-01-회원-인증]] · [[API-목록]]
