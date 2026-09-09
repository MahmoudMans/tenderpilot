#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

export DB_URL="${DB_URL:-jdbc:postgresql://localhost:55432/tenderpilot}"
export DB_USERNAME="${DB_USERNAME:-tenderpilot}"
export DB_PASSWORD="${DB_PASSWORD:-tenderpilot_dev}"

export OPENSEARCH_URL="${OPENSEARCH_URL:-http://localhost:19200}"
export OLLAMA_URL="${OLLAMA_URL:-http://localhost:11434}"

export CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS:-http://localhost:3000}"

export ACCESS_TOKEN_ISSUER="${ACCESS_TOKEN_ISSUER:-tenderpilot-local}"

export ACCESS_TOKEN_SECRET="${ACCESS_TOKEN_SECRET:-tenderpilot-development-access-token-secret-2026-change-before-production}"

export ACCESS_TOKEN_TTL="${ACCESS_TOKEN_TTL:-PT15M}"

export REFRESH_TOKEN_TTL="${REFRESH_TOKEN_TTL:-P7D}"

export REFRESH_COOKIE_NAME="${REFRESH_COOKIE_NAME:-tenderpilot_refresh}"

export REFRESH_COOKIE_SECURE="${REFRESH_COOKIE_SECURE:-false}"

cd "${ROOT_DIR}/backend"

printf '\n'
printf '================================================================================\n'
printf 'TENDERPILOT — BACKEND DEVELOPMENT SERVER\n'
printf '================================================================================\n'
printf 'Backend URL: http://localhost:8080\n'
printf 'Database:    %s\n' "${DB_URL}"
printf 'OpenSearch:  %s\n' "${OPENSEARCH_URL}"
printf 'Auth issuer: %s\n' "${ACCESS_TOKEN_ISSUER}"
printf '================================================================================\n'
printf '\n'

exec mvn spring-boot:run
