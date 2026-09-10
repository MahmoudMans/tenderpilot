#!/usr/bin/env bash
set -euo pipefail

MODEL="${OLLAMA_MODEL:-qwen3:4b}"
BASE_URL="${OLLAMA_BASE_URL:-http://localhost:11434}"

echo "============================================================"
echo "TenderPilot Local AI Preparation"
echo "============================================================"
echo "MODEL=${MODEL}"
echo "OLLAMA_BASE_URL=${BASE_URL}"

if ! command -v ollama >/dev/null 2>&1; then
  echo "[FAIL] Ollama command is not installed."
  echo "[INFO] Install Ollama first, then rerun this script."
  exit 1
fi

if ! curl -fsS "${BASE_URL}/api/tags" >/dev/null 2>&1; then
  echo "[FAIL] Ollama API is not reachable at ${BASE_URL}"
  echo "[INFO] Start Ollama and rerun this script."
  exit 1
fi

if ollama list 2>/dev/null \
  | awk 'NR > 1 {print $1}' \
  | grep -Fxq "${MODEL}"; then

  echo "[PASS] MODEL_ALREADY_AVAILABLE=${MODEL}"

else

  echo "[INFO] Pulling local model ${MODEL}"
  ollama pull "${MODEL}"

fi

echo "[PASS] LOCAL_AI_READY"
