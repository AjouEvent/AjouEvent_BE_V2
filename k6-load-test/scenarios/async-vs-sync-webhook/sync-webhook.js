/**
 * 동기 FCM 웹훅 부하 테스트
 *
 * 엔드포인트: POST /api/v2/test/webhook/sync
 * FCM 전송: sendEach 블로킹 (FCM 응답 올 때까지 서블릿 스레드 점유)
 *
 * 실행 전 준비:
 *   1. seed.sql 실행 (DB 멤버 + 토픽 구독 등록)
 *   2. ./load-test/run.sh load-test/scenarios/async-vs-sync-webhook/setup.js (FCM 토큰 등록)
 *
 * 실행:
 *   ./load-test/run.sh load-test/scenarios/async-vs-sync-webhook/sync-webhook.js single
 *   ./load-test/run.sh load-test/scenarios/async-vs-sync-webhook/sync-webhook.js ramp
 *   ./load-test/run.sh load-test/scenarios/async-vs-sync-webhook/sync-webhook.js rate
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
    `${BASE_URL}/api/v2/test/webhook/sync`,
    buildWebhookPayload('[SYNC]', __VU, __ITER),
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
