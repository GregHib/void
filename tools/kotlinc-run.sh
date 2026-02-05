#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD_ROOT="${BUILD_ROOT:-$ROOT_DIR/build/make-kotlinc}"
JAVA_CMD="${JAVA:-java}"

if [[ "$BUILD_ROOT" != /* ]]; then
  BUILD_ROOT="$ROOT_DIR/$BUILD_ROOT"
fi

cp_file="$BUILD_ROOT/run.classpath"
if [[ ! -f "$cp_file" ]]; then
  echo "[kotlinc-run] ERROR: classpath file not found: $cp_file" >&2
  echo "[kotlinc-run] Hint: make kt-build" >&2
  exit 2
fi

CLASSPATH="$(cat "$cp_file")"
if [[ -z "$CLASSPATH" ]]; then
  echo "[kotlinc-run] ERROR: empty classpath in $cp_file" >&2
  exit 2
fi

cd "$ROOT_DIR"
exec "$JAVA_CMD" -cp "$CLASSPATH" Main
