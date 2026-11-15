---
type: 질문
status: 미정
source: 개선 노트 §0.1·§3~10 · screens.json notes · policies.json 15장
updated: 2026-11-15
tags: []
---

# 확인할 질문

미정인 값은 정책이나 화면 노트에 확정값으로 옮기지 않는다. 근거: 개선 노트 §0.1, `screens.json` notes, `policies.json` 15장.

### Q-01 DB 논리 백업을 중단할지

- 상태: 미정.
- 출처 1: 개선 노트 §0.1·§3.3: 중단 결정 대기, 아직 가동.
- 출처 2: infra.json backup: HeatWave 자동 백업 보관 1일과 별도 논리 백업.

### Q-02 크롤러 개선의 착수 범위를 어디까지 잡을지

- 상태: 미정.
- 출처 1: 개선 노트 §0.1: 감지 로직·소프트웨어학과·연결 풀 중 선택 대기.
- 출처 2: policies.json 15장 이슈 17~19: 공지 유실·404·겹침 실행.

### Q-03 이미지 변환 위치를 A1 imgproxy와 OCI Functions 중 어디로 할지

- 상태: 미정.
- 출처 1: 개선 노트 §0.1·§10: 이미지 미러링 기본 방향, 위치 미정.
- 출처 2: 보류 과제 PDF ④·⑤: 미러링과 프록시 설계가 각각 존재.

### Q-04 재게시 공지를 새 알림으로 보낼지

- 상태: 미정.
- 출처 1: 개선 노트 §0.1·§6: 재게시 처리 정책 결정 대기.
- 출처 2: policies.json 15장 이슈 17: 현재 크롤러는 재게시를 놓칠 수 있음.

### Q-05 운영 DB 토픽 이름이 어느 쪽인지

- 상태: 미정.
- 출처 1: policies.json 15장 이슈 2: 운영 DB 미확인.
- 출처 2: CR/config/notifierConfigs.json:4-5 AI모빌리티공학과 / RES/data.sql:2 미래모빌리티공학과.

### Q-06 홈의 알림 받기 버튼이 약관 동의로 가는 것이 의도인지

- 상태: 미정.
- 출처 1: screens.json notes 10: 의도 확인 필요.
- 출처 2: screens.json SDD-WEB-HOME-0010: 홈 화면 동작.

### Q-07 FAQ의 알림 일시 중지·비밀번호 변경 안내를 수정할지

- 상태: 미정.
- 출처 1: screens.json notes 11: 현재 기능과 불일치.
- 출처 2: policies.json 1장 POL-AUTH-01: Google 로그인만 존재.

### Q-08 미도달 등록·가이드 화면을 유지할지

- 상태: 미정.
- 출처 1: screens.json notes 1~3: 내부 진입 경로가 없거나 모달이 열리지 않음.
- 출처 2: screens.json screens: 17개 화면 목록에는 해당 경로가 없음.

### Q-09 구독 키워드 검색을 실제 필터로 고칠지

- 상태: 미정.
- 출처 1: screens.json notes 9: 검색어 필터가 없음.
- 출처 2: screens.json SDD-WEB-SUB-0020: 키워드 알림 탭.

### Q-10 회원정보 수정 뒤로가기의 의도된 경로가 무엇인지

- 상태: 미정.
- 출처 1: screens.json notes 7: onBack prop 무시.
- 출처 2: screens.json SDD-WEB-MY-0020: 회원정보 수정 화면.
