# threadpool-isolation — 스레드풀 격리 부하 테스트

## 목적

FCM 비동기 콜백 처리 시 **Executor 전략**에 따른 서버 성능 차이를 실증합니다.

> "왜 FCM 콜백에 전용 스레드풀이 필요한가?"

## 비교 시나리오

| 구분 | Executor | 엔드포인트 | 파일 |
|---|---|---|---|
| 시나리오 1 (미분리) | `MoreExecutors.directExecutor()` | `POST /api/v2/test/webhook/direct-pool` | `direct-pool-webhook.js` |
| 시나리오 2 (격리, 현재) | `fcmCallbackExecutor` (전용 풀) | `POST /api/webhook/crawling` | `isolated-pool-webhook.js` |

### 시나리오 1 — directExecutor (스레드풀 미분리)

Firebase Admin Java SDK는 gRPC가 아닌 **HTTP 기반** (`google-http-client`)으로 FCM API를 호출합니다.
`sendEachAsync()`는 SDK 내부의 `firebase-worker-*` 스레드풀에서 HTTP 요청을 처리하고,
`directExecutor()`를 지정하면 해당 스레드가 콜백까지 직접 실행합니다.

```
Firebase.sendEachAsync(messages)         [firebase-worker-N 스레드]
  └─ HTTP 요청 완료 후 ApiFuture resolve
       └─ ApiFutures.addCallback(future, callback, directExecutor())
            └─ onSuccess() / onFailure() 가 firebase-worker-N 에서 직접 실행
                 └─ DB write (processPushResult) 이 firebase-worker-N 을 점유
                      └─ SDK가 다음 FCM HTTP 응답을 처리하지 못해 지연 누적
```

**고부하 시 예상 현상:**
- `http_req_duration` p99 / max 급등
- VisualVM: `firebase-worker-*` 스레드가 RUNNABLE 상태로 DB I/O 점유

### 시나리오 2 — fcmCallbackExecutor (스레드풀 격리)

```
Firebase.sendEachAsync(messages)         [firebase-worker-N 스레드]
  └─ HTTP 요청 완료 후 ApiFuture resolve
       └─ ApiFutures.addCallback(future, callback, fcmCallbackExecutor)
            └─ firebase-worker-N 즉시 반환 → 다음 HTTP 응답 처리 가능
                 └─ fcm-callback-* 전용 스레드에서 onSuccess() / onFailure() 처리
```

**고부하 시 예상 현상:**
- `http_req_duration` 안정적 유지
- VisualVM: `fcm-callback-*` 스레드가 콜백 처리, `firebase-worker-*` 스레드는 WAITING

## 실행 순서

### 1. DB 시드 데이터 삽입

```bash
mysql -u <user> -p <database> < k6-load-test/scenarios/threadpool-isolation/seed.sql
```

### 2. 시나리오 1 실행 (directExecutor)

```bash
# 동작 확인
./k6-load-test/run.sh k6-load-test/scenarios/threadpool-isolation/direct-pool-webhook.js single

# 부하 테스트 (ramp: 0→200 VU)
./k6-load-test/run.sh k6-load-test/scenarios/threadpool-isolation/direct-pool-webhook.js ramp

# 고정 RPS (rate: 30 RPS, 60s)
./k6-load-test/run.sh k6-load-test/scenarios/threadpool-isolation/direct-pool-webhook.js rate
```

### 3. 시나리오 2 실행 (격리 풀)

```bash
# 동작 확인
./k6-load-test/run.sh k6-load-test/scenarios/threadpool-isolation/isolated-pool-webhook.js single

# 부하 테스트 (ramp: 0→200 VU)
./k6-load-test/run.sh k6-load-test/scenarios/threadpool-isolation/isolated-pool-webhook.js ramp

# 고정 RPS (rate: 30 RPS, 60s)
./k6-load-test/run.sh k6-load-test/scenarios/threadpool-isolation/isolated-pool-webhook.js rate
```

## 관측 방법

### k6 출력 지표

| 지표 | 의미 | directExecutor | fcmCallbackExecutor |
|---|---|---|---|
| `http_req_duration` p95 | 95번째 백분위 응답시간 | 급등 | 안정 |
| `http_req_duration` p99 | 99번째 백분위 응답시간 | 타임아웃 가능 | bounded |
| `http_req_failed` | 에러율 | 증가 | 낮음 |
| `http_reqs` | 초당 처리량 | 감소 | 유지 |

### VisualVM

1. Thread 탭 → `firebase-worker-*` 스레드 상태 확인
   - directExecutor: **RUNNABLE** (DB I/O 점유 — 다음 FCM 응답 처리 불가)
   - fcmCallbackExecutor: **WAITING** (즉시 반환 — 다음 FCM 응답 처리 가능)
2. Thread 탭 → `fcm-callback-*` 스레드 확인 (격리 풀에서만 존재)
3. Monitor 탭 → Heap 사용량 추이 비교

### Java Flight Recorder (선택, 오버헤드 최소)

```bash
# 서버 실행 시 JFR 옵션 추가 (120초 기록)
java -XX:StartFlightRecording=filename=threadpool-test.jfr,duration=120s -jar app.jar

# JDK Mission Control로 분석
jmc threadpool-test.jfr
```

## 환경 변수 (.env.local)

```
BASE_URL=http://localhost:8080
RATE=30
DURATION=60s
VUS=200
```
