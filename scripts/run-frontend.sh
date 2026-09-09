#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

export NEXT_PUBLIC_API_URL="${NEXT_PUBLIC_API_URL:-http://localhost:8080}"

cd "${ROOT_DIR}/frontend"

printf '\n'
printf '================================================================================\n'
printf 'TENDERPILOT — FRONTEND DEVELOPMENT SERVER\n'
printf '================================================================================\n'
printf 'Frontend URL: http://localhost:3000\n'
printf 'Backend URL:  %s\n' "${NEXT_PUBLIC_API_URL}"
printf '================================================================================\n'
printf '\n'

exec npm run dev
