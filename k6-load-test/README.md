# k6-load-test

k6 기반 부하 테스트 패키지입니다. 시나리오 단위로 폴더를 구성하며, 각 폴더는 독립적으로 실행 가능합니다.

---

## 패키지 구조

```
k6-load-test/
├── README.md                    ← 이 파일 (패키지 전체 가이드)
├── .env.example                 ← 환경변수 템플릿 (커밋됨)
├── .env.local                   ← 실제 환경변수 (커밋 제외 — .gitignore)
├── run.sh                       ← 시나리오 실행 스크립트
├── lib/
│   └── auth.js                  ← 공유 인증 유틸 (모든 시나리오에서 재사용)
├── results/                     ← k6 결과 JSON (커밋 제외 — .gitignore)
└── scenarios/
    └── <scenario-group>/        ← 시나리오 그룹 (기능·비교 단위)
        ├── README.md            ← 시나리오 목적, 실행 방법, 측정 지표 설명
        ├── seed.sql             ← (선택) 사전 DB 데이터
        ├── setup.js             ← (선택) 1회성 환경 세팅 (API 호출 등)
        ├── options.js           ← preset 옵션 + payload 빌더
        └── <scenario>.js        ← 부하 테스트 시나리오 파일
```

---

## 시나리오 추가 규칙

> AI 또는 개발자가 새 시나리오를 추가할 때 이 규칙을 따릅니다.

### 1. 폴더 생성

`scenarios/<scenario-group>/` 폴더를 생성합니다.
이름은 비교 대상 또는 측정 목적을 명확히 나타냅니다.

```
예: async-vs-sync-webhook, cache-vs-db-read, batch-vs-stream-send
```

### 2. 필수 파일

| 파일 | 역할 |
|------|------|
| `README.md` | 시나리오 목적, 사전 준비, 실행 명령, 측정 지표, 예상 결과 |
| `options.js` | `scenarioOptions` export + `buildXxxPayload()` 빌더 함수 |
| `<scenario>.js` | 실제 부하 테스트 (1개 이상) |

### 3. 선택 파일

| 파일 | 사용 시점 |
|------|---------|
| `seed.sql` | 테스트 데이터를 DB에 직접 삽입해야 할 때 |
| `setup.js` | API 호출로 1회성 환경 세팅이 필요할 때 (FCM 토큰 등록 등) |

### 4. 파일 작성 규칙

**options.js**
- `scenarioOptions`를 export합니다 (`sharedOptions` 아님).
- `buildXxxPayload(label, vu, iter)` 형태로 payload 빌더를 export합니다.
- preset은 항상 `single`, `ramp`, `rate` 3가지를 구현합니다.
- `rate` preset은 `.env.local`의 `RATE`, `DURATION`, `VUS`를 읽습니다.

```js
export const scenarioOptions = presets[__ENV.K6_PRESET || 'ramp'];
export function buildXxxPayload(label, vu, iter) { ... }
```

**scenario.js**
- `setup()`에서 크롤링 토큰 등 1회성 토큰만 발급합니다.
- 유저 세팅(FCM 등록, DB 삽입)은 `seed.sql` + `setup.js`에서 분리합니다.
- `default function`에서 `__VU`, `__ITER`을 payload에 포함해 요청을 고유하게 만듭니다.

```js
export const options = scenarioOptions;

export function setup() {
  return { crawlingToken: generateCrawlingToken() };
}

export default function(data) {
  const res = http.post(url, buildXxxPayload('[LABEL]', __VU, __ITER), { ... });
  check(res, { 'status is 200': (r) => r.status === 200 });
  sleep(1);
}
```

**seed.sql**
- 멱등성을 보장합니다 (`INSERT IGNORE`, `WHERE NOT EXISTS` 활용).
- 저장 프로시저로 N명 루프를 구현하고, 마지막에 `DROP PROCEDURE`로 정리합니다.
- 실행 명령을 파일 상단 주석에 명시합니다.

**setup.js**
- `vus: 1`, `iterations: parseInt(__ENV.USER_COUNT || '1')`으로 선언합니다.
- `__ITER + 1`을 인덱스로 사용해 `test1@...` ~ `testN@...` 순서로 처리합니다.
- `lib/auth.js`의 함수를 import해 사용합니다.

---

## 환경변수 규칙

k6는 shell 환경변수를 자동 상속하지 않습니다. `.env.local`에 작성하면 `run.sh`가 `-e KEY=VALUE`로 자동 주입합니다.

### 표준 환경변수 키

새 시나리오 작성 시 동일한 의미의 변수는 기존 키를 재사용합니다. **같은 의미인데 새 키를 만들지 않습니다.**

| 키 | 의미 | 예시 |
|----|------|------|
| `BASE_URL` | 서버 주소 | `http://localhost:8080` |
| `TEST_USER_EMAIL` | 단일 유저 모드 이메일 | `test@ajou.ac.kr` |
| `TEST_USER_FCM_TOKEN` | 브라우저 발급 FCM 토큰 | `dF3x...` |
| `USER_COUNT` | 테스트 대상 유저 수 | `100` |
| `TOPIC` | 구독할 토픽 영문명 | `AjouNormal` |
| `RATE` | `rate` preset의 초당 요청 수 | `5` |
| `DURATION` | `rate` preset의 지속 시간 | `30s` |
| `VUS` | `rate` preset의 예비 VU 수 | `10` |

> 새 시나리오에 필요한 변수가 위 목록에 없으면 `.env.example`에 추가하고 표에도 기록합니다.

### .env.local 설정 예시

```
BASE_URL=http://localhost:8080
TEST_USER_FCM_TOKEN=<브라우저에서 발급한 FCM 토큰>
USER_COUNT=100
TOPIC=AjouNormal
RATE=5
DURATION=30s
VUS=10
```

---

## 실행 방법

### 기본 흐름

```bash
# 1. (필요 시) DB 시드 실행
mysql -u <user> -p <database> < k6-load-test/scenarios/<group>/seed.sql

# 2. (필요 시) 1회성 환경 세팅
./k6-load-test/run.sh k6-load-test/scenarios/<group>/setup.js

# 3. 시나리오 실행
./k6-load-test/run.sh k6-load-test/scenarios/<group>/<scenario>.js [preset]
```

### Preset

| preset | 동작 | 사용 시점 |
|--------|------|---------|
| `single` (기본값) | 1 VU, 1회 요청 | 동작 확인 |
| `ramp` | VU 10 → 50 단계적 증가 | 부하 증가 패턴 측정 |
| `rate` | 초당 고정 RPS | 처리량 한계 측정 |

### 결과 파일

`k6-load-test/results/result-<scenario>-<preset>-<datetime>.json`에 자동 저장됩니다.

---

## 현재 시나리오 목록

| 시나리오 | 폴더 | 측정 목적 |
|---------|------|---------|
| async-vs-sync-webhook | `scenarios/async-vs-sync-webhook/` | FCM 비동기 vs 동기 전송의 서블릿 스레드 점유량·처리량 비교 |

---

## lib/auth.js — 공유 인증 유틸

| 함수 | 설명 |
|------|------|
| `loginWithFcm(email, fcmToken)` | FCM 토큰 등록 + JWT 발급 (`POST /api/v2/auth/test-login/fcm`) |
| `login(email)` | FCM 없이 JWT 발급 (`POST /api/v2/auth/test-login`) |
| `subscribeToTopic(accessToken, topic)` | 토픽 구독 (`POST /api/v2/topics/subscriptions`) |
| `generateCrawlingToken()` | 크롤링 토큰 발급 (`POST /api/v2/auth/test-crawling-token`) |
