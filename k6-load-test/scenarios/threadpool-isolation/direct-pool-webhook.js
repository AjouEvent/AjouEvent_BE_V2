/**
 * 스레드풀 미분리 웹훅 부하 테스트 (시나리오 1)
 *
 * 엔드포인트: POST /api/v2/test/webhook/direct-pool
 * FCM 콜백 Executor: MoreExecutors.directExecutor()
 *   Firebase Admin SDK는 HTTP 기반(google-http-client)으로 FCM API를 호출하며,
 *   sendEachAsync()는 내부 firebase-worker-* 스레드풀에서 HTTP 요청을 처리합니다.
 *   → directExecutor()를 사용하면 firebase-worker-N 스레드가 콜백(DB write)까지 직접 실행
 *   → firebase-worker-N 이 DB I/O를 처리하는 동안 다음 FCM HTTP 응답을 받지 못함
 *   → sendEachAsync() 처리 지연 누적 → HTTP 응답 시간 급등 → k6 p95/p99/max 상승
 *
 * 관찰 포인트:
 *   k6: p95/p99/max 트렌드, http_req_failed 에러율
 *   VisualVM Thread 탭: firebase-worker-* 스레드가 RUNNABLE 상태로 DB I/O 점유
 *   VisualVM Monitor 탭: Heap 완만한 증가
 *
 * 실행 전 준비:
 *   1. seed.sql 실행 (DB 멤버 + 토픽 구독 + 가짜 FCM 토큰 등록)
 *
 * 실행:
 *   ./k6-load-test/run.sh k6-load-test/scenarios/threadpool-isolation/direct-pool-webhook.js single
 *   ./k6-load-test/run.sh k6-load-test/scenarios/threadpool-isolation/direct-pool-webhook.js ramp
 *   ./k6-load-test/run.sh k6-load-test/scenarios/threadpool-isolation/direct-pool-webhook.js rate
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
    `${BASE_URL}/api/v2/test/webhook/direct-pool`,
    buildWebhookPayload('[DIRECT]', __VU, __ITER),
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
