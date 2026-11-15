---
type: 화면
status: 확정
source: screens.json SDD-WEB-MY-0050 · src/pages/myPage/FAQPage.jsx
updated: 2026-11-15
tags: []
---

# SDD-WEB-MY-0050 FAQ

- 경로: `/faq`
- 진입 조건·소스: src/pages/myPage/FAQPage.jsx
- 하단 내비게이션: 숨김
- 상단 제목: FAQ
- 화면 분류: 프로필 > FAQ

## 배치

근거: `screens.json` SDD-WEB-MY-0050 layout.

1. ← FAQ (header)
2. 서비스 소개 (section) — Q. 아주이벤트는 어떤 서비스인가요? / Q. 로그인 없이도 이용할 수 있나요?
3. 알림 & 구독 (section) — Q. 토픽 구독은 무엇인가요? / Q. 키워드 구독은 어떻게 동작하나요? / Q. 알림이 오지 않아요. / Q. 알림을 너무 많이 받고 있어요.
4. 계정 관리 (section) — Q. 회원정보(이름, 학과)를 변경하고 싶어요. / Q. 비밀번호를 변경하고 싶어요. / Q. 회원 탈퇴는 어떻게 하나요?
5. 기타 (section) — Q. 오류를 발견했어요. / Q. 개발팀에 대해 알고 싶어요.

## 구성 설명

근거: `screens.json` SDD-WEB-MY-0050 descriptions.

### 1 [뒤로가기]

- 이전 기록이 있으면 뒤로, 없으면 /event로 이동

### 2 [질문 항목]

- 질문 선택 시 아래에 'A. 답변' 박스 펼침
- 다시 선택 시 접힘, › 아이콘 90도 회전
- 항목별로 독립적으로 펼침/접힘
- API 호출 없음 (화면 내 고정 문구)

## 팝업

근거: `screens.json` SDD-WEB-MY-0050 popups.

- 없음.

## API와 정책

- 이 화면 설명에 명시된 API 없음. 전체 경로는 [[API-목록]] 참조.

관련 정책: [[POL-01-회원-인증]] · [[API-목록]]
