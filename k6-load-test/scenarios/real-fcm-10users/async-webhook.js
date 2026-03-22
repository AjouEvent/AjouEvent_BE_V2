/**
 * 비동기 FCM 웹훅 부하 테스트 (실제 FCM 토큰 10명)
 *
 * 엔드포인트: POST /api/webhook/crawling
 * FCM 전송: sendEachAsync — 서블릿 스레드 즉시 반환, FCM I/O는 Firebase 스레드풀
 *
 * 실행 전 준비:
 *   1. seed.sql 실행
 *   2. ./k6-load-test/run.sh k6-load-test/scenarios/real-fcm-10users/setup.js
 *
 * 실행:
 *   ./k6-load-test/run.sh k6-load-test/scenarios/real-fcm-10users/async-webhook.js single
 *   ./k6-load-test/run.sh k6-load-test/scenarios/real-fcm-10users/async-webhook.js ramp
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
    buildWebhookPayload('[ASYNC]', __VU, __ITER),
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
