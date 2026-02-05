#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

KOTLIN_VERSION="${1:-2.2.20}"

OS_RAW="$(uname -s)"
case "${OS_RAW}" in
  Linux|Darwin) ;;
  *)
    echo "Unsupported OS: ${OS_RAW}"
    exit 2
    ;;
esac

DEST_DIR="$ROOT_DIR/.kotlin/kotlin-compiler-${KOTLIN_VERSION}"
TMP_DIR="${DEST_DIR}.tmp.$$"
ARCHIVE="/tmp/kotlin-compiler-${KOTLIN_VERSION}.zip"

URL="https://github.com/JetBrains/kotlin/releases/download/v${KOTLIN_VERSION}/kotlin-compiler-${KOTLIN_VERSION}.zip"

echo "Downloading Kotlin compiler ${KOTLIN_VERSION}..."
echo "  ${URL}"
curl -fsSL -o "$ARCHIVE" "$URL"

rm -rf "$TMP_DIR"
mkdir -p "$TMP_DIR"

echo "Extracting to ${DEST_DIR} ..."
unzip -q "$ARCHIVE" -d "$TMP_DIR"

rm -rf "$DEST_DIR"
mv "$TMP_DIR/kotlinc" "$DEST_DIR"

cat <<EOF
Done.

Use it:
  export KOTLIN_HOME="$DEST_DIR"
  export PATH="\$KOTLIN_HOME/bin:\$PATH"

Verify:
  kotlinc -version
EOF

