---
type: 정책
status: 확정
source: policies.md 3장 · policies.json 3장
updated: 2026-11-15
tags: []
---

# 3장 키워드

원문: `policies.md` 3장. 각 항목의 정책·값·근거는 추출 JSON 원문 그대로다.

### POL-KW-01 키워드 등록

- 정책: 키워드는 토픽 하나에 묶어서 등록합니다. 같은 토픽·같은 키워드는 회원끼리 keywords 행 하나를 함께 씁니다.
- 값: POST /api/keyword/subscribe {koreanKeyword, topicName(department)} → 201. 없는 토픽 404. 저장 형식 encoded_keyword = URLEncode(키워드).replace("+", "%20") + "_" + department, search_keyword = 키워드 + "_" + department.
- 근거: controller/KeywordController.java:26-33, orchestrator/KeywordOrchestrator.java:42-48, service/keyword/KeywordCommandService.java:28-34, domain/keyword/Keyword.java:46-58

### POL-KW-02 중복과 개수 제한

- 정책: 같은 키워드를 두 번 구독할 수 없고, 회원 한 명이 구독할 수 있는 키워드는 모든 토픽을 합쳐 10개입니다.
- 값: 중복 409 AE-KEYWORD-ALREADY-SUBSCRIBED. 10개를 가진 상태에서 추가 400 AE-KEYWORD-MAX-SUBSCRIPTION-LIMIT. 생성 값 is_read=false.
- 근거: service/keyword/KeywordCommandService.java:23, service/keyword/KeywordCommandService.java:41-47, service/keyword/KeywordCommandService.java:74-84, domain/keyword/KeywordMember.java:46-53

### POL-KW-03 입력 검증

- 정책: 키워드 문자열에는 검증이 없습니다. 빈 문자열, 공백, 길이 제한을 검사하지 않고 앞뒤 공백도 자르지 않습니다.
- 값: 없음(DB 컬럼 VARCHAR(255))
- 근거: dto/keyword/KeywordSubscribeRequest.java, service/keyword/KeywordCommandService.java:28-34, RES/schema.sql:61-69

### POL-KW-04 키워드 해지와 초기화

- 정책: 해지는 encodedKeyword로 합니다. 키워드 행 자체가 없으면 404이고, 구독하지 않은 키워드면 아무 일 없이 끝납니다. 전체 초기화도 있습니다.
- 값: POST /api/keyword/unsubscribe {encodedKeyword} → 204, 없는 키워드 404 AE-KEYWORD-NOT-FOUND. DELETE /api/keyword/subscriptions/reset → 204. 해지해도 keywords 행은 남습니다.
- 근거: orchestrator/KeywordOrchestrator.java:50-55, orchestrator/KeywordOrchestrator.java:63-67, service/keyword/KeywordCommandService.java:36-39, service/keyword/KeywordCommandService.java:55-72

### POL-KW-05 내 키워드 목록

- 정책: 구독 중인 키워드를 토픽 이름과 읽음 상태와 함께 돌려줍니다. 정렬 기준은 지정돼 있지 않습니다.
- 값: GET /api/keyword/userKeywords → [{encodedKeyword, koreanKeyword, searchKeyword, topicName(한국어 토픽명), isRead, lastReadAt}]
- 근거: orchestrator/KeywordOrchestrator.java:57-61, dto/keyword/KeywordResponse.java, repository/adapter/keyword/KeywordMemberJpaRepositoryAdapter.java:23-24

### POL-KW-06 푸시 매칭 규칙

- 정책: 새 공지가 들어오면 그 공지의 토픽에 등록된 키워드만 검사합니다. 공지 제목이나 본문에 키워드가 들어 있으면 매칭입니다. 대소문자는 구분하지 않고 부분 문자열로 비교합니다.
- 값: 비교 title.toLowerCase().contains(kw) || content.toLowerCase().contains(kw)(Java)
- 근거: service/keyword/KeywordQueryService.java:24-32, orchestrator/PushOrchestrator.java:40-46

### POL-KW-07 키워드 푸시 대상과 유형

- 정책: 매칭된 키워드마다 KEYWORD 유형 푸시를 따로 만들어, 그 키워드를 구독한 회원 전원의 활성 토큰으로 보냅니다. 토픽 구독 여부와 토픽 수신 설정은 보지 않습니다.
- 값: 알림 유형 KEYWORD, 제목 "{키워드} - [{토픽 한국어명}]"(POL-PUSH-05)
- 근거: service/push/PushClusterCommandService.java:76-105, repository/adapter/keyword/KeywordMemberJpaRepositoryAdapter.java:29-30, orchestrator/PushOrchestrator.java:43

### POL-KW-08 키워드 게시글 목록

- 정책: 키워드 탭의 게시글은 제목만 검사해 보여 줍니다. 전체 목록은 구독 키워드를 토픽별로 묶어 조회하고 각 글에 처음 맞은 키워드를 붙입니다. 단일 목록은 키워드 하나의 글을 보여 주고 그 키워드를 읽음으로 바꿉니다.
- 값: 전체 GET /api/event/getSubscribedPostsByKeyword — 토픽별 LOWER(title) LIKE %LOWER(키워드)%, created_at 내림차순, 기본 10개(토픽마다 10개를 가져와 합침). 단일 GET /api/event/getSubscribedPostsByKeyword/{keyword} — 경로 값은 search_keyword("{키워드}_{department}"), 구독하지 않은 키워드 404, 조건 title LIKE %키워드%.
- 근거: orchestrator/ClubEventOrchestrator.java:180-232, repository/adapter/clubevent/ClubEventJpaRepositoryAdapter.java:109-121, repository/adapter/keyword/KeywordMemberJpaRepositoryAdapter.java:38-40, service/clubevent/ClubEventQueryService.java:145-150
