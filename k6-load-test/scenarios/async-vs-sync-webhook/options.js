/**
 * async-vs-sync-webhook 시나리오 옵션
 *
 * K6_PRESET 환경변수로 선택합니다.
 *   single  단건 1회 요청 (동작 확인용)
 *   ramp    VU 단계별 증가 — 동기 vs 비동기 스레드 점유 차이 측정에 적합
 *   rate    초당 고정 RPS (.env.local 의 RATE, DURATION, VUS 참고)
 */

const preset = __ENV.K6_PRESET || 'ramp';

const thresholds = {
  http_req_duration: ['p(95)<5000'],
  http_req_failed: ['rate<0.05'],
};

const presets = {
  single: {
    vus: 1,
    iterations: 1,
    thresholds,
  },

  // VU를 단계적으로 증가시켜 스레드 점유 차이를 측정합니다.
  // 총 소요: 약 20초 — VisualVM 캡처 1장으로 비교 가능
  ramp: {
    stages: [
      { duration: '5s',  target: 20 },
      { duration: '10s', target: 50 },
      { duration: '5s',  target: 0  },
    ],
    thresholds,
  },

  // 초당 고정 RPS로 서버를 밀어붙입니다.
  // 동기 시 스레드가 FCM I/O에 묶이면 큐가 쌓이면서 p95 급등·에러 발생을 관찰할 수 있습니다.
  // 권장값: RATE=20, DURATION=60s, VUS=100
  rate: {
    scenarios: {
      fixed: {
        executor: 'constant-arrival-rate',
        rate: parseInt(__ENV.RATE || '20'),
        timeUnit: '1s',
        duration: __ENV.DURATION || '60s',
        preAllocatedVUs: parseInt(__ENV.VUS || '100'),
      },
    },
    thresholds,
  },
};

export const scenarioOptions = presets[preset] || presets.ramp;

/**
 * 웹훅 Payload 빌더
 *
 * title 과 url 에 VU번호 + 반복횟수를 붙여 매 요청을 고유하게 만듭니다.
 * → isDuplicateNotice() 중복 체크를 통과하기 위해 필요합니다.
 *
 * @param {string} label  시나리오 구분 레이블 (예: '[SYNC]', '[ASYNC]')
 * @param {number} vu     k6 __VU
 * @param {number} iter   k6 __ITER
 */
export function buildWebhookPayload(label, vu, iter) {
  const ts = Date.now();
  return JSON.stringify({
    title: `k6 부하 테스트 ${label} vu=${vu} iter=${iter} ts=${ts}`,
    content: 'k6 자동 생성 테스트입니다.',
    category: '공지사항',
    department: '아주대학교',
    englishTopic: 'AjouNormal',
    koreanTopic: '아주대학교-일반',
    url: `https://ajouevent.com/test/${vu}-${iter}-${ts}`,
    images: [],
    date: '2026-03-21T10:00:00',
  });
}
