#!/usr/bin/env bash

set -euo pipefail

BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
FRONTEND_URL="${FRONTEND_URL:-http://localhost:3000}"
OPENSEARCH_URL="${OPENSEARCH_URL:-http://localhost:19200}"

printf '\n'
printf '================================================================================\n'
printf 'TENDERPILOT — LOCAL RUNTIME STATUS\n'
printf '================================================================================\n'

printf '\n[1/4] Backend actuator\n'

curl \
  --fail \
  --silent \
  --show-error \
  "${BACKEND_URL}/actuator/health"

printf '\n'

printf '\n[2/4] TenderPilot system contract\n'

curl \
  --fail \
  --silent \
  --show-error \
  "${BACKEND_URL}/api/v1/system/status"

printf '\n'

printf '\n[3/4] OpenSearch\n'

curl \
  --fail \
  --silent \
  --show-error \
  "${OPENSEARCH_URL}"

printf '\n'

printf '\n[4/4] Frontend\n'

curl \
  --fail \
  --silent \
  --show-error \
  --head \
  "${FRONTEND_URL}/dashboard" \
  | sed -n '1,8p'

printf '\n'
printf '[PASS] RUNTIME_STATUS_CHECK\n'
