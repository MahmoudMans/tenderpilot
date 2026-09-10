#!/usr/bin/env bash
set -euo pipefail

GENERATION_MODEL="${OLLAMA_MODEL:-qwen3:4b}"
EMBEDDING_MODEL="${OLLAMA_EMBEDDING_MODEL:-nomic-embed-text-v2-moe}"
BASE_URL="${OLLAMA_BASE_URL:-http://localhost:11434}"

echo "============================================================"
echo "TenderPilot Local AI Preparation"
echo "============================================================"
echo "GENERATION_MODEL=${GENERATION_MODEL}"
echo "EMBEDDING_MODEL=${EMBEDDING_MODEL}"
echo "OLLAMA_BASE_URL=${BASE_URL}"

if ! command -v ollama >/dev/null 2>&1; then

  echo "[FAIL] Ollama command is not installed."
  exit 1

fi

if ! curl -fsS "${BASE_URL}/api/tags" >/dev/null 2>&1; then

  echo "[FAIL] Ollama API is not reachable at ${BASE_URL}"
  exit 1

fi

ensure_model() {

  local model="$1"

  if ollama list 2>/dev/null \
    | awk 'NR > 1 {print $1}' \
    | grep -Fxq "${model}"; then

    echo "[PASS] MODEL_ALREADY_AVAILABLE=${model}"

  else

    echo "[INFO] Pulling local model ${model}"

    ollama pull "${model}"

  fi
}

ensure_model "${GENERATION_MODEL}"
ensure_model "${EMBEDDING_MODEL}"

echo "[PASS] LOCAL_GENERATION_MODEL_READY=${GENERATION_MODEL}"
echo "[PASS] LOCAL_EMBEDDING_MODEL_READY=${EMBEDDING_MODEL}"
echo "[PASS] TENDERPILOT_LOCAL_RAG_READY"
