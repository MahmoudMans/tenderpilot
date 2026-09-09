#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "${ROOT_DIR}"

POSTGRES_USER="${POSTGRES_USER:-tenderpilot}"
POSTGRES_DB="${POSTGRES_DB:-tenderpilot}"
OPENSEARCH_URL="${OPENSEARCH_URL:-http://localhost:19200}"

if ! command -v docker >/dev/null 2>&1; then
  printf '[FAIL] Docker is required.\n' >&2
  exit 1
fi

if ! command -v curl >/dev/null 2>&1; then
  printf '[FAIL] curl is required.\n' >&2
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  printf '[FAIL] Docker Compose v2 is required.\n' >&2
  exit 1
fi

printf '\n'
printf '================================================================================\n'
printf 'TENDERPILOT — START LOCAL INFRASTRUCTURE\n'
printf '================================================================================\n'

docker compose up -d postgres opensearch

printf '\nWaiting for PostgreSQL...\n'

POSTGRES_READY=0

for attempt in $(seq 1 60); do

  if docker compose exec -T postgres \
    pg_isready \
    -U "${POSTGRES_USER}" \
    -d "${POSTGRES_DB}" \
    >/dev/null 2>&1; then

    POSTGRES_READY=1
    break

  fi

  sleep 1

done

if [[ "${POSTGRES_READY}" -ne 1 ]]; then

  printf '[FAIL] PostgreSQL did not become ready.\n' >&2

  docker compose logs \
    --tail=100 \
    postgres \
    || true

  exit 1

fi

printf '[PASS] POSTGRES_READY\n'

printf '\nWaiting for OpenSearch...\n'

OPENSEARCH_READY=0

for attempt in $(seq 1 120); do

  if curl \
    --fail \
    --silent \
    --show-error \
    "${OPENSEARCH_URL}" \
    >/dev/null 2>&1; then

    OPENSEARCH_READY=1
    break

  fi

  sleep 1

done

if [[ "${OPENSEARCH_READY}" -ne 1 ]]; then

  printf '[FAIL] OpenSearch did not become ready.\n' >&2

  docker compose logs \
    --tail=150 \
    opensearch \
    || true

  exit 1

fi

printf '[PASS] OPENSEARCH_READY\n'

printf '\n'
printf 'POSTGRES_STATUS=UP\n'
printf 'OPENSEARCH_STATUS=UP\n'
printf 'INFRASTRUCTURE_STATUS=READY\n'
