#!/usr/bin/env bash
#
# Takes pictures of the whole TenderPilot interface and writes them, with a text for each screen, into
# ./portfolio so they can go straight into a portfolio. It uses the demo build (synthetic data), so no
# backend, database, search index or local AI is needed and nothing real appears in the pictures.
#
#   ./scripts/capture-portfolio.sh                 everything, about 5 to 10 minutes
#   ./scripts/capture-portfolio.sh --docs-only     rewrite README, gallery and pages from the last run
#
# Options (environment variables):
#   TP_ONLY=dashboard,tasks   only these screens (ids are in scripts/portfolio/catalog.mjs)
#   TP_QUICK=1                desktop light only, no full-page pictures
#   TP_SCALE=2                sharper desktop pictures (larger files)
#   TP_FORMAT=jpeg            smaller files than the default png
#   TP_OUT=/some/folder       write somewhere other than ./portfolio
#   TP_BASE_URL=http://...    take the pictures of an app that is already running (nothing is built or started)
#   TP_EMAIL / TP_PASSWORD    only for a real backend that asks you to sign in
#
# First run: installs the screenshot tool (playwright-core, a few MB) into portfolio/.tools. It uses Google
# Chrome or Edge when they are installed, and downloads Chromium (about 150 MB) when neither is.
#
# Git ignores the portfolio folder (it holds large pictures and would make the release gates see an uncommitted
# change). To commit it anyway: git add -f portfolio

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT_DIR="${TP_OUT:-${ROOT_DIR}/portfolio}"
TOOLS_DIR="${OUT_DIR}/.tools"
LOG_DIR="${TOOLS_DIR}/logs"

say() { printf '%s\n' "$*"; }
fail() { printf '%s\n' "$*" >&2; exit 1; }

case "${1:-}" in
  -h|--help)
    sed -n '2,23p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
    exit 0
    ;;
  --docs-only)
    command -v node >/dev/null 2>&1 || fail "node is required."
    node "${ROOT_DIR}/scripts/portfolio/render.mjs" "${OUT_DIR}"
    exit 0
    ;;
  "")
    ;;
  *)
    fail "Unknown option: $1 (try --help)"
    ;;
esac

command -v node >/dev/null 2>&1 || fail "node is required."
command -v npm >/dev/null 2>&1 || fail "npm is required."
NODE_MAJOR="$(node -p 'process.versions.node.split(".")[0]')"
if [ "${NODE_MAJOR}" -lt 20 ]; then
  fail "Node 20 or newer is needed (this is $(node -v))."
fi

mkdir -p "${LOG_DIR}"
printf '*\n' > "${TOOLS_DIR}/.gitignore"
# The pictures are large and would show up as an uncommitted change in the release gates, so git ignores the
# folder (a .gitignore inside it that ignores everything, itself included). Skipped if the folder has its own.
if [ ! -e "${OUT_DIR}/.gitignore" ]; then
  printf '*\n' > "${OUT_DIR}/.gitignore"
fi

# --- 1. The screenshot tool, installed once into portfolio/.tools -------------------------------------------
if [ ! -f "${TOOLS_DIR}/node_modules/playwright-core/package.json" ]; then
  say "Installing the screenshot tool (one time)..."
  printf '{ "name": "tenderpilot-portfolio-tools", "private": true }\n' > "${TOOLS_DIR}/package.json"
  if ! (cd "${TOOLS_DIR}" && npm install --no-audit --no-fund --loglevel=error playwright-core@1.56.0); then
    fail "Could not install playwright-core. Check your internet connection and run the script again."
  fi
fi

# --- 2. The app: build and start the demo, unless an app is already running ---------------------------------
PIDS=()
WEB_PORT=""
API_PORT=""

cleanup() {
  for pid in ${PIDS[@]+"${PIDS[@]}"}; do
    kill "${pid}" >/dev/null 2>&1 || true
  done
  for port in "${WEB_PORT}" "${API_PORT}"; do
    [ -n "${port}" ] || continue
    command -v lsof >/dev/null 2>&1 || continue
    for pid in $(lsof -ti "tcp:${port}" -sTCP:LISTEN 2>/dev/null || true); do
      kill "${pid}" >/dev/null 2>&1 || true
    done
  done
}
trap cleanup EXIT INT TERM

free_port() {
  node -e 'const s=require("net").createServer();s.listen(0,"127.0.0.1",()=>{console.log(s.address().port);s.close()})'
}

wait_for() {
  local url="$1" seconds="$2" waited=0
  while ! curl -fsS -o /dev/null "${url}" >/dev/null 2>&1; do
    waited=$((waited + 1))
    if [ "${waited}" -ge "${seconds}" ]; then return 1; fi
    sleep 1
  done
}

if [ -n "${TP_BASE_URL:-}" ]; then
  BASE_URL="${TP_BASE_URL}"
  say "Using the app that is already running at ${BASE_URL}"
else
  command -v curl >/dev/null 2>&1 || fail "curl is required."
  WEB_PORT="$(free_port)"
  API_PORT="$(free_port)"
  BASE_URL="http://localhost:${WEB_PORT}"
  export NEXT_PUBLIC_API_URL="http://localhost:${API_PORT}"

  if [ ! -d "${ROOT_DIR}/frontend/node_modules" ]; then
    say "Installing frontend dependencies..."
    (cd "${ROOT_DIR}/frontend" && npm install)
  fi

  say "Building the frontend for the demo (about a minute)..."
  if ! (cd "${ROOT_DIR}/frontend" && npm run build) > "${LOG_DIR}/build.log" 2>&1; then
    tail -n 40 "${LOG_DIR}/build.log" >&2
    fail "The frontend build failed (see above and ${LOG_DIR}/build.log)."
  fi

  say "Starting the demo API and the frontend..."
  PORT="${API_PORT}" node "${ROOT_DIR}/scripts/mock-api-server.mjs" > "${LOG_DIR}/api.log" 2>&1 &
  PIDS+=("$!")
  (cd "${ROOT_DIR}/frontend" && exec ./node_modules/.bin/next start -p "${WEB_PORT}") > "${LOG_DIR}/web.log" 2>&1 &
  PIDS+=("$!")

  wait_for "http://localhost:${API_PORT}/api/v1/system/status" 30 || fail "The demo API did not start (see ${LOG_DIR}/api.log)."
  wait_for "${BASE_URL}/login" 90 || fail "The frontend did not start (see ${LOG_DIR}/web.log)."
fi

# --- 3. Take the pictures -----------------------------------------------------------------------------------
say ""
say "Taking the pictures. This takes a few minutes; it is working while the numbers count up."
say ""
if ! TP_BASE_URL="${BASE_URL}" TP_OUT="${OUT_DIR}" TP_TOOLS_DIR="${TOOLS_DIR}" node "${ROOT_DIR}/scripts/portfolio/capture.mjs"; then
  fail "The capture did not finish. The messages above say why."
fi

say ""
say "Done. Open this in a browser to look through everything:"
say "  ${OUT_DIR}/index.html"
say "The explanations are in ${OUT_DIR}/README.md and ${OUT_DIR}/pages/."
