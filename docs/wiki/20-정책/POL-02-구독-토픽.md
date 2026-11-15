---
type: 정책
status: 확정
source: policies.md 2장 · policies.json 2장
updated: 2026-11-15
tags: []
---

# 2장 구독 (토픽)

원문: `policies.md` 2장. 각 항목의 정책·값·근거는 추출 JSON 원문 그대로다.

### POL-SUB-01 토픽 정의와 개수

- 정책: 토픽은 공지를 수집하는 게시판 하나에 대응합니다. 백엔드에는 토픽 59개가 있고, 크롤러는 그중 TEST를 뺀 58개 게시판을 수집합니다.
- 값: 분류별 개수 학과 42 · 단과대 12 · 공지사항 2 · 대학원 1 · 기숙사 1 · 테스트 1. 크롤러 파서 유형 1(51개) · 5(4개) · 2(1개) · 3(1개) · 4(1개). / 참고: data.sql은 local 프로필에서만 실행됩니다(RES/application-local.yaml:10-12). 운영 topics 테이블은 같은 구성을 전제로 하지만 이 문서에서 운영 DB를 직접 조회하지는 않았습니다.
- 근거: domain/clubevent/Type.java:10-68(enum 59개), RES/data.sql:1-60(시드 59행), CR/config/notifierConfigs.json(58개)

### POL-SUB-02 토픽 식별자

- 정책: 토픽은 세 가지 이름을 가집니다. 구독·키워드 API는 영문 department로, 공지 분류는 대문자 type으로, 웹훅의 푸시 라우팅은 한국어 korean_topic으로 토픽을 찾습니다.
- 값: 예) department=Software, type=SOFTWARE, korean_topic=소프트웨어학과
- 근거: service/topic/TopicCommandService.java:27, service/clubevent/ClubEventCommandService.java:82-88, orchestrator/PushOrchestrator.java:31, domain/topic/Topic.java:34-48

### POL-SUB-03 전체 토픽 목록 조회

- 정책: GET /api/topic/all은 로그인 없이 전체 토픽을 표시 순서대로 돌려줍니다. 테스트 토픽도 포함됩니다.
- 값: 정렬 korean_order 오름차순. 응답 {classification, koreanOrder, koreanTopic}
- 근거: controller/TopicController.java:28-32, orchestrator/TopicOrchestrator.java:28-33, dto/topic/TopicDetailResponse.java

### POL-SUB-04 토픽 구독

- 정책: POST /api/topic/subscribe로 토픽 하나를 구독합니다. 구독하면 알림 수신이 켜진 상태로 시작합니다. 회원의 활성 FCM 토큰은 topic_tokens에도 연결합니다.
- 값: 요청 {topic: department}. 없는 토픽 404 AE-TOPIC-NOT-FOUND, 이미 구독 409 AE-TOPIC-ALREADY-SUBSCRIBED. 생성 값 is_read=false, receive_notification=true, last_read_at=now. 응답 204.
- 근거: orchestrator/TopicOrchestrator.java:35-39, service/topic/TopicCommandService.java:25-44, service/token/TokenService.java:32-44, RES/create-indexes.sql:30(회원·토픽 유일 키)

### POL-SUB-05 토픽 구독 해지

- 정책: POST /api/topic/unsubscribe는 토픽 구독과 토큰 연결을 지웁니다. 구독하지 않은 토픽이어도 오류 없이 끝납니다.
- 값: 요청 {topic: department}. 없는 토픽만 404. 응답 204.
- 근거: orchestrator/TopicOrchestrator.java:41-46, service/topic/TopicCommandService.java:46-49, service/token/TokenService.java:46-50

### POL-SUB-06 전체 구독 초기화

- 정책: DELETE /api/topic/subscriptions/reset은 회원의 토픽 구독과 토큰 연결을 모두 지웁니다.
- 값: 응답 204
- 근거: orchestrator/TopicOrchestrator.java:55-59, service/topic/TopicCommandService.java:51-60, service/token/TokenService.java:52-61

### POL-SUB-07 토픽별 알림 수신 끄기

- 정책: 구독은 유지한 채 토픽 푸시만 끌 수 있습니다. 끄면 그 토픽의 토픽 푸시와 알림함 기록 대상에서 빠집니다. 같은 토픽의 키워드 푸시는 이 설정과 상관없이 갑니다.
- 값: POST /api/topic/subscriptions/notification {topic, receiveNotification} → 204. 구독하지 않은 토픽 404 AE-TOPIC-SUBSCRIPTION-NOT-FOUND.
- 근거: service/topic/TopicCommandService.java:69-78, repository/adapter/topic/TopicMemberJpaRepositoryAdapter.java:28-29, service/push/PushClusterCommandService.java:80(키워드 대상 조회에는 수신 설정 조건 없음)

### POL-SUB-08 구독 상태 조회

- 정책: 전체 토픽에 구독 여부와 수신 여부를 붙여 보여 주는 API와, 구독한 토픽만 보여 주는 API가 있습니다.
- 값: GET /api/topic/subscriptionsStatus → {id, koreanTopic, englishTopic, classification, subscribed, receiveNotification, koreanOrder}, korean_order 오름차순. GET /api/topic/subscriptions → 구독 토픽, 토픽 id 내림차순.
- 근거: service/topic/TopicQueryService.java:49-70, orchestrator/TopicOrchestrator.java:48-53, dto/topic/TopicStatusResponse.java

### POL-SUB-09 가입 시 기본 구독

- 정책: 가입할 때 자동으로 구독되는 토픽이나 키워드는 없습니다.
- 값: 0개
- 근거: service/member/MemberService.java:24-35, orchestrator/AuthOrchestrator.java:43-59

### POL-SUB-10 구독 개수 제한

- 정책: 토픽 구독 개수에는 상한이 없습니다.
- 값: 무제한(59개 전부 가능)
- 근거: service/topic/TopicCommandService.java:25-44(개수 검사 없음)

### POL-SUB-11 구독 탭 읽음 표시

- 정책: GET /api/subscriptions/isSubscribedTabRead는 회원의 토픽 구독·키워드 구독 중 하나라도 읽지 않음이면 false를 돌려줍니다. 토픽은 해당 카테고리 목록(GET /api/event/{type})을 열 때, 키워드는 단일 키워드 목록(GET /api/event/getSubscribedPostsByKeyword/{keyword})을 열 때 읽음으로 바뀝니다.
- 값: 응답 {isSubscribedTabRead: boolean}(true = 모두 읽음) / 표시 순서는 korean_order 값입니다(55가 두 토픽에 중복). 크롤러가 쓰는 한국어 이름이 다르면 괄호에 적었습니다.
- 근거: orchestrator/SubscriptionOrchestrator.java:28-32, orchestrator/ClubEventOrchestrator.java:100-102, orchestrator/ClubEventOrchestrator.java:224-226, domain/topic/TopicMember.java:55-58

## 원문 표 1

| # | 표시 순서(korean_order) | 분류 | 토픽명(백엔드) | department (구독 키) | type | 크롤러 유형 | 게시판 URL | 근거 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | 1 | 단과대 | 간호대학 | Nursing | NURSING | 3 | https://www.ajoumc.or.kr/nursing/board/commBoardNRNewsList.do | data.sql:43 |
| 2 | 2 | 학과 | 건설시스템공학과 | CivilSystemsEngineering | CIVILSYSTEMSENGINEERING | 1 | https://ce.ajou.ac.kr/ce/board/notice.do | data.sql:13 |
| 3 | 3 | 학과 | 건축학과 | Architecture | ARCHITECTURE | 1 | https://arch.ajou.ac.kr/arch/board/total-notice.do | data.sql:7 |
| 4 | 4 | 단과대 | 경영대학 | Business | BUSINESS | 1 | https://biz.ajou.ac.kr/biz/community/notice.do | data.sql:9 |
| 5 | 5 | 학과 | 경영인텔리전스학과 | ManagementIntelligence | MANAGEMENTINTELLIGENCE | 1 | https://ebiz.ajou.ac.kr/ebiz/board/notice.do | data.sql:36 |
| 6 | 6 | 학과 | 경영학과 | BusinessAdministration | BUSINESSADMINISTRATION | 1 | https://abiz.ajou.ac.kr/abiz/board/notice.do | data.sql:10 |
| 7 | 7 | 학과 | 경제정치사회융합학부 | EconomicPoliticalAndSocialStudies | ECONOMICPOLITICALANDSOCIALSTUDIES | 5 | https://eps.ajou.ac.kr/eps/life/notice.do | data.sql:57 |
| 8 | 8 | 학과 | 경제학과 | Economics | ECONOMICS | 1 | https://econ.ajou.ac.kr/econ/activity/notice.do | data.sql:19 |
| 9 | 9 | 단과대 | 공과대학 | Engineering | ENGINEERING | 1 | https://eng.ajou.ac.kr/eng/community/notice.do | data.sql:21 |
| 10 | 10 | 학과 | 교통시스템공학과 | TransportationSystemsEngineering | TRANSPORTATIONSYSTEMSENGINEERING | 1 | https://tse.ajou.ac.kr/tse/board/board03.do | data.sql:53 |
| 11 | 11 | 학과 | 국방디지털융합학과 | MilitaryDigitalConvergence | MILITARYDIGITALCONVERGENCE | 1 | https://mdc.ajou.ac.kr/mdc/administration/notice.do | data.sql:41 |
| 12 | 12 | 학과 | 국어국문학과 | KoreanLanguageLiterature | KOREANLANGUAGELITERATURE | 1 | https://kor.ajou.ac.kr/kor/academic/notice.do | data.sql:35 |
| 13 | 13 | 단과대 | 국제학부대학 | International | INTERNATIONAL | 1 | https://isa.ajou.ac.kr/isa/support/notice.do | data.sql:34 |
| 14 | 14 | 학과 | 글로벌경영학과 | GlobalBusiness | GLOBALBUSINESS | 1 | https://gb.ajou.ac.kr/gb/admission/alram.do | data.sql:26 |
| 15 | 15 | 학과 | 금융공학과 | FinancialEngineering | FINANCIALENGINEERING | 1 | https://fe.ajou.ac.kr/fe/notice/notice.do | data.sql:24 |
| 16 | 16 | 학과 | 기계공학과 | MechanicalEngineering | MECHANICALENGINEERING | 1 | https://me.ajou.ac.kr/me/board/under-notice.do | data.sql:39 |
| 17 | 17 | 기숙사 | 기숙사 | Dormitory | DORMITORY | 1 | https://dorm.ajou.ac.kr/dorm/notice/notice.do | data.sql:54 |
| 18 | 18 | 단과대 | 다산학부대학 | Dasan | DASAN | 1 | https://uc.ajou.ac.kr/uc/comunity/cnotice.do | data.sql:17 |
| 19 | 19 | 대학원 | 대학원 | Graduate | GRADUATE | 1 | https://grad.ajou.ac.kr/gs/community/notice.do | data.sql:27 |
| 20 | 20 | 학과 | 디지털미디어학과 | DigitalMedia | DIGITALMEDIA | 1 | https://media.ajou.ac.kr/media/board/notice.do | data.sql:18 |
| 21 | 21 | 학과 | 문화콘텐츠학과 | CultureContents | CULTURECONTENTS | 1 | https://culture.ajou.ac.kr/culture/academic/notice.do | data.sql:15 |
| 22 | 22 | 학과 | 물리학과 | Physics | PHYSICS | 1 | https://physics.ajou.ac.kr/physics/board/notice.do | data.sql:45 |
| 23 | 23 | 학과 | 미래모빌리티공학과 (크롤러: AI모빌리티공학과) | AIMobilityEngineering | AIMOBILITYENGINEERING | 1 | https://mobility.ajou.ac.kr/mobility/board/notice.do | data.sql:2 |
| 24 | 24 | 학과 | 불어불문학과 | FrenchLanguageLiterature | FRENCHLANGUAGELITERATURE | 1 | https://france.ajou.ac.kr/france/notice/notice.do | data.sql:25 |
| 25 | 25 | 학과 | 사이버보안학과 | CyberSecurity | CYBERSECURITY | 5 | https://security.ajou.ac.kr/security/board/under-notice.do | data.sql:16 |
| 26 | 26 | 학과 | 사학과 | History | HISTORY | 1 | https://history.ajou.ac.kr/history/admission/notice.do | data.sql:28 |
| 27 | 27 | 단과대 | 사회과학대학 | SocialScience | SOCIALSCIENCE | 1 | https://coss.ajou.ac.kr/coss/community/community01.do | data.sql:49 |
| 28 | 28 | 학과 | 사회학과 | Sociology | SOCIOLOGY | 1 | https://soci.ajou.ac.kr/soci/activity/notice.do | data.sql:50 |
| 29 | 29 | 학과 | 산업공학과 | IndustrialEngineering | INDUSTRIALENGINEERING | 1 | https://ie.ajou.ac.kr/ie/academic/notice.do | data.sql:30 |
| 30 | 30 | 학과 | 생명과학과 | BiologicalScience | BIOLOGICALSCIENCE | 1 | https://biology.ajou.ac.kr/biolog/admission/notice.do | data.sql:8 |
| 31 | 31 | 단과대 | 소프트웨어융합대학 | ComputingInformatics | COMPUTINGINFORMATICS | 1 | https://sw.ajou.ac.kr/sw/board/notice.do | data.sql:14 |
| 32 | 32 | 학과 | 소프트웨어학과 | Software | SOFTWARE | 2 | http://software.ajou.ac.kr/bbs/board.php?tbl=notice | data.sql:51 |
| 33 | 33 | 학과 | 수학과 | Mathmatics | MATHMATICS | 1 | https://math.ajou.ac.kr/math/alram/notice.do | data.sql:38 |
| 34 | 34 | 학과 | 스포츠레저학과 | SportsLeisureStudies | SPORTSLEISURESTUDIES | 1 | https://slez.ajou.ac.kr/slez/activity/notice.do | data.sql:52 |
| 35 | 35 | 학과 | 심리학과 | Psychology | PSYCHOLOGY | 1 | https://apsy.ajou.ac.kr/apsy/activity/notice.do | data.sql:47 |
| 36 | 36 | 공지사항 | 아주대학교-일반 | AjouNormal | AJOUNORMAL | 1 | https://ajou.ac.kr/kr/ajou/notice.do | data.sql:3 |
| 37 | 37 | 공지사항 | 아주대학교-장학 | AjouScholarship | AJOUSCHOLARSHIP | 1 | https://ajou.ac.kr/kr/ajou/notice_scholarship.do | data.sql:4 |
| 38 | 38 | 단과대 | 약학대학 | Pharmacy | PHARMACY | 1 | https://pharm.ajou.ac.kr/pharm/board/notice.do | data.sql:44 |
| 39 | 39 | 학과 | 영어영문학과 | EnglishLanguageLiterature | ENGLISHLANGUAGELITERATURE | 1 | https://ell.ajou.ac.kr/english/dgree/notice.do | data.sql:22 |
| 40 | 40 | 학과 | 융합시스템공학과 | IntegrativeSystemsEngineering | INTEGRATIVESYSTEMSENGINEERING | 5 | https://ise.ajou.ac.kr/ise/board/notice.do | data.sql:32 |
| 41 | 41 | 학과 | 응용화학과 | AppliedChemistry | APPLIEDCHEMISTRY | 1 | https://appchem.ajou.ac.kr/appchem/college/notice.do | data.sql:56 |
| 42 | 42 | 학과 | 응용화학생명공학과 | AppliedChemistryBiologicalEngineering | APPLIEDCHEMISTRYBIOLOGICALENGINEERING | 1 | https://chembio.ajou.ac.kr/chembio/admission/notice.do | data.sql:6 |
| 43 | 43 | 단과대 | 의과대학 | Medicine | MEDICINE | 4 | https://www.ajoumc.or.kr/medicine/board/commBoardUVNoticeList.do | data.sql:40 |
| 44 | 44 | 학과 | 인공지능융합학과 | AppliedArtificialIntelligence | APPLIEDARTIFICIALINTELLIGENCE | 1 | https://aai.ajou.ac.kr/aai/dgree/notice.do | data.sql:5 |
| 45 | 45 | 단과대 | 인문대학 | Humanities | HUMANITIES | 1 | https://human.ajou.ac.kr/human/community/community01.do | data.sql:29 |
| 46 | 46 | 단과대 | 자연과학대학 | NaturalScience | NATURALSCIENCE | 1 | https://ns.ajou.ac.kr/ns/board/notice.do | data.sql:42 |
| 47 | 47 | 학과 | 자유전공학부 | LiberalStudies | LIBERALSTUDIES | 5 | https://pre.ajou.ac.kr/pre/community/notice.do | data.sql:59 |
| 48 | 48 | 학과 | 전자공학과 | ElectricalComputerEngineering | ELECTRICALCOMPUTERENGINEERING | 1 | https://ece.ajou.ac.kr/ece/bachelor/notice.do | data.sql:20 |
| 49 | 49 | 단과대 | 첨단ICT융합대학 | InformationTechnology | INFORMATIONTECHNOLOGY | 1 | https://it.ajou.ac.kr/it/board/notice.do | data.sql:31 |
| 50 | 50 | 학과 | 정치외교학과 | PoliticalScienceDiplomacy | POLITICALSCIENCEDIPLOMACY | 1 | https://pol.ajou.ac.kr/pol/activity/notice.do | data.sql:46 |
| 51 | 51 | 학과 | 지능형반도체공학과 | IntelligenceSemiconductorEngineering | INTELLIGENCESEMICONDUCTORENGINEERING | 1 | https://aisemi.ajou.ac.kr/aisemi/board/notice.do | data.sql:33 |
| 52 | 52 | 학과 | 첨단신소재공학과 | MaterialsScienceEngineering | MATERIALSSCIENCEENGINEERING | 1 | https://mse.ajou.ac.kr/mse/board/notice.do | data.sql:37 |
| 53 | 53 | 테스트 | 테스트 | Test | TEST | 수집 안 함 | - | data.sql:55 |
| 54 | 54 | 학과 | 프런티어과학학부 | FrontierSciences | FRONTIERSCIENCES | 1 | https://www.ajou.ac.kr/frontiers/board/notice.do | data.sql:58 |
| 55 | 55 | 학과 | 첨단바이오융합대학 | BioConvergenceEngineering | BIOCONVERGENCEENGINEERING | 1 | https://ibio.ajou.ac.kr/ibio/data/data01.do | data.sql:60 |
| 56 | 55 | 학과 | 행정학과 | PublicAdministration | PUBLICADMINISTRATION | 1 | https://pba.ajou.ac.kr/pba/activity/notice.do | data.sql:48 |
| 57 | 56 | 학과 | 화학공학과 | ChemicalEngineering | CHEMICALENGINEERING | 1 | https://che.ajou.ac.kr/che/board/notice.do | data.sql:11 |
| 58 | 57 | 학과 | 화학과 | Chemistry | CHEMISTRY | 1 | https://chem.ajou.ac.kr/chem/board/notice.do | data.sql:12 |
| 59 | 58 | 학과 | 환경안전공학과 | EnvironmentalSafetyEngineering | ENVIRONMENTALSAFETYENGINEERING | 1 | https://env.ajou.ac.kr/env/board/notice.do | data.sql:23 |
