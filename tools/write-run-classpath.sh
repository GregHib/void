#!/usr/bin/env bash
# write-run-classpath.sh
#
# Purpose:
#   Write `<build-root>/run.classpath`, the full runtime classpath used by
#   `tools/kotlinc-run.sh` to run `Main`.
#
# How it works:
#   - Collects Kotlin runtime jars from `KOTLIN_HOME/lib`.
#   - Appends Maven deps from `MAVEN_CP_FILE` (may point to `vendor/...`).
#   - Appends the built module jars in dependency order.
#   - Writes a single `:`-separated classpath string to `run.classpath`.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

BUILD_ROOT="${BUILD_ROOT:-$ROOT_DIR/build/make-kotlinc}"
KOTLIN_HOME="${KOTLIN_HOME:-$ROOT_DIR/.kotlin/kotlin-compiler-2.2.20}"
MAVEN_CP_FILE="${MAVEN_CP_FILE:-$BUILD_ROOT/maven.classpath}"
INCLUDE_DB="${INCLUDE_DB:-0}"

if [[ "$BUILD_ROOT" != /* ]]; then BUILD_ROOT="$ROOT_DIR/$BUILD_ROOT"; fi
if [[ "$KOTLIN_HOME" != /* ]]; then KOTLIN_HOME="$ROOT_DIR/$KOTLIN_HOME"; fi
if [[ "$MAVEN_CP_FILE" != /* ]]; then MAVEN_CP_FILE="$ROOT_DIR/$MAVEN_CP_FILE"; fi

modules=(buffer types config cache network engine game)
if [[ "$INCLUDE_DB" == "1" ]]; then
  modules=(buffer types config cache network engine database game)
fi

shopt -s nullglob
runtime_kotlin_jars=(
  "$KOTLIN_HOME"/lib/kotlin-stdlib*.jar
  "$KOTLIN_HOME"/lib/kotlin-reflect*.jar
  "$KOTLIN_HOME"/lib/kotlin-script-runtime*.jar
  "$KOTLIN_HOME"/lib/kotlin-annotations-jvm*.jar
)
shopt -u nullglob

KOTLIN_RT_CP="$(IFS=:; echo "${runtime_kotlin_jars[*]}")"

MAVEN_CP=""
if [[ -f "$MAVEN_CP_FILE" ]]; then
  MAVEN_CP="$(cat "$MAVEN_CP_FILE" || true)"
fi

module_cp_parts=()
for m in "${modules[@]}"; do
  jar="$BUILD_ROOT/$m/$m.jar"
  if [[ ! -f "$jar" ]]; then
    echo "[write-run-classpath] ERROR: missing module jar: $jar" >&2
    exit 2
  fi
  module_cp_parts+=("$jar")
done
MODULE_CP="$(IFS=:; echo "${module_cp_parts[*]}")"

run_cp="$KOTLIN_RT_CP"
if [[ -n "$MAVEN_CP" ]]; then run_cp="$run_cp:$MAVEN_CP"; fi
run_cp="$run_cp:$MODULE_CP"

mkdir -p "$BUILD_ROOT"
echo -n "$run_cp" >"$BUILD_ROOT/run.classpath"
echo "[write-run-classpath] Wrote $BUILD_ROOT/run.classpath" >&2
