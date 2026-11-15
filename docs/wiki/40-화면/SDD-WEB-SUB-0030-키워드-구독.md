---
type: 화면
status: 확정
source: screens.json SDD-WEB-SUB-0030 · src/pages/subscribePage/KeywordSubscribePage.jsx
updated: 2026-11-15
tags: []
---

# SDD-WEB-SUB-0030 키워드 구독

- 경로: `/subscribe/keywordSubscribe`
- 진입 조건·소스: src/pages/subscribePage/KeywordSubscribePage.jsx
- 하단 내비게이션: 표시
- 상단 제목: 키워드 구독
- 화면 분류: 구독 > 키워드 알림 > 키워드 구독

## 배치

근거: `screens.json` SDD-WEB-SUB-0030 layout.

1. ← 키워드 구독 (header)
2. ⚙ 게시판 선택 (선택 후 게시판명 표시) (button)
3. 알림 받을 키워드를 입력해주세요. (input)
4. 구독 (button)
5. 알림 설정한 키워드  {n} / 10 (section)
6. 구독 키워드 카드 (list) — 키워드 (굵게) / 게시판명 배지 / 휴지통 버튼
7. 구독한 키워드가 없습니다. (empty) — 게시판을 선택하고 키워드를 추가해보세요.
8. 비로그인: 로그인이 필요한 서비스입니다 + [로그인] (empty)

## 구성 설명

근거: `screens.json` SDD-WEB-SUB-0030 descriptions.

### 1 [뒤로가기]

- 선택 시 이전 화면으로 이동

### 2 [게시판 선택]

- 선택 시 '게시판 선택' 팝업 노출
- API: GET /api/topic/subscriptionsStatus
- 선택된 상태면 파란 테두리 + 게시판명 표시

### 3 [키워드 입력]

- 앞 공백 자동 제거
- 한글·영어·공백 외 입력 시 아래 빨간 오류 문구
- 1글자면 '두 글자 이상 입력해 주세요.'

### 4 [구독 버튼]

- 게시판 미선택 시 '게시판 선택' 경고 토스트
- 2글자 미만/특수문자 시 '입력 오류' 토스트, 입력 비움
- API: POST /api/keyword/subscribe (키워드, 게시판)
- 성공 시 '구독 성공' 토스트, 목록 갱신, 게시판 선택 해제
- 처리 중에는 버튼 비활성

### 5 [키워드 개수]

- 최대 10개 (서버에서 초과 시 400)
- API: GET /api/keyword/userKeywords

### 6 [휴지통 버튼]

- 확인 없이 바로 구독 취소
- API: POST /api/keyword/unsubscribe
- 성공 시 '구독 취소 성공' 토스트, 목록에서 제거
- 실패 시 '구독 실패 / 서버 에러' 토스트

### 8 [비로그인 상태]

- [로그인] 선택 시 /login으로 이동

## 팝업

근거: `screens.json` SDD-WEB-SUB-0030 popups.

### 2-1 게시판 선택

- 본문: 분류 아코디언(학과 / 단과대 / 공지사항 / 기숙사 / 대학원), 게시판별 [선택] / [선택 해제]. 선택 시 팝업 닫힘
- 버튼: 선택 / 선택 해제

### 4-1 게시판 선택(경고)

- 본문: 키워드를 구독하기 전에 게시판을 선택해 주세요.
- 버튼: 

### 4-2 입력 오류

- 본문: 2글자 이상, 한글만 입력해주세요
- 버튼: 

### 4-3 구독 실패 토스트 (응답 코드별)

- 본문: 409 이미 구독한 키워드 / 400 키워드 개수 초과 / 404 찾을 수 없음 / 500 서버 오류 / 기타 오류
- 버튼: 

## API와 정책

- `GET /api/keyword/userKeywords` — [[API-목록]]
- `GET /api/topic/subscriptionsStatus` — [[API-목록]]
- `POST /api/keyword/subscribe` — [[API-목록]]
- `POST /api/keyword/unsubscribe` — [[API-목록]]

관련 정책: [[POL-02-구독-토픽]] · [[API-목록]]
