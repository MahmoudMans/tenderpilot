#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "${ROOT_DIR}"

printf '\n'
printf '================================================================================\n'
printf 'TENDERPILOT — LOCAL QUALITY GATE\n'
printf '================================================================================\n'

###############################################################################
# 1. SHELL SYNTAX
###############################################################################

printf '\n[1/6] Shell script syntax\n'

for script in scripts/*.sh; do

  printf 'Checking %s\n' "${script}"

  bash -n "${script}"

done

printf '[PASS] SHELL_SCRIPT_SYNTAX\n'

###############################################################################
# 2. BACKEND TESTS
###############################################################################

printf '\n[2/6] Backend tests\n'

(
  cd backend
  mvn test
)

printf '[PASS] BACKEND_TESTS\n'

###############################################################################
# 3. FRONTEND LINT
###############################################################################

printf '\n[3/6] Frontend lint\n'

(
  cd frontend
  npm run lint
)

printf '[PASS] FRONTEND_LINT\n'

###############################################################################
# 4. FRONTEND TYPESCRIPT
###############################################################################

printf '\n[4/6] Frontend TypeScript\n'

(
  cd frontend
  ./node_modules/.bin/tsc --noEmit
)

printf '[PASS] FRONTEND_TYPECHECK\n'

###############################################################################
# 5. FRONTEND BUILD
###############################################################################

printf '\n[5/6] Frontend production build\n'

(
  cd frontend
  npm run build
)

printf '[PASS] FRONTEND_BUILD\n'

###############################################################################
# 6. COMPOSE CONFIGURATION
###############################################################################

printf '\n[6/6] Docker Compose validation\n'

docker compose config >/dev/null

printf '[PASS] COMPOSE_VALIDATION\n'

###############################################################################
# FINAL
###############################################################################

printf '\n'
printf '================================================================================\n'
printf 'TENDERPILOT QUALITY GATE RESULT\n'
printf '================================================================================\n'
printf 'SHELL_SCRIPT_SYNTAX=PASS\n'
printf 'BACKEND_TESTS=PASS\n'
printf 'FRONTEND_LINT=PASS\n'
printf 'FRONTEND_TYPECHECK=PASS\n'
printf 'FRONTEND_BUILD=PASS\n'
printf 'COMPOSE_VALIDATION=PASS\n'
printf 'QUALITY_GATE_STATUS=PASS\n'
printf '================================================================================\n'
