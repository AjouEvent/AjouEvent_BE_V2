/**
 * 크롤링 토큰 사전 발급 스크립트 (1회성 실행)
 *
 * seed.sql 실행 후, 시나리오 실행 전에 동작 확인 용도로 사용합니다.
 * FCM 토큰은 seed.sql에서 가짜 토큰(load_test_token_N)으로 직접 삽입하므로
 * 이 스크립트에서 별도로 FCM 등록을 수행하지 않습니다.
 *
 * 실행 순서:
 *   1. seed.sql 실행 → 멤버 + FCM 토큰 + 토픽 구독 DB 등록
 *   2. ./k6-load-test/run.sh k6-load-test/scenarios/async-vs-sync-webhook/async-webhook.js [preset]
 *   3. ./k6-load-test/run.sh k6-load-test/scenarios/async-vs-sync-webhook/sync-webhook.js  [preset]
 */
import { generateCrawlingToken } from '../../lib/auth.js';
import { check } from 'k6';

export const options = {
  vus: 1,
  iterations: 1,
};

export default function () {
  const crawlingToken = generateCrawlingToken();

  check(crawlingToken, {
    '[setup] 크롤링 토큰 발급 성공': (t) => t !== null && t !== undefined,
  });

  console.log(`[setup] 크롤링 토큰: ${crawlingToken}`);
}
