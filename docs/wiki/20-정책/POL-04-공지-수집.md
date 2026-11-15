---
type: 정책
status: 확정
source: policies.md 4장 · policies.json 4장
updated: 2026-11-15
tags: []
---

# 4장 공지 수집

원문: `policies.md` 4장. 각 항목의 정책·값·근거는 추출 JSON 원문 그대로다.

### POL-CRAWL-01 수집기와 대상

- 정책: 별도 Go 크롤러가 58개 게시판 첫 페이지를 읽고, 새 글을 백엔드 웹훅으로 보냅니다. 게시판마다 HTML 구조에 맞는 파서 유형(1~5)을 씁니다.
- 값: 게시판 58개(2.1 표), 유형 1 · 5는 아주대 CMS, 2는 소프트웨어학과, 3 · 4는 의료원(간호대학·의과대학)
- 근거: CR/main.go:19-38, CR/config/notifierConfigs.json, CR/models/NotifierConfig.go:3-8

### POL-CRAWL-02 수집 주기

- 정책: 환경변수 CRAWLING_PERIOD(초) 간격으로 58개 게시판을 동시에(게시판마다 고루틴 하나) 수집합니다. 앞 주기가 끝났는지는 확인하지 않습니다.
- 값: 운영 60초(노트 1.3·7.4 — 「매분 58번」, 6주기 348건)
- 근거: CR/main.go:40-55

### POL-CRAWL-03 새 글 판정

- 정책: 게시판마다 두 값을 DB(notice 테이블)에 저장해 두고 비교합니다. 고정 공지 개수가 늘면 늘어난 수만큼 위에서부터, 첫 일반 글의 목록 번호가 커지면 늘어난 수만큼 위에서부터 새 글로 봅니다. 값이 줄면 저장값만 낮추고 아무것도 보내지 않습니다.
- 값: 저장값 box(고정 공지 수), num(첫 일반 글 번호). 한 주기에 일반 글 최대 10건(소프트웨어학과는 15 − 고정 공지 수). 처음 보는 게시판은 두 값을 0으로 넣습니다.
- 근거: CR/src/notifiers/BaseNotifier.go:119-211, CR/src/utils/utils.go:148-182, CR/src/utils/utils.go:229-236

### POL-CRAWL-04 저장값 갱신 시점

- 정책: 저장값은 상세 페이지를 다 읽은 뒤, 웹훅을 보내기 전에 DB에 갱신합니다. 웹훅이 실패해도 다시 보내지 않습니다.
- 값: 재전송 0회
- 근거: CR/src/notifiers/BaseNotifier.go:153-160, CR/src/notifiers/BaseNotifier.go:203-210, CR/src/notifiers/BaseNotifier.go:61-64, CR/src/utils/utils.go:209-226

### POL-CRAWL-05 학교 서버 요청

- 정책: 게시판·본문 페이지 요청은 실패하면 짧게 쉬었다가 다시 시도합니다. 4xx(408·429 제외)는 다시 시도하지 않습니다.
- 값: 요청 타임아웃 15초, 최대 3회, 재시도 대상 네트워크 오류·408·429·5xx, 대기 300ms × 시도 횟수, User-Agent는 Chrome 129 문자열. 소프트웨어학과는 302 응답 본문을 그대로 파싱.
- 근거: CR/src/utils/utils.go:31-35, CR/src/utils/utils.go:238-358

### POL-CRAWL-06 웹훅 요청 형식

- 정책: 새 글 하나마다 백엔드 POST /api/webhook/crawling을 한 번 호출합니다. 매번 Redis에서 크롤링 토큰을 읽어 헤더에 넣습니다.
- 값: 헤더 crawling-token: {Redis "crawling-token" 값}. 본문 {id, category, title, department, date, url, content, images[], englishTopic, koreanTopic}. date는 게시일이 아니라 크롤링 시각(YYYY-MM-DDTHH:MM:SS). 대상 주소는 환경변수 WEBHOOK_ENDPOINT.
- 근거: CR/src/utils/utils.go:38-53, CR/src/utils/utils.go:185-227, CR/models/Notice.go:3-14, CR/src/notifiers/Type1Notifier.go:60-61, CR/src/notifiers/Type1Notifier.go:98-110, dto/webhook/WebhookRequest.java:9-40

### POL-CRAWL-07 웹훅 인증 토큰

- 정책: 백엔드는 매일 05:30에 크롤링 토큰을 새로 만들어 Redis에 저장하고, 웹훅 헤더 값이 Redis 값과 같아야 처리합니다. 앱 기동 시에는 토큰을 만들지 않습니다.
- 값: Redis 키 crawling-token, 16바이트 난수 Base64, TTL 90,000초(25시간). 불일치·없음 401 AE-WEBHOOK-INVALID-TOKEN. 헤더 자체가 없으면 500(처리기 없음).
- 근거: scheduler/CrawlingTokenScheduler.java:18-22, repository/adapter/webhook/CrawlingTokenCacheAdapter.java:17-36, service/webhook/WebhookService.java:17-21, RES/config/webhook.yaml:3, controller/WebhookController.java:21-26

### POL-CRAWL-08 백엔드 중복 판정

- 정책: 같은 토픽의 최신 공지 10건 중 제목과 URL이 모두 같은 글이 있으면 이미 처리한 공지로 보고 거절합니다.
- 값: 비교 범위 같은 type의 created_at 최신 10건. 409 AE-EVENT-DUPLICATE-NOTICE.
- 근거: service/clubevent/ClubEventCommandService.java:31-40, repository/port/clubevent/ClubEventRepositoryPort.java:74-76, repository/adapter/clubevent/ClubEventJpaRepositoryAdapter.java:100-107

### POL-CRAWL-09 토픽 검증

- 정책: englishTopic이 백엔드 Type 값이 아니면 저장하지 않습니다(크롤러가 본문을 못 읽어 보내는 빈 공지도 여기서 걸립니다).
- 값: 400 AE-EVENT-INVALID-TYPE(대소문자 무시 비교)
- 근거: service/clubevent/ClubEventCommandService.java:82-88, CR/src/notifiers/Type1Notifier.go:66

### POL-CRAWL-10 저장 항목

- 정책: 공지는 club_events에 저장하고 이미지는 club_event_images에 따로 저장합니다. 작성 시각은 크롤러 값이 아니라 서버가 받은 시각입니다.
- 값: title, content(전체, TEXT), content_preview(앞 200자), writer=department, subject=koreanTopic, url, type, created_at=수신 시각, likes_count=0, view_count=0. 이미지가 없으면 아주대 심볼 이미지(https://www.ajou.ac.kr/_res/ajou/kr/img/intro/img-symbol.png) 1장. id·category·date는 저장하지 않음.
- 근거: service/clubevent/ClubEventCommandService.java:25-26, service/clubevent/ClubEventCommandService.java:42-80, service/clubevent/ClubEventCommandService.java:90-95, domain/clubevent/ClubEvent.java:38-69

### POL-CRAWL-11 저장 후 캐시 무효화와 응답

- 정책: 공지를 저장하면 그 토픽의 목록 캐시와 인기글 캐시를 지우고, 푸시를 만든 뒤 바로 응답합니다(발송 완료를 기다리지 않음).
- 값: 응답 200 {message: "Webhook processed successfully.", englishTopic, title, eventId}
- 근거: service/clubevent/ClubEventCommandService.java:76-77, orchestrator/WebhookOrchestrator.java:22-42
