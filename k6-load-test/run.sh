#!/usr/bin/env bash
# 사용법: ./k6-load-test/run.sh <파일경로> [preset]
#
# preset:
#   single  단건 1회 요청 (기본값 — 동작 확인용)
#   ramp    VU 단계별 증가
#   rate    고정 RPS (.env.local 의 RATE, DURATION, VUS 참고)
#
# 예시:
#   ./k6-load-test/run.sh k6-load-test/scenarios/async-vs-sync-webhook/async-webhook.js
#   ./k6-load-test/run.sh k6-load-test/scenarios/async-vs-sync-webhook/async-webhook.js ramp
#   ./k6-load-test/run.sh k6-load-test/scenarios/async-vs-sync-webhook/async-webhook.js rate

FILE=$1
PRESET=${2:-single}
ENV_FILE="$(dirname "$0")/.env.local"

echo "<시나리오 시작: $FILE | preset: $PRESET>"

# .env.local 의 모든 변수를 -e 로 동적 변환
ENV_ARGS=()
if [ -f "$ENV_FILE" ]; then
  while IFS='=' read -r key value; do
    [[ "$key" =~ ^[[:space:]]*# ]] && continue
    [[ -z "${key// }" ]] && continue
    ENV_ARGS+=(-e "${key}=${value}")
  done < "$ENV_FILE"
fi

RESULT_FILE="k6-load-test/results/result-$(basename "$FILE" .js)-${PRESET}-$(date +%Y%m%d-%H%M%S).json"
mkdir -p "$(dirname "$RESULT_FILE")"

k6 run \
  "${ENV_ARGS[@]}" \
  -e K6_PRESET="$PRESET" \
  --out "json=$RESULT_FILE" \
  "$FILE"
