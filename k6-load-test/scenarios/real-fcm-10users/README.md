# real-fcm-10users 부하 테스트

## 목적

실제 FCM 토큰 10개를 사용해 **진짜 FCM 네트워크 왕복**이 포함된 환경에서
비동기 vs 동기 서블릿 스레드 점유 차이를 측정합니다.

가짜 토큰은 FCM이 즉시 거부(~10ms)하지만, 실제 토큰은 FCM 왕복에 **100~500ms**가
걸리므로 동기 경로의 스레드 블로킹 효과가 훨씬 뚜렷하게 나타납니다.

| 항목 | 비동기 | 동기 |
|------|--------|------|
| 엔드포인트 | `POST /api/webhook/crawling` | `POST /api/v2/test/webhook/sync` |
| FCM 전송 | `sendEachAsync` (Firebase 스레드풀) | `sendEach` (서블릿 스레드 블로킹) |
| FCM 토큰 | 실제 10개 (브라우저 발급) | 동일 |

---

## 사전 준비

### 1. FCM 토큰 10개 발급

`fcm-test-frontend`를 서로 다른 브라우저 탭/창 10개에서 열어 각각 FCM 토큰을 발급합니다.

### 2. .env.local 설정

```
BASE_URL=http://localhost:8080
FCM_TOKENS=token1,token2,token3,token4,token5,token6,token7,token8,token9,token10
TOPIC=AjouNormal
```

> 쉼표 사이에 공백 없이 붙여씁니다.

### 3. DB 시드 실행

real1~real10@ajou.ac.kr 멤버를 등록합니다.

```bash
mysql -u <user> -p <database> < k6-load-test/scenarios/real-fcm-10users/seed.sql
```

### 4. FCM 토큰 등록 + 토픽 구독

각 멤버에 실제 FCM 토큰을 등록하고 AjouNormal을 구독시킵니다.

```bash
./k6-load-test/run.sh k6-load-test/scenarios/real-fcm-10users/setup.js
```

- `real1@ajou.ac.kr` → `FCM_TOKENS`의 1번째 토큰
- `real2@ajou.ac.kr` → `FCM_TOKENS`의 2번째 토큰
- ...
- `real10@ajou.ac.kr` → `FCM_TOKENS`의 10번째 토큰

---

## 테스트 실행

서버 재기동 후 한 쪽씩 실행합니다 (DB 상태 편향 방지).

```bash
# 비동기 먼저
./k6-load-test/run.sh k6-load-test/scenarios/real-fcm-10users/async-webhook.js ramp

# 서버 재기동 후 동기
./k6-load-test/run.sh k6-load-test/scenarios/real-fcm-10users/sync-webhook.js ramp
```

> 테스트 중 실제 기기 10개에 알림이 전송됩니다. 무음 설정을 권장합니다.

---

## 파일 구조

```
real-fcm-10users/
├── README.md           ← 이 파일
├── seed.sql            ← real1~real10@ajou.ac.kr 멤버 등록
├── setup.js            ← 실제 FCM 토큰 등록 + 토픽 구독 (1회성)
├── options.js          ← 시나리오 옵션 + payload 빌더
├── async-webhook.js    ← 비동기 FCM 부하 테스트
└── sync-webhook.js     ← 동기 FCM 부하 테스트
```
