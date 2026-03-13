#!/usr/bin/env bash

set -euo pipefail

# --- Defaults ---
INITIAL_DELAY=1
ITERATIONS=1
MAX_CONCURRENT=10
MAX_RETRIES=5
LOG_FILE="load_test_results.csv"

usage() {
  cat <<EOF
Usage: $(basename "$0") [-d delay] [-i iterations] [-h]

Options:
  -d    Initial delay in seconds (default: $INITIAL_DELAY)
  -i    Number of iterations (default: $ITERATIONS)
  -h    Display this help message

Example:
  $(basename "$0") -d 0.5 -i 500
EOF
  exit 1
}

while getopts ":d:i:h" opt; do
  case "$opt" in
    d) INITIAL_DELAY="$OPTARG" ;;
    i) ITERATIONS="$OPTARG" ;;
    h) usage ;;
    \?) echo "Invalid option: -$OPTARG" >&2; usage ;;
    :)  echo "Option -$OPTARG requires an argument." >&2; usage ;;
  esac
done

shift $((OPTIND -1))

# --- Configuration ---
JSONSERVER_SOURCE_API_URL="http://localhost:9091/documents"
CHAINVAULT_TARGET_API_URL="http://localhost:8085/chainvault/process"

export CHAINVAULT_TARGET_API_URL INITIAL_DELAY MAX_RETRIES LOG_FILE

do_post() {
  local iter="$1"
  local id="$2"
  local delay="$INITIAL_DELAY"
  local attempt=1

  while [[ "$attempt" -le "$MAX_RETRIES" ]]; do
    status_code=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$CHAINVAULT_TARGET_API_URL" \
         -H "Content-Type: application/json" \
         -d "{\"docId\": \"$id\"}")

    printf "%s,%s,%s,%s\n" "$iter" "$id" "$status_code" "$(date '+%Y-%m-%d %H:%M:%S')" >> "$LOG_FILE"

    if [[ "$status_code" -eq 429 ]]; then
      jitter_ms=$(( RANDOM % 500 ))
      actual_sleep=$(awk "BEGIN {print $delay + ($jitter_ms / 1000)}")
      echo "[Iter $iter] ID $id: 429 detected. Retry $attempt in ${actual_sleep}s..."
      sleep "$actual_sleep"
      delay=$(awk "BEGIN {print $delay * 2}")
      attempt=$(( attempt + 1 ))
    else
      return 0
    fi
  done
}
export -f do_post

# Initialize CSV Log
echo "iteration,id,status_code,timestamp" > "$LOG_FILE"
echo "Starting load test: $ITERATIONS iterations, ${INITIAL_DELAY}s initial delay."

# 1. Start Global Timer
global_start=$(date +%s)

# 2. Main Iteration Loop
for ((i=1; i<=ITERATIONS; i++)); do
  echo "--- Iteration $i of $ITERATIONS ---"
  ids=$(curl -s "$JSONSERVER_SOURCE_API_URL" | jq -r '.[].id' || echo "")

  if [[ -z "$ids" ]]; then
    echo "No IDs found. Skipping..."
    continue
  fi

  # 3. Parallel Execution
  echo "$ids" | xargs -P "$MAX_CONCURRENT" -I {} bash -c "do_post $i {}"
done

# 4. Final Summary
global_end=$(date +%s)
echo "------------------------------------"
echo "Complete. Total duration: $((global_end - global_start))s"
echo "Results: $LOG_FILE"
