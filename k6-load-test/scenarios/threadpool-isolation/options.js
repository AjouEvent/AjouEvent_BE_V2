/**
 * threadpool-isolation 시나리오 옵션
 *
 * K6_PRESET 환경변수로 선택합니다.
 *   single  단건 1회 요청 (동작 확인용)
 *   ramp    VU 단계별 급증 — gRPC 스레드 포화 시점을 드러냄
 *   rate    초당 고정 RPS (.env.local 의 RATE, DURATION, VUS 참고)
 *
 * 비교 시나리오:
 *   directExecutor  Firebase gRPC 스레드에서 콜백 직접 실행 (스레드풀 분리 없음)
 *   fcmCallbackExecutor  전용 격리 풀에서 콜백 실행 (현재 구현)
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

  // VU를 빠르게 증가시켜 directExecutor의 gRPC 스레드 포화 시점을 명확히 드러냅니다.
  // 총 소요: 약 50초
  // VisualVM Thread 탭에서 grpc-default-executor-* 스레드 상태 변화를 관찰하세요.
  ramp: {
    stages: [
      { duration: '5s',  target: 20  }, // 워밍업
      { duration: '10s', target: 60  }, // 중부하
      { duration: '10s', target: 120 }, // 고부하 — gRPC 스레드 포화 시작
      { duration: '15s', target: 200 }, // 극한 — HTTP 지연 폭발 구간
      { duration: '10s', target: 0   }, // 쿨다운
    ],
    thresholds,
  },

  // 초당 고정 RPS로 지속 부하를 줍니다.
  // directExecutor: gRPC 스레드가 DB I/O에 묶여 처리량 상한이 낮게 나타납니다.
  // 권장값: RATE=30, DURATION=60s, VUS=200
  rate: {
    scenarios: {
      fixed: {
        executor: 'constant-arrival-rate',
        rate: parseInt(__ENV.RATE || '30'),
        timeUnit: '1s',
        duration: __ENV.DURATION || '60s',
        preAllocatedVUs: parseInt(__ENV.VUS || '200'),
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
 * @param {string} label  시나리오 구분 레이블 (예: '[DIRECT]', '[ISOLATED]')
 * @param {number} vu     k6 __VU
 * @param {number} iter   k6 __ITER
 */
export function buildWebhookPayload(label, vu, iter) {
  const ts = Date.now();
  return JSON.stringify({
    title: `k6 스레드풀 격리 테스트 ${label} vu=${vu} iter=${iter} ts=${ts}`,
    content: 'k6 자동 생성 테스트입니다.',
    category: '공지사항',
    department: '아주대학교',
    englishTopic: 'AjouNormal',
    koreanTopic: '아주대학교-일반',
    url: `https://ajouevent.com/test/${vu}-${iter}-${ts}`,
    images: [],
    date: '2026-03-24T10:00:00',
  });
}
