/**
 * 인증 관련 공통 함수
 *
 * [setup() 전용] — k6 VU 실행 전 1회만 호출됩니다.
 * VU 내부(export default function)에서는 사용하지 마세요.
 */
import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

/**
 * FCM 토큰과 함께 테스트 로그인
 *
 * - POST /api/v2/auth/test-login/fcm
 * - FCM 토큰을 DB에 등록하고 JWT accessToken 반환
 * - local 프로파일에서만 동작
 *
 * @param {string} email      테스트 유저 이메일
 * @param {string} fcmToken   브라우저에서 발급받은 FCM 토큰
 * @returns {string}          JWT accessToken
 */
export function loginWithFcm(email, fcmToken) {
  const res = http.post(
    `${BASE_URL}/api/v2/auth/test-login/fcm`,
    JSON.stringify({ email, fcmToken }),
    { headers: { 'Content-Type': 'application/json' } },
  );

  check(res, {
    [`[setup] loginWithFcm(${email}) → 200`]: (r) => r.status === 200,
  });

  if (res.status !== 200) {
    console.error(`[setup] loginWithFcm 실패 email=${email} status=${res.status} body=${res.body}`);
  }

  return res.json('accessToken');
}

/**
 * JWT 없이 테스트 로그인 (FCM 토큰 등록 불필요한 경우)
 *
 * - POST /api/v2/auth/test-login
 * - local 프로파일에서만 동작
 *
 * @param {string} email  테스트 유저 이메일
 * @returns {string}      JWT accessToken
 */
export function login(email) {
  const res = http.post(
    `${BASE_URL}/api/v2/auth/test-login`,
    JSON.stringify({ email }),
    { headers: { 'Content-Type': 'application/json' } },
  );

  check(res, {
    [`[setup] login(${email}) → 200`]: (r) => r.status === 200,
  });

  return res.json('accessToken');
}

/**
 * 토픽 구독
 *
 * - POST /api/v2/topics/subscriptions
 * - JWT 인증 필요
 *
 * @param {string} accessToken  JWT accessToken
 * @param {string} topic        구독할 토픽 영문명 (예: 'AjouNormal')
 */
export function subscribeToTopic(accessToken, topic) {
  const res = http.post(
    `${BASE_URL}/api/v2/topics/subscriptions`,
    JSON.stringify({ topic }),
    {
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${accessToken}`,
      },
    },
  );

  check(res, {
    [`[setup] subscribeToTopic(${topic}) → 204`]: (r) => r.status === 204,
  });

  if (res.status !== 204) {
    console.error(`[setup] subscribeToTopic 실패 topic=${topic} status=${res.status} body=${res.body}`);
  }
}

/**
 * 크롤링 토큰 발급
 *
 * - POST /api/v2/auth/test-crawling-token
 * - Redis에 크롤링 토큰 저장 후 반환
 * - local 프로파일 + 인증 불필요
 *
 * @returns {string}  crawlingToken
 */
export function generateCrawlingToken() {
  const res = http.post(`${BASE_URL}/api/v2/auth/test-crawling-token`);

  check(res, {
    '[setup] generateCrawlingToken → 200': (r) => r.status === 200,
  });

  if (res.status !== 200) {
    console.error(`[setup] generateCrawlingToken 실패 status=${res.status} body=${res.body}`);
  }

  return res.json('crawlingToken');
}
