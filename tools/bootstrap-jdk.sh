#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

JDK_MAJOR="${1:-21}"

case "${JDK_MAJOR}" in
  8|11|17|21) ;;
  *)
    echo "Usage: tools/bootstrap-jdk.sh {8|11|17|21}"
    echo "Downloads an Eclipse Temurin JDK into $ROOT_DIR/.jdk/temurin<JDK_MAJOR> (no system install required)."
    exit 2
    ;;
esac

OS_RAW="$(uname -s)"
case "${OS_RAW}" in
  Linux) OS="linux" ;;
  Darwin) OS="mac" ;;
  *)
    echo "Unsupported OS: ${OS_RAW}"
    exit 2
    ;;
esac

ARCH_RAW="$(uname -m)"
case "${ARCH_RAW}" in
  x86_64|amd64) ARCH="x64" ;;
  aarch64|arm64) ARCH="aarch64" ;;
  *)
    echo "Unsupported arch: ${ARCH_RAW}"
    exit 2
    ;;
esac

DEST_DIR="$ROOT_DIR/.jdk/temurin${JDK_MAJOR}"
TMP_DIR="${DEST_DIR}.tmp.$$"
ARCHIVE="/tmp/temurin${JDK_MAJOR}-${OS}-${ARCH}-jdk.tar.gz"

URL="https://api.adoptium.net/v3/binary/latest/${JDK_MAJOR}/ga/${OS}/${ARCH}/jdk/hotspot/normal/eclipse"

echo "Downloading Temurin JDK ${JDK_MAJOR} for ${OS}/${ARCH}..."
echo "  ${URL}"
curl -fsSL -o "$ARCHIVE" "$URL"

rm -rf "$TMP_DIR"
mkdir -p "$TMP_DIR"

echo "Extracting to ${DEST_DIR} ..."
tar -xzf "$ARCHIVE" -C "$TMP_DIR" --strip-components=1

rm -rf "$DEST_DIR"
mv "$TMP_DIR" "$DEST_DIR"

cat <<EOF
Done.

Use it for builds:
  export JAVA_HOME="$DEST_DIR"
  export PATH="\$JAVA_HOME/bin:\$PATH"

Then:
  make build
or:
  ./gradlew :game:run
EOF
