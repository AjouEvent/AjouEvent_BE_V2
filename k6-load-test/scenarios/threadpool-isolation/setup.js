/**
 * 실제 FCM 토큰 등록 스크립트 (1회성 실행)
 *
 * seed.sql 실행 후, 시나리오 실행 전에 1회만 실행합니다.
 * real1~realN@ajou.ac.kr 각각에 실제 FCM 토큰을 등록하고 AjouNormal을 구독시킵니다.
 *
 * 실행 순서:
 *   1. seed.sql 실행 → 멤버 DB 등록 (가짜 FCM 토큰 없음)
 *   2. .env.local 에 FCM_TOKENS, TOPIC 설정
 *   3. ./k6-load-test/run.sh k6-load-test/scenarios/threadpool-isolation/setup.js  ← 이 파일
 *   4. ./k6-load-test/run.sh k6-load-test/scenarios/threadpool-isolation/direct-pool-webhook.js [preset]
 *   5. ./k6-load-test/run.sh k6-load-test/scenarios/threadpool-isolation/isolated-pool-webhook.js [preset]
 *
 * .env.local 설정:
 *   FCM_TOKENS=token1,token2,...,tokenN   (쉼표로 구분, 공백 없이)
 *   TOPIC=AjouNormal
 *
 * 주의:
 *   실제 FCM 토큰은 브라우저 DevTools → Application → Firebase → Messaging 에서 확인할 수 있습니다.
 */
import { loginWithFcm, subscribeToTopic } from '../../lib/auth.js';
import { check } from 'k6';

const tokens = (__ENV.FCM_TOKENS || '').split(',');
const topic = __ENV.TOPIC || 'AjouNormal';

export const options = {
  vus: 1,
  iterations: tokens.length,
};

export default function () {
  const i = __ITER + 1;
  const email = `real${i}@ajou.ac.kr`;
  const fcmToken = tokens[__ITER].trim();

  const accessToken = loginWithFcm(email, fcmToken);

  check(accessToken, {
    [`[setup] real${i} FCM 등록 성공`]: (t) => t !== null && t !== undefined,
  });

  subscribeToTopic(accessToken, topic);

  console.log(`[setup] (${i}/${tokens.length}) ${email} → FCM 등록 + ${topic} 구독 완료`);
}
