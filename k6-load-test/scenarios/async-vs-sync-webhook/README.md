# async-vs-sync-webhook 부하 테스트

## 목적

FCM 전송 방식 **비동기 vs 동기**의 서블릿 스레드 점유량·처리량 차이를 측정합니다.

| 항목 | 비동기 (`async-webhook.js`) | 동기 (`sync-webhook.js`) |
|------|---------------------------|------------------------|
| 엔드포인트 | `POST /api/webhook/crawling` | `POST /api/v2/test/webhook/sync` |
| FCM 전송 | `sendEachAsync` (Firebase 내부 스레드풀) | `sendEach` (서블릿 스레드 블로킹) |
| 서블릿 스레드 | FCM I/O 동안 즉시 반환 | FCM 응답 올 때까지 점유 |
| DB 로직 | 완전히 동일 | 완전히 동일 |

두 시나리오는 **FCM I/O 메서드 1개만 다릅니다.** 나머지 Redis 검증·DB 쓰기·메시지 빌드 로직은 동일하므로 공정한 비교가 가능합니다.

---

## 사전 준비

### 1. .env.local 설정

```
BASE_URL=http://localhost:8080
```

### 2. DB 시드 실행

테스트 멤버(test1 ~ test100@ajou.ac.kr), 가짜 FCM 토큰(`load_test_token_N`), AjouNormal 토픽 구독을 일괄 등록합니다.

```bash
mysql -u <user> -p <database> < k6-load-test/scenarios/async-vs-sync-webhook/seed.sql
```

- CLI·GUI 등 방법은 무관합니다. sql 파일을 실행해주세요.
- **가짜 FCM 토큰**: `tokens.token_value`는 단독 UNIQUE 제약이 있어 동일한 실제 FCM 토큰을 여러 멤버에 등록할 수 없습니다. 대신 멤버마다 고유한 가짜 토큰을 삽입합니다. FCM은 이를 거부하지만 서버는 N개 메시지를 빌드·전송 시도하므로 부하 측정에 충분합니다.
- **멱등성 보장**: 이미 존재하는 멤버·토큰·구독은 중복 삽입되지 않습니다.
- `USER_COUNT`를 변경했다면 `seed.sql` 하단의 `CALL seed_async_vs_sync_webhook(100)` 숫자도 맞춰 수정하세요.

---

## 테스트 실행

Spring Boot 로컬 서버를 기동한 뒤 실행합니다.

```bash
# 비동기 테스트
./k6-load-test/run.sh k6-load-test/scenarios/async-vs-sync-webhook/async-webhook.js single

# 동기 테스트 (서버 재기동 후 — 스레드 상태 초기화)
./k6-load-test/run.sh k6-load-test/scenarios/async-vs-sync-webhook/sync-webhook.js single
```

### Preset 옵션

| preset | 설명 | 사용 시점 |
|--------|------|---------|
| `single` | 1 VU, 1회 요청 | 동작 확인 |
| `ramp` (기본값) | VU 10 → 50 단계별 증가 | 스레드 점유 차이 측정 |
| `rate` | 초당 고정 RPS | 처리량(RPS) 한계 측정 |

```bash
# preset 지정 예시
./k6-load-test/run.sh k6-load-test/scenarios/async-vs-sync-webhook/sync-webhook.js single
./k6-load-test/run.sh k6-load-test/scenarios/async-vs-sync-webhook/sync-webhook.js rate
```

`rate` preset은 `.env.local`의 `RATE`, `DURATION`, `VUS` 값을 참고합니다.

---

## 측정 지표

### k6 결과 (`k6-load-test/results/` 저장)

| 지표 | 의미 |
|------|------|
| `http_req_duration` p95 | 동기 시 FCM 왕복 시간이 응답 지연에 반영됨 |
| `http_reqs` (RPS) | 서블릿 스레드가 FCM I/O에 묶이면 처리량 감소 |
| `http_req_failed` | 스레드 고갈 시 503 에러율 증가 |

### VisualVM (스레드 모니터링)

1. VisualVM 실행 → 로컬 JVM 프로세스 클릭 → **Threads** 탭
2. `http-nio` 스레드의 RUNNABLE / WAITING 수 비교
3. **Thread Dump** 버튼 → 정확히 어느 코드 라인에서 블로킹 발생하는지 확인

---

## 파일 구조

```
async-vs-sync-webhook/
├── README.md           ← 이 파일
├── seed.sql            ← DB 시드 (멤버 + 가짜 FCM 토큰 + 토픽 구독 일괄 등록)
├── setup.js            ← 동작 확인용 크롤링 토큰 발급 스크립트 (선택)
├── options.js          ← 시나리오 옵션 (preset, payload 빌더)
├── async-webhook.js    ← 비동기 FCM 부하 테스트
└── sync-webhook.js     ← 동기 FCM 부하 테스트
```

---

## 예상 결과

- **비동기**: FCM I/O 동안 서블릿 스레드 즉시 반환 → 높은 RPS, 낮은 p95 응답시간
- **동기**: FCM I/O(수백 ms) 동안 서블릿 스레드 점유 → VU 증가 시 RPS 급감, p95 급증, WAITING 스레드 급증
