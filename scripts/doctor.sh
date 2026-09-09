#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "${ROOT_DIR}"

printf '\n'
printf 'TenderPilot development doctor\n'
printf '%s\n' '----------------------------------------'

printf 'Java:      '
java -version 2>&1 | sed -n '1p'

printf 'Maven:     '
mvn -version | sed -n '1p'

printf 'Node:      %s\n' "$(node --version)"
printf 'npm:       %s\n' "$(npm --version)"
printf 'Git:       %s\n' "$(git --version)"
printf 'Docker:    %s\n' "$(docker --version)"

printf 'Compose:   '
docker compose version

printf '%s\n' '----------------------------------------'
printf 'Project:   %s\n' "${ROOT_DIR}"
