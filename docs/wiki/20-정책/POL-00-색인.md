---
type: MOC
status: 확정
source: policies.md 1~15장 · policies.json
updated: 2026-11-15
tags: []
---

# 정책 색인

| 장 | 주제 | POL 수 | 표/이슈 수 | 노트 |
|---|---|---:|---:|---|
| 1 | 회원 · 인증 | 15 | 0 | [[POL-01-회원-인증]] |
| 2 | 구독 (토픽) | 11 | 59 | [[POL-02-구독-토픽]] |
| 3 | 키워드 | 8 | 0 | [[POL-03-키워드]] |
| 4 | 공지 수집 | 11 | 0 | [[POL-04-공지-수집]] |
| 5 | 알림 발송 | 16 | 11 | [[POL-05-알림-발송]] |
| 6 | 알림함 | 5 | 0 | [[POL-06-알림함]] |
| 7 | 공지 목록 · 상세 · 검색 | 7 | 0 | [[POL-07-공지-검색]] |
| 8 | 인기글 · 주간 트렌드 · 배너 | 4 | 0 | [[POL-08-인기글-트렌드-배너]] |
| 9 | 조회수 | 7 | 0 | [[POL-09-조회수]] |
| 10 | 찜(북마크) · 캘린더 연동 | 8 | 0 | [[POL-10-찜-캘린더]] |
| 11 | 스케줄러 전체 목록 | 0 | 5 | [[POL-11-스케줄러]] |
| 12 | 에러 코드 목록 | 0 | 29 | [[POL-12-에러코드]] |
| 13 | 외부 연동 | 5 | 10 | [[POL-13-외부-연동]] |
| 14 | 주요 API 목록 | 0 | 41 | [[POL-14-API]] |
| 15 | 알려진 이슈 | 0 | 26 | [[POL-15-알려진-이슈]] |

## 근거 경로 약어

| 약어 | 실제 경로 |
| --- | --- |
| (없음) | `AjouEvent_BE_V2/src/main/java/com/example/ajouevent_be_v2/` |
| `RES/` | `AjouEvent_BE_V2/src/main/resources/` |
| `BE/` | `AjouEvent_BE_V2/` (루트 기준 파일: `deploy/`, `docs/`) |
| `CR/` | `AjouEvent_Crawling_V2/` |
| `노트` | `~/Downloads/ajouevent-improvement-notes.md` (운영 측정 기록) |

- 시간은 모두 KST입니다. 앱은 기동 시 JVM 기본 시간대를 `Asia/Seoul`로 설정하고(`AjouEventBeV2Application.java:21-24`), DB 접속도 `serverTimezone=Asia/Seoul`입니다(`RES/config/db.yaml:4`).

---

> [!important] 정책 조회
> 먼저 이 색인에서 장을 고르고 해당 노트를 읽는다. 에러와 API 표는 각각 [[에러코드-전량]], [[API-목록]]에서 찾는다.
