#!/usr/bin/env bash
# bootstrap-coursier.sh
#
# Purpose:
#   Download a repo-local `coursier` launcher (`cs`) into `./.cs/cs` so the
#   Makefile build can fetch Maven dependencies without a system install.
#
# How it works:
#   Detects OS/arch, downloads the matching `cs-<arch>-<os>.gz` from the latest
#   GitHub release, decompresses it, marks it executable, and atomically moves
#   it into place.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

OS_RAW="$(uname -s)"
case "${OS_RAW}" in
  Linux) OS="pc-linux" ;;
  Darwin) OS="apple-darwin" ;;
  *)
    echo "Unsupported OS: ${OS_RAW}"
    exit 2
    ;;
esac

ARCH_RAW="$(uname -m)"
case "${ARCH_RAW}" in
  x86_64|amd64) ARCH="x86_64" ;;
  aarch64|arm64) ARCH="aarch64" ;;
  *)
    echo "Unsupported arch: ${ARCH_RAW}"
    exit 2
    ;;
esac

DEST_DIR="$ROOT_DIR/.cs"
DEST_BIN="$DEST_DIR/cs"
TMP_BIN="${DEST_BIN}.tmp.$$"

URL="https://github.com/coursier/coursier/releases/latest/download/cs-${ARCH}-${OS}.gz"

mkdir -p "$DEST_DIR"

echo "Downloading coursier launcher..."
echo "  ${URL}"
curl -fsSL "$URL" | gzip -d > "$TMP_BIN"
chmod +x "$TMP_BIN"
mv "$TMP_BIN" "$DEST_BIN"

cat <<EOF
Done.

Use it:
  $DEST_BIN --help
EOF
