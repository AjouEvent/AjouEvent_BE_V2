---
type: 화면
status: 확정
source: screens.json SDD-WEB-MY-0060 · src/pages/myPage/VersionPage.jsx
updated: 2026-11-15
tags: []
---

# SDD-WEB-MY-0060 버전 & 히스토리

- 경로: `/version`
- 진입 조건·소스: src/pages/myPage/VersionPage.jsx
- 하단 내비게이션: 숨김
- 상단 제목: 버전 & 히스토리
- 화면 분류: 프로필 > 버전 & 히스토리

## 배치

근거: `screens.json` SDD-WEB-MY-0060 layout.

1. ← 버전 & 히스토리 (header)
2. 세로 타임라인 (list) — Phase 0 프로젝트 시작 (2024 초반) / V1 핵심 기능 완성 (2024 중반 ~ 후반) / V1.5 아키텍처 개선 & 코드 품질 향상 / V2 전면 리아키텍처 (2025 하반기 ~ 2026) / Now V1 → V2 마이그레이션 & 안정화 (2026 ~)
3. 단계별 카드 (card) — 버전 배지 + 제목 / 기간 / Backend / Frontend / Infra 배지 / 작업 내역 글머리표

## 구성 설명

근거: `screens.json` SDD-WEB-MY-0060 descriptions.

### 1 [뒤로가기]

- 이전 기록이 있으면 뒤로, 없으면 /event로 이동

### 2 [타임라인]

- 현재 단계(Now)는 파란 점 + 깜빡임 효과
- 현재 단계 카드 머리는 연파랑 배경
- API 호출 없음 (화면 내 고정 문구)

### 3 [분야 배지 색]

- Backend 파랑, Frontend 초록, Infra 주황

## 팝업

근거: `screens.json` SDD-WEB-MY-0060 popups.

- 없음.

## API와 정책

- 이 화면 설명에 명시된 API 없음. 전체 경로는 [[API-목록]] 참조.

관련 정책: [[POL-01-회원-인증]] · [[API-목록]]
