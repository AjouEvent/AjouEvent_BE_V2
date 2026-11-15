---
type: vault-home
status: 확정
source: policies.json · screens.json · infra.json · Figma 파일 페이지 맵
updated: 2026-11-15
tags: []
---

이 폴더는 vault의 공개 사본이며 BE eb8dc94 · FE 4cedd82 · Crawler 2f2e6de(2026-11-15)를 기준으로 합니다.


# AjouEvent 내부 LLM 위키

AjouEvent의 코드·정책·화면·운영 기록을 에이전트가 먼저 읽는 지식 저장소다. 근거: `policies.json`, `screens.json`, `infra.json` 추출 기준.

## 다섯 가지 전제

1. 회원은 Google OAuth 인가 코드로 로그인한다. 근거: POL-AUTH-01.
2. 토픽은 59개이고 크롤러가 58개 게시판을 수집한다. 근거: POL-SUB-01.
3. 알림은 크롤러 → 웹훅 → outbox → FCM으로 흐른다. 근거: POL-CRAWL-01 · POL-PUSH-01.
4. 운영은 2026-11-13 OCI로 이전했다. 근거: `infra.json` meta.cutover.
5. 코드·화면·운영 자료의 차이는 [[Q-확인필요]]와 [[구현현황-코드매핑]]에서 확인한다. 근거: `infra.json` conflicts · `screens.json` notes.

## 시작

[[에이전트-운영규약]] → [[MOC-전체지도]] → 작업에 해당하는 노트를 읽는다.

## 폴더

`10-기준`은 용어와 문서 체계, `20-정책`은 POL, `40-화면`은 SDD, `50-에러코드`와 `55-API`는 표의 진입점, `60-인프라`는 운영, `70-구현`은 구현 차이, `90-질문`은 미정, `_sources`는 원문 사본이다. 근거: [[MOC-전체지도]].

## Figma

- [아주이벤트 v1.0 — 위키·정책서·화면 설계서](https://www.figma.com/design/Elzan9lwVvKbDqHXvvhfwq/?node-id=0-1)
- [Design System](https://www.figma.com/design/Elzan9lwVvKbDqHXvvhfwq/?node-id=1-2)
- [Infra](https://www.figma.com/design/Elzan9lwVvKbDqHXvvhfwq/?node-id=1-3)
- [아주이벤트 기획 — 컨셉 기획서](https://www.figma.com/design/Elzan9lwVvKbDqHXvvhfwq/?node-id=1-13388)

Figma 페이지 ID의 근거: 작업 자료의 파일 페이지 맵. 자세한 문서 관계는 [[문서-체계]].
