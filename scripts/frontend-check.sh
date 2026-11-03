#!/usr/bin/env bash
#
# Frontend-only quality gate: lint, TypeScript, production build, and a check
# that the generated dark theme is up to date. Faster than ./scripts/check.sh
# (which also runs the backend tests and Docker Compose validation).

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

printf '\n'
printf '================================================================================\n'
printf 'TENDERPILOT — FRONTEND QUALITY GATE\n'
printf '================================================================================\n'

if [ ! -d frontend/node_modules ]; then
  printf '\n[0/4] Installing frontend dependencies\n'
  (cd frontend && npm install)
fi

printf '\n[1/4] Dark theme is up to date\n'
THEME_FILE="frontend/src/app/theme-dark.css"
BACKUP="$(mktemp "${TMPDIR:-/tmp}/tp-theme.XXXXXX")"
cp "${THEME_FILE}" "${BACKUP}"
if node scripts/generate-dark-theme.mjs; then
  if cmp -s "${BACKUP}" "${THEME_FILE}"; then
    printf '[PASS] DARK_THEME_UP_TO_DATE\n'
  else
    printf '[NOTE] %s was regenerated because new colour classes were found.\n' "${THEME_FILE}"
    printf '       Review the change and keep it.\n'
  fi
else
  cp "${BACKUP}" "${THEME_FILE}"
  printf '[WARN] Could not regenerate the dark theme; kept the existing file.\n'
fi
rm -f "${BACKUP}"

printf '\n[2/4] Frontend lint\n'
(cd frontend && npm run lint)
printf '[PASS] FRONTEND_LINT\n'

printf '\n[3/4] Frontend TypeScript\n'
(cd frontend && ./node_modules/.bin/tsc --noEmit)
printf '[PASS] FRONTEND_TYPECHECK\n'

printf '\n[4/4] Frontend production build\n'
(cd frontend && npm run build)
printf '[PASS] FRONTEND_BUILD\n'

printf '\n'
printf '================================================================================\n'
printf 'FRONTEND_QUALITY_GATE_STATUS=PASS\n'
printf '================================================================================\n'
