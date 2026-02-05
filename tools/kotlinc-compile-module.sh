#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="${ROOT_DIR:-$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)}"

MODULE="${MODULE:?MODULE env var required}"
OUT_JAR="${OUT_JAR:?OUT_JAR env var required}"
BUILD_ROOT="${BUILD_ROOT:-$ROOT_DIR/build/make-kotlinc}"

KOTLIN_HOME="${KOTLIN_HOME:-$ROOT_DIR/.kotlin/kotlin-compiler-2.2.20}"
KOTLINC="${KOTLINC:-$KOTLIN_HOME/bin/kotlinc}"

MAVEN_CP_FILE="${MAVEN_CP_FILE:-$BUILD_ROOT/maven.classpath}"
MODULE_DEPS_CP="${MODULE_DEPS_CP:-}"

SCRIPTS_TXT_FILE="${SCRIPTS_TXT_FILE:-$BUILD_ROOT/generated/scripts.txt}"

KOTLINC_JAVA_OPTS="${KOTLINC_JAVA_OPTS:--Xmx6g -Xms512m}"
EXTRA_KOTLINC_FLAGS="${EXTRA_KOTLINC_FLAGS:-}"

if [[ "$BUILD_ROOT" != /* ]]; then BUILD_ROOT="$ROOT_DIR/$BUILD_ROOT"; fi
if [[ "$KOTLIN_HOME" != /* ]]; then KOTLIN_HOME="$ROOT_DIR/$KOTLIN_HOME"; fi
if [[ "$KOTLINC" != /* ]]; then KOTLINC="$ROOT_DIR/$KOTLINC"; fi
if [[ "$MAVEN_CP_FILE" != /* ]]; then MAVEN_CP_FILE="$ROOT_DIR/$MAVEN_CP_FILE"; fi
if [[ "$SCRIPTS_TXT_FILE" != /* ]]; then SCRIPTS_TXT_FILE="$ROOT_DIR/$SCRIPTS_TXT_FILE"; fi
if [[ "$OUT_JAR" != /* ]]; then OUT_JAR="$ROOT_DIR/$OUT_JAR"; fi

src_dir="$ROOT_DIR/$MODULE/src/main/kotlin"
res_dir="$ROOT_DIR/$MODULE/src/main/resources"

out_dir="$BUILD_ROOT/$MODULE"
classes_dir="$out_dir/classes"
sources_file="$out_dir/sources.txt"

mkdir -p "$classes_dir"
rm -rf "$classes_dir"/*

MAVEN_CP=""
if [[ -f "$MAVEN_CP_FILE" ]]; then
  MAVEN_CP="$(cat "$MAVEN_CP_FILE" || true)"
fi

shopt -s nullglob
runtime_kotlin_jars=(
  "$KOTLIN_HOME"/lib/kotlin-stdlib*.jar
  "$KOTLIN_HOME"/lib/kotlin-reflect*.jar
  "$KOTLIN_HOME"/lib/kotlin-script-runtime*.jar
  "$KOTLIN_HOME"/lib/kotlin-annotations-jvm*.jar
)
shopt -u nullglob

if [[ ${#runtime_kotlin_jars[@]} -eq 0 ]]; then
  echo "[kotlinc-module] ERROR: kotlin runtime jars not found under: $KOTLIN_HOME/lib" >&2
  exit 2
fi

KOTLIN_RT_CP="$(IFS=:; echo "${runtime_kotlin_jars[*]}")"

KOTLIN_FLAGS=(
  -jvm-target 21
  -Xinline-classes
  -Xcontext-parameters
  -Xjvm-default=all-compatibility
)

extra_flags=()
if [[ -n "$EXTRA_KOTLINC_FLAGS" ]]; then
  # shellcheck disable=SC2206
  extra_flags=($EXTRA_KOTLINC_FLAGS)
fi

cp="$KOTLIN_RT_CP"
if [[ -n "$MAVEN_CP" ]]; then cp="$cp:$MAVEN_CP"; fi
if [[ -n "$MODULE_DEPS_CP" ]]; then cp="$cp:$MODULE_DEPS_CP"; fi

if [[ -d "$src_dir" ]]; then
  mapfile -t sources < <(find "$src_dir" -type f -name '*.kt' -print | sort)
else
  sources=()
fi

if [[ ${#sources[@]} -gt 0 ]]; then
  printf "%s\n" "${sources[@]}" >"$sources_file"
  echo "[kotlinc-module] Compiling $MODULE ($(( ${#sources[@]} )) files)"
  JAVA_OPTS="$KOTLINC_JAVA_OPTS" "$KOTLINC" "${KOTLIN_FLAGS[@]}" "${extra_flags[@]}" -classpath "$cp" -d "$classes_dir" @"$sources_file"
else
  echo "[kotlinc-module] No Kotlin sources for $MODULE"
fi

if [[ -d "$res_dir" ]]; then
  (cd "$res_dir" && tar -cf - .) | (cd "$classes_dir" && tar -xf -)
fi

if [[ "$MODULE" == "game" && -f "$SCRIPTS_TXT_FILE" ]]; then
  cp "$SCRIPTS_TXT_FILE" "$classes_dir/scripts.txt"
fi

mkdir -p "$(dirname "$OUT_JAR")"
echo "[kotlinc-module] Jarring $MODULE -> $OUT_JAR"
(cd "$classes_dir" && jar cf "$OUT_JAR" .)
