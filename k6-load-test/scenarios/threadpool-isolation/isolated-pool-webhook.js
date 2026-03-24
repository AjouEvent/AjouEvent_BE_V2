/**
 * 스레드풀 격리 웹훅 부하 테스트 (시나리오 2 — 현재 구현)
 *
 * 엔드포인트: POST /api/webhook/crawling
 * FCM 콜백 Executor: fcmCallbackExecutor (전용 격리 풀)
 *   core=4, max=16, queue=100, prefix=fcm-callback-
 *   Firebase Admin SDK는 HTTP 기반(google-http-client)으로 FCM API를 호출하며,
 *   sendEachAsync()는 내부 firebase-worker-* 스레드풀에서 HTTP 요청을 처리합니다.
 *   → ApiFuture 완료 시 콜백을 fcmCallbackExecutor 에 위임 후 firebase-worker-N 즉시 반환
 *   → firebase-worker-N 이 다음 FCM HTTP 응답을 지연 없이 처리
 *   → fcm-callback-* 전용 스레드에서 DB write(processPushResult) 실행
 *   → 고부하에서도 HTTP 응답 시간 안정적 유지
 *
 * 관찰 포인트:
 *   k6: p95/p99/max 안정적 유지, http_req_failed 낮음
 *   VisualVM Thread 탭: fcm-callback-* 스레드가 콜백 처리, firebase-worker-* 는 WAITING
 *   VisualVM Monitor 탭: Heap 안정적
 *
 * 실행 전 준비:
 *   1. seed.sql 실행 (DB 멤버 + 토픽 구독 + 가짜 FCM 토큰 등록)
 *
 * 실행:
 *   ./k6-load-test/run.sh k6-load-test/scenarios/threadpool-isolation/isolated-pool-webhook.js single
 *   ./k6-load-test/run.sh k6-load-test/scenarios/threadpool-isolation/isolated-pool-webhook.js ramp
 *   ./k6-load-test/run.sh k6-load-test/scenarios/threadpool-isolation/isolated-pool-webhook.js rate
 */
import http from 'k6/http';
import { check } from 'k6';
import { scenarioOptions, buildWebhookPayload } from './options.js';
import { generateCrawlingToken } from '../../lib/auth.js';

export const options = scenarioOptions;

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export function setup() {
  const crawlingToken = generateCrawlingToken();
  console.log(`[setup] 크롤링 토큰 발급 완료`);
  console.log(`[time] 테스트 시작: ${new Date().toLocaleTimeString('ko-KR')}`);
  return { crawlingToken };
}

export function teardown() {
  console.log(`[time] 테스트 종료: ${new Date().toLocaleTimeString('ko-KR')}`);
}

export default function (data) {
  const res = http.post(
    `${BASE_URL}/api/webhook/crawling`,
    buildWebhookPayload('[ISOLATED]', __VU, __ITER),
    {
      headers: {
        'Content-Type': 'application/json',
        'crawling-token': data.crawlingToken,
      },
    },
  );

  check(res, {
    'status is 200': (r) => r.status === 200,
  });
}
