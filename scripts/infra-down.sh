#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "${ROOT_DIR}"

printf '\n'
printf 'Stopping TenderPilot local infrastructure...\n'

docker compose down

printf '[PASS] INFRASTRUCTURE_STOPPED\n'
