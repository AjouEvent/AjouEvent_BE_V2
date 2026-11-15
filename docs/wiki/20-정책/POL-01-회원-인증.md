---
type: 정책
status: 확정
source: policies.md 1장 · policies.json 1장
updated: 2026-11-15
tags: []
---

# 1장 회원 · 인증

원문: `policies.md` 1장. 각 항목의 정책·값·근거는 추출 JSON 원문 그대로다.

### POL-AUTH-01 가입·로그인 수단

- 정책: 가입과 로그인은 Google OAuth 인가 코드 방식 하나뿐입니다. 이메일·비밀번호 로그인은 없습니다. 클라이언트가 Google에서 받은 인가 코드를 POST /api/users/oauth로 보내면 서버가 토큰으로 교환합니다.
- 값: 요청 {authorizationCode, redirectUri, fcmToken(선택)}. 인가 코드는 URL 디코딩 후 사용. Google 요청 범위 email, profile, https://www.googleapis.com/auth/calendar, https://www.googleapis.com/auth/user.organization.read. 교환 실패 시 400 AE-AUTH-INVALID-AUTHORIZATION-CODE.
- 근거: controller/AuthController.java:29-35, dto/auth/OauthRequest.java, service/auth/OauthService.java:42-93, RES/config/oauth.yaml:9-13, config/SecurityConfig.java:85-87(formLogin·httpBasic·oauth2Login 비활성)

### POL-AUTH-02 자동 회원 가입과 저장 항목

- 정책: 로그인한 Google 이메일로 회원을 찾고, 없으면 그 자리에서 회원을 만듭니다(별도 가입 절차 없음). 학과는 Google People API의 소속 정보에서 가져오고, 못 가져오면 비워 둡니다.
- 값: 저장 항목 email(필수·유일), name(Google 이름), major(People API organizations[0].department, 실패 시 null), role(기본 USER). 이메일 도메인(아주대 계정 여부) 검사는 없습니다.
- 근거: service/member/MemberService.java:24-35, service/auth/OauthService.java:79-112, domain/member/Member.java:36-47, RES/create-indexes.sql:9(email 유일 키)

### POL-AUTH-03 신규 가입 알림

- 정책: 새 회원이 생기면 Discord 회원 채널에 가입 메시지를 보냅니다. 전송에 실패해도 로그인은 그대로 진행합니다.
- 값: 문구 "{회원 id}번째 유저 {이름}({학과 또는 '미입력'}) 님이 회원가입했습니다!"
- 근거: orchestrator/AuthOrchestrator.java:46-48, orchestrator/AuthOrchestrator.java:85-97

### POL-AUTH-04 로그인 시 부가 처리

- 정책: 로그인 요청에 FCM 토큰이 있으면 토큰을 등록하고(POL-PUSH-14), Google이 refresh token을 내려 주면 캘린더 연동용으로 저장합니다(POL-CAL-02).
- 값: 없음
- 근거: orchestrator/AuthOrchestrator.java:49-57

### POL-AUTH-05 로그인 응답

- 정책: access token은 응답 본문으로, refresh token은 쿠키로 내려 줍니다.
- 값: 본문 {id, grantType: "Authorization", accessToken, name, major, email, isNewMember}
- 근거: controller/AuthController.java:30-35, controller/AuthController.java:45-47, dto/auth/LoginResponse.java

### POL-AUTH-06 토큰 종류와 만료

- 정책: 서버는 HMAC 서명 JWT 두 종류를 발급합니다. access token에는 회원 id·이메일·역할을 담고, refresh token에는 회원 id만 담습니다. 두 토큰 모두 같은 키와 같은 issuer를 쓰며, 토큰 종류를 구분하는 클레임은 없습니다.
- 값: access sub=회원 id, 클레임 memberId, email, role, iss. refresh sub, iss. 만료 시간은 환경변수 JWT_ACCESS_TOKEN_EXPIRATION, JWT_REFRESH_TOKEN_EXPIRATION(밀리초, 저장소에 값 없음). 서명 키는 JWT_SECRET(Base64).
- 근거: common/util/JwtUtil.java:26-59, RES/config/jwt.yaml:1-5, config/properties/JwtProperties.java:14-18

### POL-AUTH-07 refresh 쿠키

- 정책: refresh token은 HttpOnly 쿠키로만 주고받습니다.
- 값: 이름 refreshToken, HttpOnly, Secure(prod true · local false), Path=/, Max-Age=refresh 만료 시간, SameSite=None
- 근거: controller/AuthController.java:24, controller/AuthController.java:56-64, RES/application-prod.yaml:13-14, RES/application-local.yaml:34-35

### POL-AUTH-08 토큰 재발급

- 정책: PATCH /api/users/reissue-token은 refresh 쿠키를 검증한 뒤 access·refresh 토큰을 둘 다 새로 발급합니다. 서버가 refresh token을 저장하지 않으므로, 이전 refresh token은 만료될 때까지 계속 쓸 수 있습니다.
- 값: 쿠키가 없으면 401 AE-AUTH-UNAUTHORIZED. 서명 오류·issuer 불일치 401 AE-AUTH-INVALID-TOKEN, 만료 401 AE-AUTH-EXPIRED-TOKEN. 응답의 isNewMember는 null.
- 근거: controller/AuthController.java:37-43, orchestrator/AuthOrchestrator.java:61-65, service/auth/AuthService.java:21-24, common/util/JwtUtil.java:65-94, common/exception/GlobalExceptionHandler.java:39-44

### POL-AUTH-09 요청 인증 방식

- 정책: 인증이 필요한 API는 Authorization: Bearer {access token} 헤더로 인증합니다. 필터가 토큰을 검증하고 회원을 DB에서 다시 읽어 요청에 붙입니다. 세션은 쓰지 않습니다. 헤더에 잘못된 토큰이 있으면 비인증 허용 경로라도 401로 끝납니다.
- 값: 세션 정책 STATELESS. 회원이 DB에 없으면 401 AE-AUTH-UNAUTHORIZED.
- 근거: common/auth/JwtAuthFilter.java:33-56, common/auth/CustomUserDetailsService.java:19-23, config/SecurityConfig.java:79-96

### POL-AUTH-10 비인증 허용 경로

- 정책: 아래 경로는 로그인 없이 호출할 수 있습니다. 나머지는 모두 인증이 필요합니다. @AuthUser 파라미터를 받는 API는 경로가 허용 목록 패턴에 걸려도 비로그인이면 401을 돌려줍니다.
- 값: /swagger-ui/, /v3/api-docs/, /swagger-ui.html, /api/users/oauth, /api/users/reissue-token, /api/users/logout, /api/topic/all, /api/webhook/crawling, /api/event/banner, /api/event/popular, /api/event/subscribed, /api/event/detail/, /api/event/{type}. local 프로필에서만 /api/v2/auth/test-login, /api/v2/auth/test-login/fcm, /api/v2/auth/test-crawling-token 추가. /actuator/는 별도 체인에서 전부 허용(관리 포트 9090, 노출 health, prometheus).
- 근거: config/SecurityConfig.java:44-59, config/SecurityConfig.java:61-69, common/auth/AuthArgumentResolver.java:38-44, RES/application.yaml:21-33

### POL-AUTH-11 로그아웃

- 정책: POST /api/users/logout은 refresh 쿠키를 만료시키기만 합니다. 서버 쪽에서 토큰을 무효로 만들지 않으므로 이미 발급된 access·refresh token은 만료 시각까지 유효합니다.
- 값: 응답 204, Max-Age=0 쿠키
- 근거: controller/AuthController.java:49-54, controller/AuthController.java:66-74

### POL-AUTH-12 회원 탈퇴

- 정책: DELETE /api/users는 회원 행을 실제로 삭제합니다(소프트 삭제 아님). JPA cascade로 FCM 토큰, 찜, 토픽 구독, 키워드 구독이 함께 지워집니다. 알림함·발송 기록, 캘린더 토큰 파일, 공지의 찜 수(likes_count)는 처리하지 않습니다.
- 값: 함께 삭제 tokens, event_likes, topic_members, keyword_members. 삭제하지 않음 push_notifications, push_cluster_tokens(→ 알려진 이슈 1), 캘린더 refresh token 파일.
- 근거: controller/MemberController.java:46-50, service/member/MemberService.java:71-75, domain/member/Member.java:49-60, RES/schema.sql:160-195

### POL-AUTH-13 회원 정보 조회·수정

- 정책: 회원은 자기 이름·이메일·학과를 조회하고, 이름과 학과를 고칠 수 있습니다. null로 보낸 항목은 바꾸지 않습니다. register-info는 학과만 반영합니다.
- 값: GET /api/users → {name, email, major}. PATCH /api/users {name, major} → 204. POST /api/users/register-info {major} → 201 {email, name}. 길이·형식 검증 없음.
- 근거: controller/MemberController.java:28-44, orchestrator/MemberOrchestrator.java:21-32, service/member/MemberService.java:47-60, domain/member/Member.java:69-72

### POL-AUTH-14 역할

- 정책: 역할은 USER와 ADMIN 두 가지이고 가입 시 USER입니다. ADMIN 전용 API는 없습니다(@AuthAdmin을 쓰는 곳이 없음).
- 값: Role { USER, ADMIN }, 권한 이름 ROLE_{role}
- 근거: domain/member/Role.java:6-9, domain/member/Member.java:45-47, common/auth/CustomUserDetails.java:20-22, common/auth/AuthArgumentResolver.java:48-50

### POL-AUTH-15 CORS

- 정책: 허용 출처는 환경변수로 정하고, 쿠키를 포함한 요청을 허용합니다.
- 값: 출처 CORS_ALLOWED_ORIGINS(prod 기본 https://dev.ajouevent.com, 공통 기본 http://localhost:3000,http://localhost:8080), 메서드 GET·POST·PUT·DELETE·PATCH, 헤더 Authorization·Content-Type, credentials 허용, preflight 캐시 3600초
- 근거: config/SecurityConfig.java:106-114, RES/application.yaml:54-55, RES/application-prod.yaml:10-11
