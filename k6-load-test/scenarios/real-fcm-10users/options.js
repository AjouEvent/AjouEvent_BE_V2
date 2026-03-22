/**
 * real-fcm-10users 시나리오 옵션
 *
 * 실제 FCM 토큰 10개 기반 부하 테스트입니다.
 * 실제 FCM 네트워크 왕복이 발생하므로 async vs sync 차이가 뚜렷하게 나타납니다.
 *
 * K6_PRESET 환경변수로 선택합니다.
 *   single  단건 1회 요청 (동작 확인용)
 *   ramp    VU 단계적 증가 — 서블릿 스레드 점유 차이 측정
 *   rate    초당 고정 RPS (.env.local 의 RATE, DURATION, VUS 참고)
 */

const preset = __ENV.K6_PRESET || 'ramp';

const thresholds = {
  http_req_duration: ['p(95)<10000'],
  http_req_failed: ['rate<0.05'],
};

const presets = {
  single: {
    vus: 1,
    iterations: 1,
    thresholds,
  },

  // 실제 FCM 왕복이 있으므로 응답 시간이 가짜 토큰보다 깁니다.
  // VU를 늘리면 sync 경로에서 서블릿 스레드 고갈이 더 빠르게 나타납니다.
  ramp: {
    stages: [
      { duration: '5s',  target: 10 },
      { duration: '10s', target: 30 },
      { duration: '5s',  target: 0  },
    ],
    thresholds,
  },

  rate: {
    scenarios: {
      fixed: {
        executor: 'constant-arrival-rate',
        rate: parseInt(__ENV.RATE || '10'),
        timeUnit: '1s',
        duration: __ENV.DURATION || '30s',
        preAllocatedVUs: parseInt(__ENV.VUS || '50'),
      },
    },
    thresholds,
  },
};

export const scenarioOptions = presets[preset] || presets.ramp;

/**
 * @param {string} label  '[ASYNC]' | '[SYNC]'
 * @param {number} vu     k6 __VU
 * @param {number} iter   k6 __ITER
 */
export function buildWebhookPayload(label, vu, iter) {
  const ts = Date.now();
  return JSON.stringify({
    title: `k6 실FCM 테스트 ${label} vu=${vu} iter=${iter} ts=${ts}`,
    content: 'k6 실제 FCM 토큰 부하 테스트입니다.',
    category: '공지사항',
    department: '아주대학교',
    englishTopic: 'AjouNormal',
    koreanTopic: '아주대학교-일반',
    url: `https://ajouevent.com/test/real/${vu}-${iter}-${ts}`,
    images: [],
    date: '2026-03-21T10:00:00',
  });
}
