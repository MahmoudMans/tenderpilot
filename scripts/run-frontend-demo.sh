#!/usr/bin/env bash
#
# Runs the frontend against the synthetic demo API — no backend, Postgres,
# OpenSearch or Ollama required. Handy for design work and screenshots.
#
#   ./scripts/run-frontend-demo.sh            # normal demo account
#   MOCK_FRESH=1 ./scripts/run-frontend-demo.sh   # brand-new account (onboarding)

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MOCK_PORT="${MOCK_PORT:-8080}"

export NEXT_PUBLIC_API_URL="http://localhost:${MOCK_PORT}"

command -v node >/dev/null 2>&1 || { printf 'node is required.\n' >&2; exit 1; }

if [ ! -d "${ROOT_DIR}/frontend/node_modules" ]; then
  printf 'Installing frontend dependencies...\n'
  (cd "${ROOT_DIR}/frontend" && npm install)
fi

PORT="${MOCK_PORT}" node "${ROOT_DIR}/scripts/mock-api-server.mjs" &
MOCK_PID=$!

cleanup() {
  kill "${MOCK_PID}" >/dev/null 2>&1 || true
}
trap cleanup EXIT INT TERM

printf '\n'
printf '================================================================================\n'
printf 'TENDERPILOT — FRONTEND DEMO (synthetic data, no backend)\n'
printf '================================================================================\n'
printf 'Frontend URL: http://localhost:3000\n'
printf 'Demo API:     %s\n' "${NEXT_PUBLIC_API_URL}"
printf 'Sign in with any email and password.\n'
printf '================================================================================\n'
printf '\n'

cd "${ROOT_DIR}/frontend"
npm run dev
